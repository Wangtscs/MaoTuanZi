/**
 * QR 码生成工具（纯 ArkTS 手写实现，无第三方依赖）
 *
 * 支持：字节模式（8-bit）、纠错级别 L、版本 1~5（自动选最小可容纳版本）。
 * - 版本 1（21×21）：最多 17 字节
 * - 版本 2（25×25）：最多 32 字节
 * - 版本 3（29×29）：最多 53 字节
 * - 版本 4（33×33）：最多 78 字节
 * - 版本 5（37×37）：最多 106 字节
 * 注：以上字节数已扣除 4 位模式指示符 + 8 位字符计数指示符的开销。
 *
 * 完整实现：GF(256) Reed-Solomon 纠错码、终止符、填充字节、对齐图案、
 * 格式信息（BCH(15,5) 纠错）、8 种掩码择优（4 条规则惩罚评分）、静区。
 * 实现依据 ISO/IEC 18004。
 *
 * 输出：boolean[][] 矩阵（true=黑模块），可交由 Canvas 绘制或转 SVG Data URL。
 *
 * 典型用途：生成深度链接 `maotuanzi://square/post/{id}`（约 25~30 字节 → 版本 2），
 * 系统扫码后可直接拉起 App 打开对应帖子。
 *
 * ⚠️ 修改本文件后请运行 apps/mobile/tools/qr-verify/verify_all.py 回归，
 *    该脚本会用 OpenCV 真实解码校验产物是否仍可扫描。
 */
export class QrCodeEncoder {
  /** 支持的最高版本（V1~V5 纠错级别 L 均为单纠错块，无需块交错逻辑） */
  private static readonly MAX_VERSION: number = 5

  /** 字节模式 L 级可编码字节容量（index = version） */
  private static readonly BYTE_CAP: number[] = [0, 17, 32, 53, 78, 106]
  /** 总数据码字数（index = version） */
  private static readonly TOTAL_DATA_CW: number[] = [0, 19, 34, 55, 80, 108]
  /** 每块纠错码字数（V1~V5 均为单块） */
  private static readonly EC_CW: number[] = [0, 7, 10, 15, 20, 26]
  /** 对齐图案中心坐标（index = version；与探测图形重叠的组合在使用时跳过） */
  private static readonly ALIGN: number[][] = [
    [], [], [6, 18], [6, 22], [6, 26], [6, 30]
  ]

  // ===== GF(256) 对数 / 指数表（本原多项式 0x11D） =====
  private static readonly GF_EXP: number[] = QrCodeEncoder.buildGfExp()
  private static readonly GF_LOG: number[] = QrCodeEncoder.buildGfLog()

  /** 指数表（长度 512，便于乘法下标溢出时回绕） */
  private static buildGfExp(): number[] {
    const exp: number[] = new Array(512).fill(0)
    let x: number = 1
    for (let i = 0; i < 255; i++) {
      exp[i] = x
      x = x << 1
      if (x > 255) {
        x = x ^ 0x11D
      }
    }
    for (let i = 255; i < 512; i++) {
      exp[i] = exp[i - 255]
    }
    return exp
  }

  /** 对数表 */
  private static buildGfLog(): number[] {
    const log: number[] = new Array(256).fill(0)
    for (let i = 0; i < 255; i++) {
      log[QrCodeEncoder.GF_EXP[i]] = i
    }
    return log
  }

  /** GF(256) 乘法 */
  private static gmul(a: number, b: number): number {
    if (a === 0 || b === 0) {
      return 0
    }
    return QrCodeEncoder.GF_EXP[(QrCodeEncoder.GF_LOG[a] + QrCodeEncoder.GF_LOG[b]) % 255]
  }

  /**
   * 编码文本为 QR 矩阵。
   * @param text 待编码文本（按 UTF-8 转字节；ASCII 链接与中文均支持）
   * @returns boolean[][] 模块矩阵，true=黑模块
   * @throws 文本超出版本 5 容量时抛出异常
   */
  static encode(text: string): boolean[][] {
    const dataBytes: number[] = QrCodeEncoder.utf8Bytes(text)
    const len: number = dataBytes.length

    // 选最小可容纳版本
    let version: number = 0
    for (let v = 1; v <= QrCodeEncoder.MAX_VERSION; v++) {
      if (QrCodeEncoder.BYTE_CAP[v] >= len) {
        version = v
        break
      }
    }
    if (version === 0) {
      throw new Error(
        `文本长度 ${len} 字节超出容量上限（版本 ${QrCodeEncoder.MAX_VERSION} 最多 ` +
        `${QrCodeEncoder.BYTE_CAP[QrCodeEncoder.MAX_VERSION]} 字节），请缩短内容或改用后端生成`
      )
    }

    const totalData: number = QrCodeEncoder.TOTAL_DATA_CW[version]
    const ec: number = QrCodeEncoder.EC_CW[version]
    // 字节模式：版本 1~9 字符计数指示符为 8 位，10~40 为 16 位
    const lenBits: number = (version <= 9) ? 8 : 16

    // ===== 1. 构造数据位流 =====
    const bits: number[] = []
    QrCodeEncoder.putBits(bits, 0x4, 4)         // 模式指示符：字节模式 0100
    QrCodeEncoder.putBits(bits, len, lenBits)   // 字符计数指示符
    for (let i = 0; i < dataBytes.length; i++) {
      QrCodeEncoder.putBits(bits, dataBytes[i], 8)
    }
    // 终止符（最多 4 个 0，且不超出容量）
    const capacityBits: number = totalData * 8
    const term: number = Math.min(4, capacityBits - bits.length)
    if (term > 0) {
      QrCodeEncoder.putBits(bits, 0, term)
    }
    // 补齐到字节边界
    while (bits.length % 8 !== 0) {
      bits.push(0)
    }
    // 位流转数据码字
    const dataCw: number[] = []
    for (let i = 0; i < bits.length; i += 8) {
      let b: number = 0
      for (let j = 0; j < 8; j++) {
        b = (b << 1) | (bits[i + j] || 0)
      }
      dataCw.push(b)
    }
    // 填充字节（0xEC / 0x11 交替）
    const pad: number[] = [0xEC, 0x11]
    let pi: number = 0
    while (dataCw.length < totalData) {
      dataCw.push(pad[pi % 2])
      pi++
    }

    // ===== 2. Reed-Solomon 纠错码字 =====
    const ecCw: number[] = QrCodeEncoder.rsEncode(dataCw, ec)
    const allCw: number[] = dataCw.concat(ecCw)

    // ===== 3. 8 种掩码择优（惩罚分最低者胜） =====
    const size: number = 4 * version + 17
    let bestMatrix: boolean[][] = []
    let bestPenalty: number = Number.MAX_SAFE_INTEGER
    for (let mask = 0; mask < 8; mask++) {
      const m: boolean[][] = QrCodeEncoder.buildMatrix(allCw, version, size, mask)
      const p: number = QrCodeEncoder.penalty(m, size)
      if (p < bestPenalty) {
        bestPenalty = p
        bestMatrix = m
      }
    }
    return bestMatrix
  }

  /** 追加 n 位（高位在前） */
  private static putBits(arr: number[], val: number, n: number): void {
    for (let i = n - 1; i >= 0; i--) {
      arr.push((val >> i) & 1)
    }
  }

  /** UTF-8 编码为字节数组（含代理对处理，支持 Emoji） */
  private static utf8Bytes(text: string): number[] {
    const out: number[] = []
    for (let i = 0; i < text.length; i++) {
      let c: number = text.charCodeAt(i)
      // 高代理项：与低代理项合成完整码点
      if (c >= 0xD800 && c <= 0xDBFF && i + 1 < text.length) {
        const next: number = text.charCodeAt(i + 1)
        if (next >= 0xDC00 && next <= 0xDFFF) {
          c = 0x10000 + ((c - 0xD800) << 10) + (next - 0xDC00)
          i++
        }
      }
      if (c < 0x80) {
        out.push(c)
      } else if (c < 0x800) {
        out.push(0xC0 | (c >> 6))
        out.push(0x80 | (c & 0x3F))
      } else if (c < 0x10000) {
        out.push(0xE0 | (c >> 12))
        out.push(0x80 | ((c >> 6) & 0x3F))
        out.push(0x80 | (c & 0x3F))
      } else {
        out.push(0xF0 | (c >> 18))
        out.push(0x80 | ((c >> 12) & 0x3F))
        out.push(0x80 | ((c >> 6) & 0x3F))
        out.push(0x80 | (c & 0x3F))
      }
    }
    return out
  }

  // ===== Reed-Solomon =====

  /** 生成多项式（最高次在前，长度 ec+1，gen[0]=1） */
  private static rsGenPoly(ec: number): number[] {
    let poly: number[] = [1]
    for (let i = 0; i < ec; i++) {
      const mult: number = QrCodeEncoder.GF_EXP[i]
      const next: number[] = new Array(poly.length + 1).fill(0)
      for (let j = 0; j < poly.length; j++) {
        next[j] = next[j] ^ poly[j]
        next[j + 1] = next[j + 1] ^ QrCodeEncoder.gmul(poly[j], mult)
      }
      poly = next
    }
    return poly
  }

  /** 计算纠错码字（长度 = ec） */
  private static rsEncode(data: number[], ec: number): number[] {
    const gen: number[] = QrCodeEncoder.rsGenPoly(ec)
    const res: number[] = new Array(ec).fill(0)
    for (let i = 0; i < data.length; i++) {
      const factor: number = data[i] ^ res[0]
      for (let j = 0; j < ec - 1; j++) {
        res[j] = res[j + 1]
      }
      res[ec - 1] = 0
      if (factor !== 0) {
        for (let j = 0; j < ec; j++) {
          res[j] = res[j] ^ QrCodeEncoder.gmul(gen[j + 1], factor)
        }
      }
    }
    return res
  }

  // ===== 矩阵构建 =====

  /** 构建指定掩码下的完整矩阵 */
  private static buildMatrix(cw: number[], version: number, size: number, mask: number): boolean[][] {
    const m: boolean[][] = []
    for (let i = 0; i < size; i++) {
      const row: boolean[] = []
      for (let j = 0; j < size; j++) {
        row.push(false)
      }
      m.push(row)
    }

    // 三个探测图形（7×7）；分隔白边保持 false 即可
    QrCodeEncoder.drawFinder(m, 0, 0)
    QrCodeEncoder.drawFinder(m, 0, size - 7)
    QrCodeEncoder.drawFinder(m, size - 7, 0)

    // 时序图形（第 6 行 / 第 6 列黑白交替，起点列 8 为黑）
    for (let i = 8; i < size - 8; i++) {
      const v: boolean = (i % 2 === 0)
      m[6][i] = v
      m[i][6] = v
    }

    // 对齐图案
    const pos: number[] = QrCodeEncoder.ALIGN[version]
    for (let a = 0; a < pos.length; a++) {
      for (let b = 0; b < pos.length; b++) {
        const ar: number = pos[a]
        const ac: number = pos[b]
        if (QrCodeEncoder.inFinder(ar, ac, size)) {
          continue
        }
        QrCodeEncoder.drawAlign(m, ar, ac)
      }
    }

    // 格式信息（含当前掩码号）
    QrCodeEncoder.drawFormat(m, size, QrCodeEncoder.formatBits(mask))

    // 数据位填充（含掩码翻转）
    QrCodeEncoder.drawData(m, cw, size, version, mask)

    return m
  }

  /** 绘制 7×7 探测图形 */
  private static drawFinder(m: boolean[][], r: number, c: number): void {
    for (let i = 0; i < 7; i++) {
      for (let j = 0; j < 7; j++) {
        const outer: boolean = (i === 0 || i === 6 || j === 0 || j === 6)
        const inner: boolean = (i >= 2 && i <= 4 && j >= 2 && j <= 4)
        if (outer || inner) {
          m[r + i][c + j] = true
        }
      }
    }
  }

  /** 绘制 5×5 对齐图案 */
  private static drawAlign(m: boolean[][], cr: number, cc: number): void {
    for (let dr = -2; dr <= 2; dr++) {
      for (let dc = -2; dc <= 2; dc++) {
        const ring: boolean = (Math.abs(dr) === 2 || Math.abs(dc) === 2)
        const center: boolean = (dr === 0 && dc === 0)
        if (ring || center) {
          m[cr + dr][cc + dc] = true
        }
      }
    }
  }

  /** 坐标是否落在探测图形（含分隔与格式信息）保留区内 */
  private static inFinder(r: number, c: number, size: number): boolean {
    if (r <= 8 && c <= 8) {
      return true
    }
    if (r <= 8 && c >= size - 8) {
      return true
    }
    if (r >= size - 8 && c <= 8) {
      return true
    }
    return false
  }

  /** 计算 15 位格式信息：5 位数据（纠错级别 L=01 + 3 位掩码）+ 10 位 BCH 校验，再异或掩码 0x5412 */
  private static formatBits(mask: number): number {
    const data: number = (0x1 << 3) | (mask & 0x7)
    let rem: number = data
    for (let i = 0; i < 10; i++) {
      rem = (rem << 1) ^ (((rem >> 9) & 1) * 0x537)
    }
    return ((data << 10) | rem) ^ 0x5412
  }

  /**
   * 写入格式信息（两份副本 + 固定黑模块）。
   *
   * ⚠️ 易错点：bit0~bit5 走的是【第 8 列、行 0~5】（竖直方向），
   *    bit9~bit14 走的是【第 8 行、列 5~0】（水平方向）。
   *    行列若写反（转置），扫码器将完全无法识别。
   */
  private static drawFormat(m: boolean[][], size: number, fmt: number): void {
    const bit = (x: number, i: number): boolean => (((x >> i) & 1) === 1)
    // 副本 1：水平条位于第 8 行（列 0~5、7、8），垂直条位于第 8 列（行 7、5~0）
    for (let i = 0; i <= 5; i++) {
      m[8][i] = bit(fmt, 14 - i)
    }
    m[8][7] = bit(fmt, 14 - 6)
    m[8][8] = bit(fmt, 14 - 7)
    m[7][8] = bit(fmt, 14 - 8)
    for (let i = 9; i < 15; i++) {
      m[14 - i][8] = bit(fmt, 14 - i)
    }
    // 副本 2：行 8 最右侧 8 列（bit0~7）、列 8 最下方 7 行（bit8~14）
    for (let i = 0; i < 8; i++) {
      m[size - 1 - i][8] = bit(fmt, 14 - i)
    }
    for (let i = 8; i < 15; i++) {
      m[8][size - 15 + i] = bit(fmt, 14 - i)
    }
    // 固定黑模块（规范要求恒为黑）
    m[size - 8][8] = true
  }

  /**
   * Z 字形填充数据位（含掩码翻转）。
   *
   * ⚠️ 易错点：每两列为一组，组内两列共用同一垂直方向，
   *    方向仅由列组决定（不能依赖组内偏移 dc），逐组上下交替。
   */
  private static drawData(m: boolean[][], cw: number[], size: number, version: number, mask: number): void {
    let bitIndex: number = 0
    const totalBits: number = cw.length * 8
    for (let col = size - 1; col >= 1; col -= 2) {
      if (col === 6) {
        col = 5   // 跳过时序列 6，该组改用列 5 / 4
      }
      // 列组垂直方向（ISO/IEC 18004 §8.7.3）
      const upward: boolean = (((col + 1) & 2) === 0)
      for (let row = 0; row < size; row++) {
        const r: number = upward ? (size - 1 - row) : row
        for (let dc = 0; dc < 2; dc++) {
          const c: number = col - dc
          if (QrCodeEncoder.isFunctionModule(r, c, size, version)) {
            continue
          }
          let bit: number = 0
          if (bitIndex < totalBits) {
            bit = (cw[bitIndex >> 3] >> (7 - (bitIndex & 7))) & 1
            bitIndex++
          }
          // 掩码条件成立 → 该模块取反
          m[r][c] = QrCodeEncoder.maskCondition(mask, r, c) ? (bit === 0) : (bit === 1)
        }
      }
    }
  }

  /** 判断模块是否为功能图形（不可填数据） */
  private static isFunctionModule(r: number, c: number, size: number, version: number): boolean {
    if (r <= 8 && c <= 8) {
      return true
    }
    if (r <= 8 && c >= size - 8) {
      return true
    }
    if (r >= size - 8 && c <= 8) {
      return true
    }
    if (r === 6 || c === 6) {
      return true
    }
    const pos: number[] = QrCodeEncoder.ALIGN[version]
    for (let a = 0; a < pos.length; a++) {
      for (let b = 0; b < pos.length; b++) {
        const ar: number = pos[a]
        const ac: number = pos[b]
        if (QrCodeEncoder.inFinder(ar, ac, size)) {
          continue
        }
        if (Math.abs(r - ar) <= 2 && Math.abs(c - ac) <= 2) {
          return true
        }
      }
    }
    if (r === size - 8 && c === 8) {
      return true
    }
    return false
  }

  /**
   * 掩码条件（mask 0~7），ISO/IEC 18004 表 10。
   *
   * ⚠️ 易错点：判定式一律为 `=== 0`（条件成立时翻转模块）。
   *    若误写成 `=== 1`，等于整体反转掩码，约半数数据模块出错，扫码必失败。
   */
  private static maskCondition(mask: number, r: number, c: number): boolean {
    switch (mask) {
      case 0:
        return (r + c) % 2 === 0
      case 1:
        return r % 2 === 0
      case 2:
        return c % 3 === 0
      case 3:
        return (r + c) % 3 === 0
      case 4:
        return (Math.floor(r / 2) + Math.floor(c / 3)) % 2 === 0
      case 5:
        return ((r * c) % 2) + ((r * c) % 3) === 0
      case 6:
        return (((r * c) % 2) + ((r * c) % 3)) % 2 === 0
      case 7:
        return (((r + c) % 2) + ((r * c) % 3)) % 2 === 0
      default:
        return false
    }
  }

  // ===== 掩码惩罚评分（4 条规则） =====

  private static penalty(m: boolean[][], size: number): number {
    let p: number = 0

    // 规则 1：行 / 列出现连续 5 个及以上同色模块
    for (let r = 0; r < size; r++) {
      let run: number = 1
      for (let c = 1; c < size; c++) {
        if (m[r][c] === m[r][c - 1]) {
          run++
        } else {
          if (run >= 5) {
            p += 3 + (run - 5)
          }
          run = 1
        }
      }
      if (run >= 5) {
        p += 3 + (run - 5)
      }
    }
    for (let c = 0; c < size; c++) {
      let run: number = 1
      for (let r = 1; r < size; r++) {
        if (m[r][c] === m[r - 1][c]) {
          run++
        } else {
          if (run >= 5) {
            p += 3 + (run - 5)
          }
          run = 1
        }
      }
      if (run >= 5) {
        p += 3 + (run - 5)
      }
    }

    // 规则 2：2×2 同色块
    for (let r = 0; r < size - 1; r++) {
      for (let c = 0; c < size - 1; c++) {
        const v: boolean = m[r][c]
        if (m[r][c + 1] === v && m[r + 1][c] === v && m[r + 1][c + 1] === v) {
          p += 3
        }
      }
    }

    // 规则 3：出现类探测图形序列 1011101 0000（及其反序）
    const patA: number[] = [1, 0, 1, 1, 1, 0, 1, 0, 0, 0, 0]
    const patB: number[] = [0, 0, 0, 0, 1, 0, 1, 1, 1, 0, 1]
    const match = (arr: boolean[], pat: number[]): boolean => {
      for (let k = 0; k < 11; k++) {
        if ((arr[k] ? 1 : 0) !== pat[k]) {
          return false
        }
      }
      return true
    }
    for (let r = 0; r < size; r++) {
      for (let c = 0; c <= size - 11; c++) {
        const rowSlice: boolean[] = []
        const colSlice: boolean[] = []
        for (let k = 0; k < 11; k++) {
          rowSlice.push(m[r][c + k])
          colSlice.push(m[c + k][r])
        }
        if (match(rowSlice, patA) || match(rowSlice, patB)) {
          p += 40
        }
        if (match(colSlice, patA) || match(colSlice, patB)) {
          p += 40
        }
      }
    }

    // 规则 4：黑模块占比偏离 50%
    let dark: number = 0
    for (let r = 0; r < size; r++) {
      for (let c = 0; c < size; c++) {
        if (m[r][c]) {
          dark++
        }
      }
    }
    const ratio: number = (dark * 100) / (size * size)
    p += Math.floor(Math.abs(ratio - 50) / 5) * 10

    return p
  }

  /**
   * 将 QR 矩阵渲染为 SVG Data URL（含 4 模块静区，符合扫码规范）。
   *
   * 注：页面内展示推荐直接用 Canvas 绘制矩阵（见 PostDetail.ets），
   *     本方法适用于需要图片 URL 的场景（如写入剪贴板、生成分享卡）。
   *
   * @param matrix QR 矩阵
   * @param sizePx 目标边长（实际输出按整数模块尺寸对齐，可能略小于该值）
   */
  static matrixToSvgDataUrl(matrix: boolean[][], sizePx: number): string {
    const n: number = matrix.length
    const quiet: number = 4
    const total: number = n + quiet * 2
    const cell: number = Math.max(1, Math.floor(sizePx / total))
    const px: number = total * cell   // viewBox 与 width/height 必须同为像素单位
    let body: string = ''
    // 白色背景（静区 + 内容区），静区为白是扫码前提
    body += `<rect x="0" y="0" width="${px}" height="${px}" fill="#FFFFFF"/>`
    // 黑模块合并为单一 path，显著减少节点数
    let path: string = ''
    for (let i = 0; i < n; i++) {
      for (let j = 0; j < n; j++) {
        if (matrix[i][j]) {
          path += `M${(quiet + j) * cell} ${(quiet + i) * cell}h${cell}v${cell}h-${cell}z`
        }
      }
    }
    body += `<path d="${path}" fill="#000000"/>`
    const svgStr: string =
      `<svg xmlns="http://www.w3.org/2000/svg" width="${px}" height="${px}" ` +
      `viewBox="0 0 ${px} ${px}" shape-rendering="crispEdges">${body}</svg>`
    return `data:image/svg+xml,${encodeURIComponent(svgStr)}`
  }
}

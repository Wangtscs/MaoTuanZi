/**
 * 阴影 / 浮层 token
 * 暖色系场景默认带粉色色染，避免纯黑阴影显得冷硬
 */
export class Shadow {
  // 卡片层级 - 浮在奶油白上
  static readonly SM = { radius: 8,  color: 'rgba(255, 150, 170, 0.06)', offsetY: 2 }
  static readonly MD = { radius: 16, color: 'rgba(255, 150, 170, 0.10)', offsetY: 6 }
  static readonly LG = { radius: 24, color: 'rgba(255, 130, 150, 0.14)', offsetY: 12 }

  // 浮层 - 弹窗 / 底部操作栏
  static readonly FLOAT = { radius: 32, color: 'rgba(0, 0, 0, 0.10)', offsetY: -4 }

  // 顶栏底部投影（页面滚动时）
  static readonly TOPBAR = { radius: 12, color: 'rgba(0, 0, 0, 0.04)', offsetY: 4 }
}

/**
 * 阴影语法糖：在 Column/Row 上直接调用 .shadow(Shadow.MD)
 * 返回符合 ArkUI shadow 接口的对象
 */
export function shadowOf(s: { radius: number, color: string, offsetY: number }): object {
  return { radius: s.radius, color: s.color, offsetX: 0, offsetY: s.offsetY }
}

#!/usr/bin/env python3
"""
QrCodeGenerator.ets 扫码正确性回归工具

直接对真实 ArkTS 源文件做验证（Node 类型剥离运行），不依赖任何复刻实现，
再用 OpenCV 的 QRCodeDetector 真实解码 —— 能解出原文才算通过。

三个阶段：
  A. 矩阵层   : QrCodeEncoder.encode() 产出的矩阵能否被扫出
  B. SVG 层   : matrixToSvgDataUrl() 的 Data URL 渲染后能否被扫出
  C. Canvas 层: PostDetail.ets 的 Canvas 绘制参数在各 DPI 下能否被扫出
  D. 交叉对照 : 与 Python qrcode 库产物比对（可选，缺库自动跳过）

用法:
    python verify_all.py            # 跑全部
    python verify_all.py --stage A  # 只跑某阶段

依赖:
    pip install opencv-python-headless numpy
    pip install qrcode              # 可选，用于 D 阶段交叉对照
"""
import argparse, json, os, subprocess, sys, urllib.parse, re

try:
    import numpy as np
    import cv2
except ImportError:
    sys.exit("缺少依赖，请先执行: pip install opencv-python-headless numpy")

HERE = os.path.dirname(os.path.abspath(__file__))
ETS = os.path.normpath(os.path.join(HERE, "..", "..", "entry", "src", "main", "ets", "utils", "QrCodeGenerator.ets"))
BUILD = os.path.join(HERE, ".build")


def find_node():
    """定位可剥离类型运行 .mts 的 Node：优先 QR_NODE 环境变量，
    其次常见安装路径，最后回退 PATH 中的 node。"""
    import shutil
    candidates = []
    if os.environ.get("QR_NODE"):
        candidates.append(os.environ["QR_NODE"])
    # 系统 Node（本机实测 v22.17）
    candidates.append(r"E:/Environment/nodejs/node.exe")
    # WorkBuddy 托管 Node（版本目录可能随会话变化，glob 兜底）
    mdir = r"C:/Users/25091/.workbuddy/binaries/node/versions"
    if os.path.isdir(mdir):
        for d in sorted(os.listdir(mdir), reverse=True):
            candidates.append(os.path.join(mdir, d, "node.exe"))
    candidates.append("node")
    for c in candidates:
        exe = shutil.which(c) if (not os.path.isabs(c) and "/" not in c) else c
        path = exe or c
        if not os.path.isfile(path):
            continue
        try:
            r = subprocess.run([path, "--experimental-strip-types", "--version"],
                               capture_output=True, text=True, timeout=20)
            if r.returncode == 0 and "v22" in r.stdout + r.stderr:
                return path
        except Exception:
            continue
    return "node"  # 最后兜底，失败时报错由调用方暴露


NODE = find_node()

# 深度链接是本编码器的主场景
DEEP = "maotuanzi://square/post/"

CASES = [
    ("V1-短文本",    "hello"),
    ("V1-边界17",    "a" * 17),
    ("V2-越界18",    "a" * 18),
    ("V2-深链最短",   DEEP + "1"),
    ("V2-深链常规",   DEEP + "123"),
    ("V2-边界32",    "b" * 32),
    ("V3-深链长id",  DEEP + "9007199254740991"),
    ("V3-深链带参",   DEEP + "123?from=share&uid=42"),
    ("V3-边界53",    "c" * 53),
    ("V4-边界78",    "d" * 78),
    ("V5-边界106",   "e" * 106),
    ("中文标题",      "毛团子AI·帖子分享"),
    ("中英混排",      "毛团子 " + DEEP + "999"),
    ("Emoji",       "毛团子 \u2728 AI"),
]

DET = cv2.QRCodeDetector()


# ---------- 探针：把 .ets 包装成可执行 .mts ----------
def make_probe(name, tail):
    os.makedirs(BUILD, exist_ok=True)
    src = open(ETS, encoding="utf-8").read() + "\n" + tail
    path = os.path.join(BUILD, name)
    open(path, "w", encoding="utf-8").write(src)
    return path


def run_probe(path, *args):
    p = subprocess.run([NODE, "--experimental-strip-types", path, *args],
                       capture_output=True, text=True, cwd=HERE, encoding="utf-8")
    return p


def render(matrix, scale=8, quiet=4):
    n = len(matrix)
    side = (n + quiet * 2) * scale
    img = np.full((side, side), 255, dtype=np.uint8)
    for r in range(n):
        for c in range(n):
            if matrix[r][c]:
                img[(r + quiet) * scale:(r + quiet + 1) * scale,
                    (c + quiet) * scale:(c + quiet + 1) * scale] = 0
    return img


def decode(img):
    got, _, _ = DET.detectAndDecode(img)
    return got


# ---------- A. 矩阵层 ----------
def stage_a():
    print("\n[A] 矩阵层 —— QrCodeEncoder.encode()")
    probe = make_probe("_m.mts", """
const t: string = process.argv[2]
const m: boolean[][] = QrCodeEncoder.encode(t)
console.log(JSON.stringify(m.map((r: boolean[]) => r.map((v: boolean) => v ? 1 : 0))))
""")
    print("  %-14s %-6s %-7s %s" % ("CASE", "SIZE", "BYTES", "RESULT"))
    ok = fail = 0
    for name, text in CASES:
        p = run_probe(probe, text)
        if p.returncode != 0:
            print("  %-14s %-6s %-7s ERROR %s" % (name, "-", "-", p.stderr.strip()[:30]))
            fail += 1
            continue
        m = json.loads(p.stdout.strip())
        got = decode(render(m))
        good = got == text
        ok, fail = ok + good, fail + (not good)
        extra = "" if good else "  got=%r" % got[:26]
        print("  %-14s %-6s %-7s %s%s" % (
            name, len(m), len(text.encode()), "PASS" if good else "FAIL", extra))

    # 超容量保护
    p = run_probe(probe, "z" * 200)
    guard = p.returncode != 0
    print("  %-14s %-6s %-7s %s" % ("超容量200B保护", "-", 200, "PASS(已拦截)" if guard else "FAIL(未拦截)"))
    ok, fail = ok + guard, fail + (not guard)
    return ok, fail


# ---------- B. SVG 层 ----------
def stage_b():
    print("\n[B] SVG 层 —— matrixToSvgDataUrl()")
    probe = make_probe("_s.mts", """
const t: string = process.argv[2]
const px: number = parseInt(process.argv[3])
console.log(QrCodeEncoder.matrixToSvgDataUrl(QrCodeEncoder.encode(t), px))
""")
    print("  %-26s %-5s %-8s %s" % ("TEXT", "PX", "SVG-KB", "RESULT"))
    ok = fail = 0
    for text in [DEEP + "123", DEEP + "9007199254740991", "毛团子 " + DEEP + "999"]:
        for px in (200, 320, 480):
            p = run_probe(probe, text, str(px))
            if p.returncode != 0:
                print("  ERROR", p.stderr.strip()[:100]); fail += 1; continue
            durl = p.stdout.strip()
            svg = urllib.parse.unquote(durl.split(",", 1)[1])
            vb = re.search(r'viewBox="0 0 (\d+) (\d+)"', svg)
            w = re.search(r'width="(\d+)"', svg)
            # viewBox 与 width 必须同单位，否则只渲染出局部
            unit_ok = bool(vb and w and vb.group(1) == w.group(1))
            side = int(vb.group(1))
            img = np.full((side, side), 255, dtype=np.uint8)
            for x, y, c in re.findall(r'M(\d+) (\d+)h(\d+)v', svg):
                x, y, c = int(x), int(y), int(c)
                img[y:y + c, x:x + c] = 0
            good = unit_ok and decode(img) == text
            ok, fail = ok + good, fail + (not good)
            tag = "PASS" if good else ("FAIL(单位)" if not unit_ok else "FAIL(解码)")
            print("  %-26s %-5s %-8s %s" % (text[:24], px, "%.1f" % (len(durl) / 1024), tag))
    return ok, fail


# ---------- C. Canvas 层 ----------
def stage_c():
    print("\n[C] Canvas 层 —— PostDetail.ets 绘制参数 @ 各 DPI")
    probe = make_probe("_m.mts", """
const t: string = process.argv[2]
const m: boolean[][] = QrCodeEncoder.encode(t)
console.log(JSON.stringify(m.map((r: boolean[]) => r.map((v: boolean) => v ? 1 : 0))))
""")
    scales = [2.0, 2.5, 3.0, 3.5]
    print("  %-14s %-5s %-7s %s" % ("CASE", "CELL", "SIDEvp", "  ".join("%.1fx" % s for s in scales)))
    ok = fail = 0
    for name, text in [("V1", "hello"), ("V2-深链", DEEP + "123"),
                       ("V3-长深链", DEEP + "9007199254740991"),
                       ("V4", "d" * 78), ("V5", "e" * 106)]:
        m = json.loads(run_probe(probe, text).stdout.strip())
        n, quiet = len(m), 4
        total = n + quiet * 2
        cell = max(3, 120 // total)      # 与 ensureQrCode() 公式保持一致
        res = []
        for s in scales:
            cpx = max(1, round(cell * s))
            side = total * cpx
            img = np.full((side, side), 255, dtype=np.uint8)
            for r in range(n):
                for c in range(n):
                    if m[r][c]:
                        img[(quiet + r) * cpx:(quiet + r + 1) * cpx,
                            (quiet + c) * cpx:(quiet + c + 1) * cpx] = 0
            good = decode(img) == text
            ok, fail = ok + good, fail + (not good)
            res.append(" OK " if good else " X  ")
        print("  %-14s %-5s %-7s %s" % (name, cell, total * cell, "  ".join(res)))
    return ok, fail


# ---------- D. 与 qrcode 库交叉对照 ----------
def stage_d():
    print("\n[D] 交叉对照 —— vs Python qrcode 库")
    try:
        import qrcode
    except ImportError:
        print("  跳过（未安装 qrcode 库）")
        return 0, 0
    probe = make_probe("_m.mts", """
const t: string = process.argv[2]
const m: boolean[][] = QrCodeEncoder.encode(t)
console.log(JSON.stringify(m.map((r: boolean[]) => r.map((v: boolean) => v ? 1 : 0))))
""")
    ok = fail = 0
    print("  %-26s %-10s %-10s %s" % ("TEXT", "MINE-SIZE", "REF-SIZE", "RESULT"))
    for text in [DEEP + "1", DEEP + "123", "c" * 53, "e" * 106]:
        mine = json.loads(run_probe(probe, text).stdout.strip())
        q = qrcode.QRCode(error_correction=qrcode.constants.ERROR_CORRECT_L, border=4)
        q.add_data(text, optimize=0)
        q.make(fit=True)
        ref = [[1 if v else 0 for v in row] for row in q.modules]
        # 版本必须一致（说明容量表正确）；掩码可不同，故不比对逐位
        good = len(mine) == len(ref) and decode(render(mine)) == text
        ok, fail = ok + good, fail + (not good)
        print("  %-26s %-10s %-10s %s" % (
            text[:24], len(mine), len(ref), "PASS" if good else "FAIL(版本不一致)"))
    return ok, fail


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--stage", choices=list("ABCD"), help="只跑指定阶段")
    args = ap.parse_args()

    if not os.path.exists(ETS):
        sys.exit("找不到源文件: %s" % ETS)
    print("源文件:", ETS)

    stages = {"A": stage_a, "B": stage_b, "C": stage_c, "D": stage_d}
    todo = [args.stage] if args.stage else list("ABCD")
    tot_ok = tot_fail = 0
    for s in todo:
        o, f = stages[s]()
        tot_ok += o
        tot_fail += f

    print("\n" + "=" * 60)
    print("汇总: %d PASS / %d FAIL" % (tot_ok, tot_fail))
    print("=" * 60)
    return 0 if tot_fail == 0 else 1


if __name__ == "__main__":
    sys.exit(main())

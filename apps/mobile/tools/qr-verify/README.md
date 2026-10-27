# QR 码扫描正确性回归工具

用于验证 `entry/src/main/ets/utils/QrCodeGenerator.ets`（纯手写 QR 编码器）的产物**真的能被扫码器识别**。

## 为什么需要它

QR 编码涉及 Reed-Solomon 纠错、格式信息 BCH、掩码择优、Z 字形位填充等多个环节。
这些环节出错时，**生成的图形看起来完全正常**（三个定位角、对齐图案都在），但扫码器解不出任何内容。
肉眼和单元测试都无法发现，只有真实解码才能判定。

本工具直接读取 ArkTS 源文件（通过 Node 的类型剥离运行），再用 OpenCV 的
`QRCodeDetector` 解码 —— **能解出原文才算通过**，不依赖任何复刻实现。

## 使用

```bash
# 安装依赖
pip install opencv-python-headless numpy
pip install qrcode          # 可选，用于 D 阶段交叉对照

# 全量回归
python verify_all.py

# 只跑某阶段
python verify_all.py --stage A

# 指定 node 可执行文件（默认取 PATH 里的 node，需 >= 22）
QR_NODE=/path/to/node python verify_all.py
```

## 四个阶段

| 阶段 | 验证对象 | 说明 |
|---|---|---|
| A | `QrCodeEncoder.encode()` | 矩阵层。覆盖 V1~V5 各版本边界长度、中文、Emoji、超容量保护，共 15 项 |
| B | `matrixToSvgDataUrl()` | SVG 层。解析 Data URL 中的 path 重建位图后解码，并校验 `viewBox` 与 `width` 单位一致 |
| C | `PostDetail.ets` 绘制参数 | Canvas 层。按 2.0x~3.5x 屏幕密度模拟真机像素，确认小尺寸下仍可扫 |
| D | vs Python `qrcode` 库 | 交叉对照版本选择是否一致（验证容量表正确） |

## 修改编码器后必须回归

`QrCodeGenerator.ets` 里有三个极易写错、且错了不报错的地方，已在源码中用 `⚠️ 易错点` 标注：

1. **格式信息的行列方向** —— `bit0~bit5` 在第 8 **列**（竖直），`bit9~bit14` 在第 8 **行**（水平）。写成转置后完全无法识别。
2. **数据填充的列组方向** —— 每两列一组，组内两列**共用**同一垂直方向（`((col+1) & 2) === 0`），不能依赖组内偏移 `dc`。
3. **掩码判定极性** —— 一律 `=== 0`（条件成立时翻转）。写成 `=== 1` 等于整体反转掩码，约半数模块出错。

## 说明

- `.build/` 是运行时生成的临时探针目录，可安全删除。
- 本工具不修改任何源文件，只读取。

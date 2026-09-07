/**
 * 3D 模型查看器（数据链路已打通：任务 → OSS glTF URL → 本组件）
 *
 * ⚠️ 诚实实现说明：
 * API 12 原生真 3D（XComponent + C++ OpenGL / 自研 glTF 解析）超出本次前端范围，
 * 故本组件采用务实方案 —— 以「封面图/海报图预览 + 加载态」呈现模型资产，
 * 并把真实 GPU 渲染的接入点用清晰 TODO 注释标出，保证可构建、不报错。
 *
 * 未来替换路径（任选其一）：
 *  ① 三方开源 glTF 解析器（ArkTS）+ 自绘；
 *  ② XComponent + C++ OpenGL / Vulkan 原生引擎渲染 glTF；
 *  ③ 华为图形能力（如 SceneView / 3D 引擎组件）加载远端 glTF。
 * 上述任一方案就绪后，在下方 TODO 处替换为真实渲染视图即可，上层页面无需改动。
 */
import { Colors, FontSizes, Radius } from "../common/Constants";
import { GradientPlaceholder } from "./CommonComponents";
import { SparkleIcon } from "./icons/Icons";

@Component
export struct Model3DViewer {
  /** glTF 模型地址（后端 OSS 返回，如 https://oss.../*.gltf） */
  @Prop modelUrl: string = ""
  /** 可选封面/海报图（用于加载态与预览） */
  @Prop coverUrl: string = ""
  /** 视图高度（vp） */
  @Prop height: number = 280

  build() {
    Stack({ alignContent: Alignment.Center }) {
      // ============================================================
      // TODO(真实3D渲染接入点)：在此处将下方「封面/占位」替换为真实 GPU 渲染视图。
      // 方案示例（XComponent + 原生引擎）：
      //   XComponent({ id: 'model3d', type: XComponentType.SURFACE, libraryname: 'libmodel3d.so' })
      //     .onLoad((context) => { /* 通过 native API 传入 this.modelUrl 加载 glTF */ })
      //   ★ 当前工程未集成 3D 引擎与原生 so，故先以封面预览兜底，保证可构建可演示。
      // ============================================================
      if (this.coverUrl) {
        Image(this.coverUrl)
          .width("100%")
          .height(this.height)
          .objectFit(ImageFit.Cover)
          .borderRadius(Radius.LG)
      } else {
        GradientPlaceholder({
          label: "3D 模型",
          fromColor: "#E0C3FC",
          toColor: "#C8B6FF",
          height: this.height,
          radius: Radius.LG
        })
      }

      // 加载态遮罩（glTF 真实渲染接入前始终展示为"加载中"占位提示）
      Column() {
        SparkleIcon({ size: 28, color: "#FFFFFF" })
          .margin({ bottom: 8 })
        Text(this.modelUrl ? "3D 模型已生成" : "3D 模型生成中")
          .fontSize(FontSizes.BODY_S)
          .fontColor("#FFFFFF")
          .fontWeight(FontWeight.Medium)
        Text("真实 GPU 渲染引擎接入中，当前为预览态")
          .fontSize(FontSizes.TINY)
          .fontColor("rgba(255,255,255,0.82)")
          .margin({ top: 4 })
      }
      .width("100%")
      .height(this.height)
      .borderRadius(Radius.LG)
      .backgroundColor("rgba(0,0,0,0.28)")
      .justifyContent(FlexAlign.Center)
      .alignItems(HorizontalAlign.Center)
    }
    .width("100%")
    .height(this.height)
    .borderRadius(Radius.LG)
    .clip(true)
  }
}

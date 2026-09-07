/**
 * 3D 模型预览页（真实数据链路：3D 任务 → OSS glTF URL → Model3DViewer）
 *
 * 阶段3（3D 真实渲染）：
 *  - 去掉 isLocked 写死 true 与纯旋转渐变块；
 *  - 支持两种进入方式：传入 taskId（轮询 /v1/ai/3d/tasks/{id}）或直接传入 modelUrl；
 *  - 成功后用 components/Model3DViewer 展示 modelUrl（封面/加载态，真实 GPU 渲染接入点见组件内 TODO）。
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing, Radius } from "../../common/Constants";
import { TopBar, PrimaryButton } from "../../components/CommonComponents";
import { Model3DViewer } from "../../components/Model3DViewer";
import { ApiService } from "../../service/ApiService";

@Entry
@Component
struct Model3DPage {
  @State taskId: string = ""
  @State modelUrl: string = ""
  @State coverUrl: string = ""
  @State status: string = "PENDING"
  @State loading: boolean = false
  @State failed: boolean = false
  private timer: number = -1
  private startTime: number = 0
  private readonly TIMEOUT_MS: number = 120000   // 120s 超时

  aboutToAppear() {
    const params = router.getParams() as Record<string, Object>
    this.taskId = (params?.taskId as string) || ""
    this.modelUrl = (params?.modelUrl as string) || ""
    this.coverUrl = (params?.coverUrl as string) || ""

    if (this.taskId) {
      this.startTime = new Date().getTime()
      this.status = "PENDING"
      this.startPolling()
    } else if (this.modelUrl) {
      this.status = "SUCCESS"
    } else {
      this.loading = false
    }
  }

  aboutToDisappear() {
    if (this.timer > 0) {
      clearInterval(this.timer)
      this.timer = -1
    }
  }

  /** 轮询 3D 任务状态 */
  startPolling() {
    this.timer = setInterval(async () => {
      if (new Date().getTime() - this.startTime > this.TIMEOUT_MS) {
        this.status = "FAILED"
        this.failed = true
        if (this.timer > 0) clearInterval(this.timer)
        promptAction.showToast({ message: "3D 生成超时，请稍后重试", duration: 2000 })
        return
      }
      try {
        const task: any = await ApiService.getModel3dTask(this.taskId)
        if (task) {
          const st: string = (task.status as string) || this.status
          this.status = st
          if (task.modelUrl) {
            this.modelUrl = task.modelUrl as string
          }
          if (task.coverUrl) {
            this.coverUrl = task.coverUrl as string
          }

          if (this.isDone(st)) {
            if (this.timer > 0) clearInterval(this.timer)
            promptAction.showToast({ message: "3D 模型生成完成", duration: 1500 })
          } else if (this.isFailed(st)) {
            this.failed = true
            if (this.timer > 0) clearInterval(this.timer)
            promptAction.showToast({ message: "3D 生成失败，请重新尝试", duration: 2000 })
          }
        }
      } catch (e) {
        // 网络抖动不影响轮询
      }
    }, 2000)
  }

  isDone(s: string): boolean {
    const v: string = s.toUpperCase()
    return v === "SUCCESS" || v === "COMPLETED" || v === "DONE"
  }

  isFailed(s: string): boolean {
    const v: string = s.toUpperCase()
    return v === "FAILED" || v === "ERROR"
  }

  statusText(): string {
    if (this.isFailed(this.status)) return "生成失败"
    if (this.isDone(this.status)) return "生成完成"
    if (this.status === "PROCESSING" || this.status === "RUNNING") return "正在生成 3D 模型..."
    return "任务排队中..."
  }

  build() {
    Column() {
      TopBar({ title: "3D 模型预览", showBack: true, onBack: () => router.back() })

      if (this.modelUrl || this.coverUrl) {
        Scroll() {
          Column() {
            Model3DViewer({ modelUrl: this.modelUrl, coverUrl: this.coverUrl, height: 300 })
              .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.L })

            // 状态文案
            Column() {
              Text(this.statusText())
                .fontSize(FontSizes.BODY)
                .fontWeight(FontWeight.Medium)
                .fontColor(this.isFailed(this.status) ? "#FF4D4F" : Colors.TEXT_PRIMARY)
                .width("100%")
              if (this.modelUrl) {
                Text(`模型地址：${this.modelUrl}`)
                  .fontSize(FontSizes.TINY)
                  .fontColor(Colors.TEXT_TERTIARY)
                  .maxLines(2)
                  .textOverflow({ overflow: TextOverflow.Ellipsis })
                  .margin({ top: Spacing.S })
                  .width("100%")
              }
              Text("真实 GPU 渲染引擎接入中，当前为封面预览态")
                .fontSize(FontSizes.CAPTION)
                .fontColor(Colors.TEXT_SECONDARY)
                .margin({ top: Spacing.S })
                .width("100%")
            }
            .padding(Spacing.L)
            .backgroundColor(Colors.BG_CARD)
            .borderRadius(Radius.LG)
            .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.M })
            .alignItems(HorizontalAlign.Start)

            PrimaryButton({ text: "重新生成", block: true })
              .onClick(() => router.replaceUrl({ url: "pages/ai/Upload", params: { mode: "3d" } }))
              .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.L, bottom: Spacing.XXL })
          }
          .width("100%")
        }
        .layoutWeight(1)
        .scrollBar(BarState.Off)
      } else if (this.taskId && !this.failed) {
        // 任务进行中（尚未返回模型地址）
        Column() {
          LoadingProgress()
            .width(44)
            .height(44)
            .color(Colors.PRIMARY)
            .margin({ top: Spacing.XXL })
          Text(this.statusText())
            .fontSize(FontSizes.BODY)
            .fontColor(Colors.TEXT_SECONDARY)
            .margin({ top: Spacing.M })
        }
        .width("100%")
        .layoutWeight(1)
        .alignItems(HorizontalAlign.Center)
      } else {
        // 无任务、无模型：引导进入 3D 专属生成流程
        Column() {
          Text("还没有 3D 模型")
            .fontSize(FontSizes.H2)
            .fontWeight(FontWeight.Medium)
            .fontColor(Colors.TEXT_PRIMARY)
            .margin({ top: Spacing.XXL })
          Text("上传宠物照片，生成可旋转的 3D 数字手办")
            .fontSize(FontSizes.BODY_S)
            .fontColor(Colors.TEXT_SECONDARY)
            .margin({ top: Spacing.S })
          PrimaryButton({ text: "去生成 3D", block: false })
            .onClick(() => router.replaceUrl({ url: "pages/ai/Upload", params: { mode: "3d" } }))
            .margin({ top: Spacing.L })
        }
        .width("100%")
        .layoutWeight(1)
        .alignItems(HorizontalAlign.Center)
      }
    }
    .width("100%")
    .height("100%")
    .backgroundColor(Colors.BG_PAGE)
  }
}

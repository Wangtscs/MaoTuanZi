/**
 * 风格选择页（支持 2D 画像 / 3D 手办两种模式）
 *
 * 阶段3（3D 真实渲染链路）：
 *  - mode="3d" 时读取 3D 风格（后端 /v1/ai/styles 若无 3d 分类则降级硬编码），
 *    提交时先上传图片得到 sourceImageUrl，再 POST /v1/ai/3d/tasks，
 *    并跳转 pages/ai/Model3D（taskId）进入 3D 专属轮询流程。
 *  - mode="2d" 时保持原有 2D 生成流程不变。
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing, Radius } from "../../common/Constants";
import { TopBar, PriceTag, Tag } from "../../components/CommonComponents";
import { ApiService } from "../../service/ApiService";

// 3D 风格降级数据（后端 /v1/ai/styles?category=portrait_3d 不可用时兜底）
interface ThreeDStyle {
  id: string;
  name: string;
  description: string;
  code: string;
}

const THREE_D_STYLES: ThreeDStyle[] = [
  { id: "portrait_3d_fast", name: "极速 3D", description: "快速生成手办", code: "portrait_3d_fast" },
  { id: "portrait_3d_hd", name: "高清 3D", description: "高清精模", code: "portrait_3d_hd" }
]

@Entry
@Component
struct StyleSelectPage {
  @State styles: any[] = []
  @State selectedStyle: number = -1       // 2D：数字 styleId
  @State selectedStyle3d: string = ""     // 3D：风格 code 字符串
  @State loading: boolean = false
  @State mode: string = "2d"
  private imageUrl: string = ""
  private petId: number = 0

  aboutToAppear() {
    const params = router.getParams() as Record<string, Object>
    this.imageUrl = (params?.imageUrl as string) || ""
    this.petId = (params?.petId as number) || 0
    this.mode = (params?.mode as string) || "2d"
    // 无图片数据时提示用户重新上传
    if (!this.imageUrl || !this.petId) {
      promptAction.showToast({ message: "请先上传照片", duration: 1500 })
      setTimeout(() => router.replaceUrl({ url: "pages/ai/Upload", params: { mode: this.mode } }), 1500)
      return
    }
    this.loadStyles()
  }

  async loadStyles() {
    this.loading = true
    try {
      if (this.mode === "3d") {
        // 优先读后端 3D 风格，失败降级硬编码
        try {
          const data: any = await ApiService.listStyles("portrait_3d")
          if (Array.isArray(data) && data.length > 0) {
            this.styles = data
          } else {
            this.styles = THREE_D_STYLES
          }
        } catch (e) {
          this.styles = THREE_D_STYLES
        }
        if (this.styles.length > 0) {
          this.selectedStyle3d = String(this.styles[0].id)
        }
      } else {
        const data: any = await ApiService.listStyles("portrait_2d")
        this.styles = data || []
        if (this.styles.length > 0) {
          this.selectedStyle = this.styles[0].id as number
        }
      }
    } catch (e: any) {
      promptAction.showToast({ message: "加载风格失败，请重试", duration: 1500 })
      if (this.mode === "3d") {
        this.styles = THREE_D_STYLES
        this.selectedStyle3d = THREE_D_STYLES[0].id
      }
    } finally {
      this.loading = false
    }
  }

  /** 当前选中态（2D 用数字 id，3D 用字符串 code） */
  isSelected(style: any): boolean {
    if (this.mode === "3d") {
      return this.selectedStyle3d === String(style.id)
    }
    return this.selectedStyle === (style.id as number)
  }

  /** 选中风格 */
  selectStyle(style: any) {
    if (this.mode === "3d") {
      this.selectedStyle3d = String(style.id)
    } else {
      this.selectedStyle = style.id as number
    }
  }

  build() {
    Column() {
      TopBar({ title: this.mode === "3d" ? "选择 3D 风格" : "选择风格", showBack: true, onBack: () => router.back() })

      Scroll() {
        Column() {
          Text(this.mode === "3d" ? "为「毛孩子」挑选 3D 手办风格" : "为「毛孩子」挑选心仪的风格吧")
            .fontSize(FontSizes.BODY_S)
            .fontColor(Colors.TEXT_SECONDARY)
            .width("100%")
            .margin({ top: Spacing.M, bottom: Spacing.M, left: Spacing.L })

          Grid() {
            ForEach(this.styles, (style: any) => {
              GridItem() {
                Column() {
                  Column() {
                    Text(style.name.charAt(0) || "·")
                      .fontSize(28)
                      .fontWeight(FontWeight.Bold)
                      .fontColor("#FFFFFF")
                  }
                  .width("100%")
                  .aspectRatio(1)
                  .linearGradient({
                    angle: 135,
                    colors: this.getGradient(style.code)
                  })
                  .borderRadius(Radius.MD)
                  .justifyContent(FlexAlign.Center)
                  .alignItems(HorizontalAlign.Center)
                  .border(this.isSelected(style) ? { width: 3, color: Colors.PRIMARY } : { width: 0 })

                  Text(style.name)
                    .fontSize(FontSizes.BODY_S)
                    .fontWeight(FontWeight.Medium)
                    .fontColor(Colors.TEXT_PRIMARY)
                    .margin({ top: Spacing.S })
                  Text(style.description || "")
                    .fontSize(FontSizes.TINY)
                    .fontColor(Colors.TEXT_TERTIARY)
                    .margin({ top: 2 })
                    .maxLines(1)
                    .textOverflow({ overflow: TextOverflow.Ellipsis })

                  Row() {
                    if (style.isVipOnly === 1) {
                      Tag({ text: "会员", type: "warning", size: "small" })
                        .margin({ right: 4 })
                    }
                    PriceTag({ cents: style.priceCents || 0 })
                  }
                  .width("100%")
                  .margin({ top: 4 })
                  .justifyContent(FlexAlign.Center)
                }
                .width("100%")
                .padding(8)
                .onClick(() => this.selectStyle(style))
              }
            })
          }
          .columnsTemplate("1fr 1fr 1fr")
          .columnsGap(Spacing.S)
          .rowsGap(Spacing.M)
          .padding({ left: Spacing.L, right: Spacing.L })

          Button("开始生成")
            .width("calc(100% - 32vp)")
            .height(48)
            .fontSize(FontSizes.BODY)
            .fontColor(Colors.TEXT_WHITE)
            .backgroundColor(Colors.PRIMARY)
            .borderRadius(Radius.MD)
            .margin({ top: Spacing.L, bottom: Spacing.XL, left: Spacing.L, right: Spacing.L })
            .onClick(() => this.onSubmit())
        }
        .width("100%")
      }
      .layoutWeight(1)
    }
    .width("100%")
    .height("100%")
    .backgroundColor(Colors.BG_PAGE)
  }

  getGradient(code: string): any[] {
    const map: Record<string, any[]> = {
      "oil_painting": [[0xFFB6C1], [0xFFE4B5]],
      "disney": [[0xFFAA80], [0xFFE4E1]],
      "watercolor": [[0xB5EAD7], [0xFFE4E1]],
      "cyberpunk": [[0x9D7BFF], [0xFFD93D]],
      "chinese_ink": [[0xC8B6FF], [0xFFE4E1]],
      "anime": [[0xFFD93D], [0xFFC8D0]],
      "portrait_3d_fast": [[0xC8B6FF], [0xFFAA80]],
      "portrait_3d_hd": [[0x9D7BFF], [0xFFD93D]]
    }
    return map[code] || [[0xFFE4E1], [0xFFE4B5]]
  }

  /** 提交生成：3D 走 /v1/ai/3d/tasks，2D 走 /v1/ai/tasks */
  async onSubmit() {
    if (this.mode === "3d") {
      await this.submit3d()
    } else {
      await this.submit2d()
    }
  }

  async submit2d() {
    if (this.selectedStyle < 0 || this.petId <= 0) {
      promptAction.showToast({ message: "请选择风格", duration: 1500 })
      return
    }
    this.loading = true
    try {
      const task: any = await ApiService.createAiTask({
        taskType: "portrait_2d",
        styleId: this.selectedStyle,
        petId: this.petId,
        inputImageUrl: this.imageUrl
      })
      router.pushUrl({ url: "pages/ai/Generating", params: { taskId: task.id } })
    } catch (e: any) {
      promptAction.showToast({ message: e.message || "提交失败", duration: 1500 })
    } finally {
      this.loading = false
    }
  }

  async submit3d() {
    if (!this.selectedStyle3d || this.petId <= 0) {
      promptAction.showToast({ message: "请选择 3D 风格", duration: 1500 })
      return
    }
    this.loading = true
    try {
      // 契约要求 sourceImageUrl（远端 URL）：先上传本地图片，失败则降级用本地 uri
      let sourceImageUrl: string = this.imageUrl
      try {
        const up: any = await ApiService.uploadImage(this.imageUrl, "pet3d.jpg")
        const remote: string = (up?.url as string) || (up?.ossUrl as string) || (typeof up === "string" ? up : "")
        if (remote) {
          sourceImageUrl = remote
        }
      } catch (e) {
        // 上传失败降级用本地 uri（后端支持时再校验）
      }

      const task: any = await ApiService.createModel3dTask({
        sourceImageUrl: sourceImageUrl,
        style: this.selectedStyle3d
      })
      router.replaceUrl({
        url: "pages/ai/Model3D",
        params: { taskId: String(task?.taskId || ""), coverUrl: this.imageUrl }
      })
    } catch (e: any) {
      promptAction.showToast({ message: e.message || "3D 任务提交失败", duration: 1500 })
    } finally {
      this.loading = false
    }
  }
}

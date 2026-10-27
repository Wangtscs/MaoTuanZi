/**
 * 内嵌网页（隐私政策 / 用户协议）
 * 真实场景：接入 Web 组件加载真实 URL
 */
import router from "@ohos.router";
import webview from "@ohos.web.webview";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing } from "../../common/Constants";
import { TopBar } from "../../components/CommonComponents";

@Entry
@Component
struct WebViewPage {
  @State loading: boolean = false
  @State loadError: boolean = false
  @State webTitle: string = ""
  private controller: WebviewController = new webview.WebviewController()
  // 隐私政策 / 服务协议 的真实 URL（上线后替换为真实地址）
  private readonly URLS: Record<string, string> = {
    privacy: "https://api.maotuanzi.ai/docs/privacy",
    service: "https://api.maotuanzi.ai/docs/service"
  }

  aboutToAppear() {
    const params = router.getParams() as Record<string, Object>
    const type = (params?.type as string) || "privacy"
    this.webTitle = type === "privacy" ? "隐私政策" : "用户服务协议"
    this.loadUrl(type)
  }

  loadUrl(type: string) {
    const url = this.URLS[type] || this.URLS["privacy"]
    this.loading = true
    this.loadError = false
    // 真实 Web 加载
    try {
      this.controller.loadUrl(url)
    } catch (e) {
      this.loading = false
      this.loadError = true
    }
  }

  build() {
    Column() {
      TopBar({ title: this.webTitle, showBack: true, onBack: () => router.back() })

      if (this.loadError) {
        // 加载失败态
        Column() {
          Text("页面加载失败")
            .fontSize(FontSizes.H2)
            .fontColor(Colors.TEXT_PRIMARY)
            .margin({ bottom: Spacing.S })
          Text("请检查网络后重试")
            .fontSize(FontSizes.BODY_S)
            .fontColor(Colors.TEXT_SECONDARY)
          Button("重新加载")
            .fontSize(FontSizes.BODY_S)
            .fontColor(Colors.PRIMARY)
            .backgroundColor("rgba(255,182,193,0.1)")
            .borderRadius(8)
            .padding({ left: Spacing.L, right: Spacing.L, top: 8, bottom: 8 })
            .margin({ top: Spacing.L })
            .onClick(() => this.loadUrl("privacy"))
        }
        .width("100%")
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)
      } else {
        // 真实 Web 组件
        Stack() {
          Web({ src: "", controller: this.controller })
            .width("100%")
            .layoutWeight(1)
            .onPageBegin(() => { this.loading = true })
            .onPageEnd(() => { this.loading = false })
            .onErrorReceive((event) => {
              this.loading = false
              this.loadError = true
            })
            .onTitleReceive((event) => {
              if (event?.title) {
                this.webTitle = event.title
              }
            })

          // 顶部加载进度条
          if (this.loading) {
            Column()
              .width("100%")
              .height(2)
              .backgroundColor(Colors.PRIMARY)
              .position({ y: 0 })
          }
        }
        .width("100%")
        .layoutWeight(1)
      }
    }
    .width("100%")
    .height("100%")
    .backgroundColor(Colors.BG_CARD)
  }
}

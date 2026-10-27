/**
 * 发布帖子
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing, Radius } from "../../common/Constants";
import { TopBar } from "../../components/CommonComponents";
import { ApiService } from "../../service/ApiService";

@Entry
@Component
struct PostCreatePage {
  @State content: string = ""
  @State submitting: boolean = false

  build() {
    Column() {
      TopBar({
        title: "发布",
        showBack: true,
        onBack: () => router.back()
      })

      Column() {
        TextInput({ placeholder: "分享你的毛孩子吧...", text: this.content })
          .width("100%")
          .height(200)
          .fontSize(FontSizes.BODY)
          .fontColor(Colors.TEXT_PRIMARY)
          .backgroundColor(Colors.BG_CARD)
          .borderRadius(Radius.MD)
          .padding(Spacing.M)
          .textAlign(TextAlign.Start)
          .onChange((v: string) => { this.content = v })

        Text("最多500字")
          .fontSize(FontSizes.CAPTION)
          .fontColor(Colors.TEXT_TERTIARY)
          .margin({ top: Spacing.S })
          .alignSelf(HorizontalAlign.End)
      }
      .padding(Spacing.L)
      .layoutWeight(1)

      Button(this.submitting ? "发布中..." : "发布")
        .width("calc(100% - 32vp)")
        .height(48)
        .fontSize(FontSizes.BODY)
        .fontColor(Colors.TEXT_WHITE)
        .backgroundColor(Colors.PRIMARY)
        .borderRadius(Radius.MD)
        .margin({ left: Spacing.L, right: Spacing.L, bottom: Spacing.L })
        .enabled(!this.submitting && this.content.trim().length > 0)
        .onClick(() => this.onPublish())
    }
    .width("100%")
    .height("100%")
    .backgroundColor(Colors.BG_PAGE)
  }

  async onPublish() {
    if (!this.content.trim()) {
      promptAction.showToast({ message: "请输入内容", duration: 1500 })
      return
    }
    this.submitting = true
    try {
      await ApiService.publishPost({ content: this.content.trim() })
      promptAction.showToast({ message: "发布成功", duration: 1200 })
      setTimeout(() => router.back(), 1200)
    } catch (e: any) {
      promptAction.showToast({ message: e.message || "发布失败", duration: 1500 })
    } finally {
      this.submitting = false
    }
  }
}

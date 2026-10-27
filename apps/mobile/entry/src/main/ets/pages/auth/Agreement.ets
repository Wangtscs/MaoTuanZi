/**
 * 协议同意页（首次启动）
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing, Radius } from "../../common/Constants";
import { PrimaryButton } from "../../components/CommonComponents";
import { PawIcon } from "../../components/icons/Icons";

@Entry
@Component
struct AgreementPage {
  @State agreed: boolean = false;

  build() {
    Stack() {
      Column() {
        Column() {
          PawIcon({ size: 64, color: Colors.PRIMARY })
            .margin({ top: 80, bottom: 24 })
          Text("欢迎来到毛团子AI")
            .fontSize(24)
            .fontWeight(FontWeight.Bold)
            .fontColor(Colors.TEXT_PRIMARY)
            .margin({ bottom: 12 })
          Text("用 AI 让每一只毛孩子被温柔以待")
            .fontSize(FontSizes.BODY_S)
            .fontColor(Colors.TEXT_SECONDARY)
        }
        .width("100%")
        .layoutWeight(1)
        .justifyContent(FlexAlign.Start)
        .alignItems(HorizontalAlign.Center)

        Column() {
          Text("服务协议与隐私政策")
            .fontSize(FontSizes.H2)
            .fontWeight(FontWeight.Medium)
            .fontColor(Colors.TEXT_PRIMARY)
            .margin({ bottom: Spacing.M })

          Text("请你务必审慎阅读、充分理解各条款内容，特别是关于个人信息收集、使用和保护的条款。\n\n毛团子AI 是一款面向宠物主的 AI 创作工具，在使用前我们需要你的同意。")
            .fontSize(FontSizes.BODY_S)
            .fontColor(Colors.TEXT_SECONDARY)
            .lineHeight(22)
            .margin({ bottom: Spacing.L })

          Row() {
            Checkbox({ name: "agree", group: "agreement" })
              .select(this.agreed)
              .selectedColor(Colors.PRIMARY)
              .onChange((value: boolean) => { this.agreed = value })
            Text() {
              Span("我已阅读并同意 ").fontSize(FontSizes.BODY_S).fontColor(Colors.TEXT_PRIMARY)
              Span("《用户服务协议》").fontSize(FontSizes.BODY_S).fontColor(Colors.PRIMARY).onClick(() => this.openDoc("service"))
              Span(" 和 ").fontSize(FontSizes.BODY_S).fontColor(Colors.TEXT_PRIMARY)
              Span("《隐私政策》").fontSize(FontSizes.BODY_S).fontColor(Colors.PRIMARY).onClick(() => this.openDoc("privacy"))
            }
          }
          .width("100%")
          .margin({ bottom: Spacing.L })

          PrimaryButton({ text: "同意并继续", disabled: !this.agreed, block: true })
            .onClick(() => this.onAgree())

          Text("如未同意，将无法使用毛团子AI 的相关服务")
            .fontSize(FontSizes.CAPTION)
            .fontColor(Colors.TEXT_TERTIARY)
            .textAlign(TextAlign.Center)
            .margin({ top: Spacing.L })
        }
        .padding(Spacing.XL)
        .backgroundColor(Colors.BG_CARD)
        .borderRadius(Radius.LG)
        .margin({ left: Spacing.L, right: Spacing.L, bottom: Spacing.XL })
      }
      .width("100%")
      .height("100%")
      .backgroundColor(Colors.BG_PAGE)
    }
  }

  async onAgree() {
    if (!this.agreed) {
      promptAction.showToast({ message: "请先勾选同意协议", duration: 1500 })
      return
    }
    router.replaceUrl({ url: "pages/auth/Login" })
  }

  openDoc(type: string) {
    const title = type === "privacy" ? "隐私政策" : "用户服务协议"
    router.pushUrl({ url: "pages/common/WebView", params: { type, title } })
  }
}
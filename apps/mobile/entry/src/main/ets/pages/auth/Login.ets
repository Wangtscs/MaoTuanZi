/**
 * 登录页（微信一键登录模拟）
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing, Radius } from "../../common/Constants";
import { ApiService } from "../../service/ApiService";
import { HttpClient } from "../../service/HttpClient";
import { PawIcon } from "../../components/icons/Icons";

@Entry
@Component
struct LoginPage {
  @State loading: boolean = false;

  build() {
    Stack() {
      Column() {
        Column() {
          PawIcon({ size: 64, color: Colors.PRIMARY })
            .margin({ top: 120, bottom: 24 })
          Text("毛团子AI")
            .fontSize(28)
            .fontWeight(FontWeight.Bold)
            .fontColor(Colors.TEXT_PRIMARY)
            .margin({ bottom: 8 })
          Text("登录后开启你的 AI 养宠之旅")
            .fontSize(FontSizes.BODY_S)
            .fontColor(Colors.TEXT_SECONDARY)
        }
        .width("100%")
        .layoutWeight(1)
        .alignItems(HorizontalAlign.Center)
        .justifyContent(FlexAlign.Start)

        Column() {
          Row() {
            Image($r("app.media.icon_wechat"))
              .width(20)
              .height(20)
              .fillColor("#FFFFFF")
              .margin({ right: 8 })
            Text("微信一键登录")
              .fontSize(FontSizes.BODY)
              .fontColor(Colors.TEXT_WHITE)
          }
          .width("100%")
          .height(48)
          .backgroundColor("#07C160")
          .borderRadius(Radius.MD)
          .justifyContent(FlexAlign.Center)
          .alignItems(HorizontalAlign.Center)
          .enabled(!this.loading)
          .onClick(() => this.onWxLogin())

          Row() {
            Text("登录即表示同意")
              .fontSize(FontSizes.CAPTION)
              .fontColor(Colors.TEXT_TERTIARY)
            Text("《用户服务协议》")
              .fontSize(FontSizes.CAPTION)
              .fontColor(Colors.PRIMARY)
              .onClick(() => router.pushUrl({ url: "pages/common/WebView", params: { type: "service", title: "用户服务协议" } }))
            Text("和")
              .fontSize(FontSizes.CAPTION)
              .fontColor(Colors.TEXT_TERTIARY)
            Text("《隐私政策》")
              .fontSize(FontSizes.CAPTION)
              .fontColor(Colors.PRIMARY)
              .onClick(() => router.pushUrl({ url: "pages/common/WebView", params: { type: "privacy", title: "隐私政策" } }))
          }
          .textAlign(TextAlign.Center)
          .margin({ top: Spacing.M })
        }
        .padding(Spacing.L)
        .margin({ left: Spacing.L, right: Spacing.L, bottom: Spacing.XXL })
      }
      .width("100%")
      .height("100%")
      .backgroundColor(Colors.BG_PAGE)
    }
  }

  async onWxLogin() {
    this.loading = true
    try {
      const mockCode = "mock_code_" + Date.now()
      const data: any = await ApiService.wxLogin(mockCode, "毛团子用户", "", "V2.0")
      HttpClient.getInstance().setToken(data.token)
      promptAction.showToast({ message: "登录成功～", duration: 1200 })
      setTimeout(() => { router.replaceUrl({ url: "pages/Index" }) }, 800)
    } catch (e: any) {
      promptAction.showToast({ message: e.message || "登录失败", duration: 1500 })
    } finally {
      this.loading = false
    }
  }
}
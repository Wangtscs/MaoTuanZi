/**
 * 设置
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing } from "../../common/Constants";
import { TopBar } from "../../components/CommonComponents";
import { ApiService } from "../../service/ApiService";
import { HttpClient } from "../../service/HttpClient";

@Entry
@Component
struct SettingsPage {
  @State notificationEnabled: boolean = false

  build() {
    Column() {
      TopBar({ title: "设置", showBack: true, onBack: () => router.back() })

      Column() {
          // 健康提醒（订阅消息）
          Row() {
            Column() {
              Text("健康提醒")
                .fontSize(FontSizes.BODY)
                .fontColor(Colors.TEXT_PRIMARY)
              Text("接收宠物喂食、体检等提醒通知")
                .fontSize(FontSizes.CAPTION)
                .fontColor(Colors.TEXT_TERTIARY)
                .margin({ top: 2 })
            }
            .alignItems(HorizontalAlign.Start)
            .layoutWeight(1)

            if (this.notificationEnabled) {
              Row() {
                Text("已开启")
                  .fontSize(FontSizes.BODY_S)
                  .fontColor(Colors.SUCCESS)
              }
              .padding({ left: 10, right: 10, top: 4, bottom: 4 })
              .backgroundColor("rgba(82,196,26,0.1)")
              .borderRadius(Radius.SM)
              .onClick(() => {
                this.notificationEnabled = false
                promptAction.showToast({ message: "已关闭健康提醒", duration: 1500 })
              })
            } else {
              Text("去开启")
                .fontSize(FontSizes.BODY_S)
                .fontColor(Colors.PRIMARY)
                .padding({ left: 10, right: 10, top: 4, bottom: 4 })
                .backgroundColor("rgba(255,182,193,0.1)")
                .borderRadius(Radius.SM)
                .onClick(() => {
                  // TODO: @ohos.want 拉起微信小程序设置页
                  // 降级：提示用户在微信中开启订阅消息
                  promptAction.showToast({ message: "请在微信中开启订阅消息", duration: 2000 })
                })
            }
          }
          .width("100%")
          .padding({ left: Spacing.M, right: Spacing.M })
          .backgroundColor(Colors.BG_CARD)
          .alignItems(VerticalAlign.Center)
          .height(60)

          Divider().color(Colors.DIVIDER)
          this.settingsRow("隐私政策", () => router.pushUrl({ url: "pages/common/WebView", params: { type: "privacy", title: "隐私政策" } }))
          Divider().color(Colors.DIVIDER)
          this.settingsRow("用户服务协议", () => router.pushUrl({ url: "pages/common/WebView", params: { type: "service", title: "用户服务协议" } }))
          Divider().color(Colors.DIVIDER)
          this.settingsRow("数据导出（GDPR）", () => promptAction.showToast({ message: "导出请求已提交，24h 内完成", duration: 1500 }))
          Divider().color(Colors.DIVIDER)
          this.settingsRow("注销账号", () => this.onCancel(), isDanger: true)
          Divider().color(Colors.DIVIDER)
          this.settingsRow("退出登录", () => this.onLogout(), isDanger: true)
        }
        .padding({ left: Spacing.L, right: Spacing.L, top: Spacing.M })

      Text("毛团子AI V2.0.0")
        .fontSize(FontSizes.CAPTION)
        .fontColor(Colors.TEXT_TERTIARY)
        .width("100%")
        .textAlign(TextAlign.Center)
        .margin({ top: 32 })
    }
    .width("100%")
    .height("100%")
    .backgroundColor(Colors.BG_PAGE)
  }

  async onLogout() {
    try { await ApiService.logout() } catch (e) {}
    HttpClient.getInstance().setToken("")
    router.replaceUrl({ url: "pages/auth/Login" })
  }

  @Builder settingsRow(label: string, onTap: () => void, isDanger: boolean = false) {
    Row() {
      Text(label)
        .fontSize(FontSizes.BODY)
        .fontColor(isDanger ? "#FF4D4F" : Colors.TEXT_PRIMARY)
      Blank()
      Image($r("app.media.icon_chevron_right"))
        .width(16)
        .height(16)
        .fillColor(Colors.TEXT_TERTIARY)
    }
    .width("100%")
    .height(48)
    .padding({ left: Spacing.M, right: Spacing.M })
    .backgroundColor(Colors.BG_CARD)
    .alignItems(VerticalAlign.Center)
    .onClick(() => onTap())
  }

  onCancel() {
    promptAction.showDialog({
      title: "注销账号",
      message: "注销后将清除账号数据，此操作不可恢复",
      buttons: [
        { text: "再想想", color: "#999999" },
        { text: "确认注销", color: "#FF4D4F" }
      ]
    }).then(async (idx) => {
      if (idx.index === 1) {
        try {
          await ApiService.cancelAccount("no_need")
          promptAction.showToast({ message: "已注销", duration: 1500 })
          HttpClient.getInstance().setToken("")
          router.replaceUrl({ url: "pages/auth/Login" })
        } catch (e: any) {
          promptAction.showToast({ message: e.message, duration: 1500 })
        }
      }
    })
  }
}
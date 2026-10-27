/**
 * AI 生成预览页
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing, Radius } from "../../common/Constants";
import { TopBar, PrimaryButton, SecondaryButton } from "../../components/CommonComponents";
import { ApiService } from "../../service/ApiService";

@Entry
@Component
struct PreviewPage {
  @State work: any = {}
  @State loading: boolean = false
  private workId: number = 0

  aboutToAppear() {
    const params = router.getParams() as Record<string, Object>
    this.workId = (params?.workId as number) || 0
    if (this.workId > 0) {
      this.loadWork()
    }
  }

  async loadWork() {
    this.loading = true
    try {
      this.work = await ApiService.getWork(this.workId)
    } catch (e: any) {
      promptAction.showToast({ message: "加载失败，请重试", duration: 1500 })
    } finally {
      this.loading = false
    }
  }

  build() {
    Column() {
      TopBar({ title: "预览", showBack: true, onBack: () => router.back() })

      if (this.loading) {
        Column() {
          Column()
            .width("calc(100% - 32vp)")
            .height(400)
            .backgroundColor(Colors.BG_INPUT)
            .borderRadius(Radius.MD)
          LoadingProgress().color(Colors.PRIMARY).margin({ top: 20 })
        }
        .width("100%")
        .layoutWeight(1)
        .alignItems(HorizontalAlign.Center)
        .justifyContent(FlexAlign.Center)
      } else {
        Scroll() {
          Column() {
            // 大图预览
            Stack() {
              Image(this.work.previewUrl || "")
                .width("100%")
                .height(400)
                .objectFit(ImageFit.Cover)
                .borderRadius(Radius.MD)
                .onClick(() => {
                  if (this.work.previewUrl) {
                    router.pushUrl({ url: "pages/common/ImagePreview", params: { url: this.work.previewUrl } })
                  }
                })

            // 水印
            Text("毛团子AI · 预览版")
              .fontSize(20)
              .fontWeight(FontWeight.Bold)
              .fontColor("rgba(255,255,255,0.4)")
              .rotate({ angle: -20 })
              .position({ x: 60, y: 200 })
          }
          .padding(Spacing.L)

          // 标题
          Column() {
            Text(this.work.title || "我的 AI 作品")
              .fontSize(FontSizes.H2)
              .fontWeight(FontWeight.Medium)
              .fontColor(Colors.TEXT_PRIMARY)
            Text("解锁高清后即可下载与分享")
              .fontSize(FontSizes.BODY_S)
              .fontColor(Colors.TEXT_SECONDARY)
              .margin({ top: 4 })
          }
          .width("100%")
          .padding({ left: Spacing.L, right: Spacing.L })

          // 价格卡片
          Column() {
            Row() {
              Column() {
                Text("高清无水印")
                  .fontSize(FontSizes.BODY)
                  .fontWeight(FontWeight.Medium)
                  .fontColor(Colors.TEXT_PRIMARY)
                Text("永久保存 + 分享")
                  .fontSize(FontSizes.CAPTION)
                  .fontColor(Colors.TEXT_TERTIARY)
                  .margin({ top: 2 })
              }
              .layoutWeight(1)
              .alignItems(HorizontalAlign.Start)

              Column() {
                Text("¥9.9")
                  .fontSize(24)
                  .fontWeight(FontWeight.Bold)
                  .fontColor(Colors.PRIMARY)
                Text("限时优惠")
                  .fontSize(FontSizes.TINY)
                  .fontColor(Colors.WARNING)
              }
              .alignItems(HorizontalAlign.End)
            }
            .width("100%")
          }
          .padding(Spacing.L)
          .backgroundColor(Colors.BG_CARD)
          .borderRadius(Radius.MD)
          .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.M })

          // 操作
          Column() {
            PrimaryButton({ text: "解锁高清 ¥9.9", block: true })
              .margin({ bottom: Spacing.M })
              .onClick(() => this.onUnlock())

            SecondaryButton({ text: "保存到作品", block: true })
              .onClick(() => promptAction.showToast({ message: "已保存", duration: 1500 }))
          }
          .margin({ top: Spacing.L, left: Spacing.L, right: Spacing.L })
        }
        .width("100%")
      }
      .layoutWeight(1)
      .scrollBar(BarState.Off)
    }
    .width("100%")
    .height("100%")
    .backgroundColor(Colors.BG_PAGE)
  }

  async onUnlock() {
    try {
      const order: any = await ApiService.createOrder({
        productId: "portrait_single",
        workId: this.workId
      })
      promptAction.showToast({ message: "订单已创建，请前往支付", duration: 1500 })
      router.pushUrl({ url: "pages/me/OrderDetail", params: { orderId: order.id } })
    } catch (e: any) {
      promptAction.showToast({ message: e.message || "操作失败", duration: 1500 })
    }
  }
}
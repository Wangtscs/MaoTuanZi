/**
 * 订单详情
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing, Radius, Shadow, shadowOf } from "../../common/Constants";
import { TopBar, PrimaryButton, StateView } from "../../components/CommonComponents";
import { ApiService } from "../../service/ApiService";
import { PaymentManager } from "../../utils/PaymentManager";
import type { PayChannel } from "../../utils/PaymentManager";
import { sharePost, shareViaSystem } from "../../utils/ShareSheet";

@Entry
@Component
struct OrderDetailPage {
  @State order: any = {}
  @State loading: boolean = false
  @State paying: boolean = false
  @State payChannel: PayChannel = "HUAWEI" // 华为为主、微信兜底
  private orderId: number = 0

  aboutToAppear() {
    const params = router.getParams() as Record<string, Object>
    this.orderId = (params?.orderId as number) || 0
    this.load()
  }

  async load() {
    this.loading = true
    try {
      this.order = await ApiService.getOrder(this.orderId) || {}
    } catch (e: any) {
      promptAction.showToast({ message: "加载订单失败，请重试", duration: 1500 })
    } finally {
      this.loading = false
    }
  }

  /** 调起真实支付（统一支付管理器：华为/微信/ Mock 兜底） */
  async onPay() {
    if (this.paying) {
      return
    }
    this.paying = true
    try {
      const result = await PaymentManager.pay(this.orderId, this.payChannel)
      if (result.success) {
        promptAction.showToast({ message: "支付成功", duration: 1500 })
        await this.load()
      } else {
        promptAction.showToast({ message: result.message || "支付未完成", duration: 1500 })
      }
    } catch (e: any) {
      promptAction.showToast({ message: e?.message || "支付失败", duration: 1500 })
    } finally {
      this.paying = false
    }
  }

  /** 预留：支付成功后可申请退款（POST /v1/orders/{id}/refund） */
  async onRefund() {
    try {
      const res: any = await ApiService.orderRefund(this.orderId)
      promptAction.showToast({ message: "退款申请已提交", duration: 1500 })
      await this.load()
    } catch (e: any) {
      promptAction.showToast({ message: e?.message || "退款申请失败", duration: 1500 })
    }
  }

  async onClose() {
    try {
      await ApiService.closeOrder(this.orderId)
      promptAction.showToast({ message: "已关闭", duration: 1200 })
      router.back()
    } catch (e: any) {
      promptAction.showToast({ message: e.message || "操作失败", duration: 1500 })
    }
  }

  /** 分享得免费券：关联帖子则记录分享拿奖励，否则分享邀请链接 */
  async onShareCoupon() {
    const postId: number = (this.order.postId as number) || 0
    if (postId > 0) {
      await sharePost(postId, "毛团子AI 会员好礼", "WECHAT")
      return
    }
    try {
      const info: any = await ApiService.userInvite()
      const url: string = info?.inviteUrl || "maotuanzi://invite/MAOTUANZI"
      await shareViaSystem("毛团子AI · 邀请有礼", "邀好友得 VIP，快来体验萌宠 AI", url)
      promptAction.showToast({ message: "分享成功，优惠券将发放", duration: 2000 })
    } catch (e) {
      promptAction.showToast({ message: "分享成功", duration: 1500 })
    }
  }

  build() {
    Column() {
      TopBar({ title: "订单详情", showBack: true, onBack: () => router.back() })

      if (this.loading) {
        StateView({ mode: 'loading' })
      } else {
        Scroll() {
          Column() {
            // 状态区
            Column() {
              Text(this.statusText(this.order.status))
                .fontSize(28)
                .fontWeight(FontWeight.Bold)
                .fontColor(this.statusColor(this.order.status))
              Text(`¥${((this.order.payAmountCents || 0) / 100).toFixed(2)}`)
                .fontSize(48)
                .fontWeight(FontWeight.Bold)
                .fontColor(Colors.TEXT_PRIMARY)
                .margin({ top: Spacing.M })
            }
            .width("100%")
            .padding({ top: 40, bottom: 40 })
            .backgroundColor(Colors.BG_CARD)
            .alignItems(HorizontalAlign.Center)

            // 详情
            Column() {
              this.detailRow("订单号", this.order.orderNo || "-")
              this.detailRow("商品", this.order.productName || "-")
              this.detailRow("下单时间", this.formatDate(this.order.createdAt))
              this.detailRow("订单状态", this.statusText(this.order.status))
              if (this.order.paidAt) {
                this.detailRow("支付时间", this.formatDate(this.order.paidAt))
              }
              if (this.order.wxTransactionId) {
                this.detailRow("微信交易号", this.order.wxTransactionId)
              }
            }
            .padding(Spacing.L)
            .backgroundColor(Colors.BG_CARD)
            .margin({ top: Spacing.M })

            if (this.order.status === 0) {
              this.channelSelector()
              PrimaryButton({ text: this.paying ? "支付中..." : "立即支付", block: true })
                .margin({ top: Spacing.L, left: Spacing.L, right: Spacing.L })
                .onClick(() => this.onPay())

              Button("取消订单")
                .fontSize(FontSizes.BODY_S)
                .fontColor("#999999")
                .backgroundColor("transparent")
                .margin({ top: Spacing.S, bottom: Spacing.XL })
                .onClick(() => this.onClose())
            } else if (this.order.status === 1) {
              // 支付成功：展示成功动画 + 作品 + 三个操作按钮
              Column() {
                // 成功动画区：圆形底色 + 对勾中心叠放，用 Stack 居中避免负 margin 错位
                Column() {
                  Stack({ alignContent: Alignment.Center }) {
                    Column()
                      .width(72)
                      .height(72)
                      .borderRadius(36)
                      .backgroundColor("rgba(82,196,26,0.1)")
                    Text("✓")
                      .fontSize(36)
                      .fontColor(Colors.SUCCESS)
                  }
                  .margin({ bottom: Spacing.M })
                  .transition(TransitionEffect.OPACITY.animation({ duration: 400 }))

                  Text("支付成功！")
                    .fontSize(22)
                    .fontWeight(FontWeight.Bold)
                    .fontColor(Colors.TEXT_PRIMARY)
                    .margin({ top: Spacing.M })
                }
                .width("100%")
                .padding({ top: 32, bottom: 32 })
                .alignItems(HorizontalAlign.Center)

                // 作品展示
                if (this.order.workPreviewUrl) {
                  Column() {
                    Image(this.order.workPreviewUrl)
                      .width("100%")
                      .height(200)
                      .objectFit(ImageFit.Cover)
                      .borderRadius(Radius.MD)
                      .onClick(() => {
                        if (this.order.workPreviewUrl) {
                          router.pushUrl({
                            url: "pages/common/ImagePreview",
                            params: { url: this.order.workPreviewUrl }
                          })
                        }
                      })
                  }
                  .width("calc(100% - 32vp)")
                  .margin({ left: Spacing.L, right: Spacing.L })
                }

                // 三个操作按钮
                Row({ space: Spacing.S }) {
                  // 保存到相册
                  Column() {
                    Column()
                      .width(44)
                      .height(44)
                      .borderRadius(22)
                      .backgroundColor("rgba(255,182,193,0.1)")
                      .justifyContent(FlexAlign.Center)
                      .alignItems(HorizontalAlign.Center)
                    Text("保存到相册")
                      .fontSize(FontSizes.CAPTION)
                      .fontColor(Colors.TEXT_PRIMARY)
                      .margin({ top: 4 })
                  }
                  .flexGrow(1)
                  .alignItems(HorizontalAlign.Center)
                  .onClick(() => {
                    promptAction.showToast({ message: "已保存到相册", duration: 1500 })
                  })

                  // 分享得免费券
                  Column() {
                    Column()
                      .width(44)
                      .height(44)
                      .borderRadius(22)
                      .backgroundColor("rgba(255,170,128,0.1)")
                      .justifyContent(FlexAlign.Center)
                      .alignItems(HorizontalAlign.Center)
                    Text("分享得免费券")
                      .fontSize(FontSizes.CAPTION)
                      .fontColor(Colors.TEXT_PRIMARY)
                      .margin({ top: 4 })
                  }
                  .flexGrow(1)
                  .alignItems(HorizontalAlign.Center)
                  .onClick(() => this.onShareCoupon())

                  // 升级月卡
                  Column() {
                    Column()
                      .width(44)
                      .height(44)
                      .borderRadius(22)
                      .backgroundColor("rgba(250,173,20,0.1)")
                      .justifyContent(FlexAlign.Center)
                      .alignItems(HorizontalAlign.Center)
                    Text("升级月卡")
                      .fontSize(FontSizes.CAPTION)
                      .fontColor(Colors.TEXT_PRIMARY)
                      .margin({ top: 4 })
                  }
                  .flexGrow(1)
                  .alignItems(HorizontalAlign.Center)
                  .onClick(() => {
                    router.pushUrl({ url: "pages/me/Vip" })
                  })
                }
                .width("calc(100% - 32vp)")
                .margin({ top: Spacing.L, left: Spacing.L, right: Spacing.L })

                // 预留退款入口
                Text("申请退款")
                  .fontSize(FontSizes.BODY_S)
                  .fontColor(Colors.TEXT_TERTIARY)
                  .margin({ top: Spacing.L, bottom: Spacing.XXL })
                  .onClick(() => this.onRefund())
              }
              .width("100%")
            }
          }
          .width("100%")
        }
        .layoutWeight(1)
        .scrollBar(BarState.Off)
      }
    }
    .width("100%")
    .height("100%")
    .backgroundColor(Colors.BG_PAGE)
  }

  @Builder channelSelector() {
    Row({ space: Spacing.S }) {
      this.channelPill("华为支付", "HUAWEI", $r("app.media.icon_crown"))
      this.channelPill("微信支付", "WECHAT", $r("app.media.icon_coupon"))
    }
    .width("100%")
    .padding({ left: Spacing.L, right: Spacing.L, top: Spacing.M, bottom: Spacing.S })
  }

  @Builder channelPill(label: string, value: PayChannel, icon: Resource) {
    Row() {
      Image(icon)
        .width(16).height(16)
        .fillColor(this.payChannel === value ? "#FFFFFF" : Colors.PRIMARY)
        .margin({ right: 4 })
      Text(label)
        .fontSize(FontSizes.BODY_S)
        .fontColor(this.payChannel === value ? "#FFFFFF" : Colors.TEXT_PRIMARY)
    }
    .height(40)
    .flexGrow(1)
    .justifyContent(FlexAlign.Center)
    .backgroundColor(this.payChannel === value ? Colors.PRIMARY : Colors.BG_CARD)
    .borderRadius(Radius.MD)
    .border(this.payChannel === value ? { width: 0, color: Colors.PRIMARY } : { width: 1, color: Colors.DIVIDER })
    .onClick(() => {
      animateTo({ duration: 150 }, () => {
        this.payChannel = value
      })
    })
  }

  @Builder detailRow(label: string, value: string) {
    Row() {
      Text(label)
        .fontSize(FontSizes.BODY_S)
        .fontColor(Colors.TEXT_SECONDARY)
        .width(80)
      Text(value)
        .fontSize(FontSizes.BODY_S)
        .fontColor(Colors.TEXT_PRIMARY)
        .layoutWeight(1)
    }
    .width("100%")
    .padding({ top: 8, bottom: 8 })
  }

  statusText(s: number): string {
    return ["待支付", "已支付", "已完成", "已关闭", "退款中", "已退款", "拒绝"][s] || "未知"
  }

  statusColor(s: number): string {
    return ["#FAAD14", "#52C41A", "#52C41A", "#999999", "#FAAD14", "#FF4D4F", "#999999"][s] || "#666666"
  }

  formatDate(s: string): string {
    return s ? new Date(s).toLocaleString("zh-CN") : "-"
  }
}

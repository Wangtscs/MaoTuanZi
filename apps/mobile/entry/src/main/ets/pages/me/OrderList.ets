/**
 * 我的订单列表
 */
import router from "@ohos.router";
import { Colors, FontSizes, Spacing, Radius } from "../../common/Constants";
import { TopBar, EmptyState, Tag } from "../../components/CommonComponents";
import { ApiService } from "../../service/ApiService";

@Entry
@Component
struct OrderListPage {
  @State orders: any[] = []
  @State loading: boolean = false

  aboutToAppear() {
    this.load()
  }

  async load() {
    this.loading = true
    try {
      const data: any = await ApiService.listOrders(undefined, 1, 50)
      this.orders = data?.list || []
    } catch (e: any) {
      promptAction.showToast({ message: "加载订单失败，请重试", duration: 1500 })
    } finally {
      this.loading = false
    }
  }

  build() {
    Column() {
      TopBar({ title: "我的订单", showBack: true, onBack: () => router.back() })

      if (this.orders.length === 0 && !this.loading) {
        EmptyState({
          iconName: "folder",
          title: "还没有订单",
          desc: "去逛逛，会员和单次服务都在这里"
        })
      } else {
        List() {
          ForEach(this.orders, (o: any) => {
            ListItem() {
              Column() {
                Row() {
                  Text(o.orderNo)
                    .fontSize(FontSizes.BODY_S)
                    .fontColor(Colors.TEXT_PRIMARY)
                    .layoutWeight(1)
                  Tag({ text: this.statusName(o.status), type: this.statusType(o.status), size: "small" })
                }
                .width("100%")
                .margin({ bottom: Spacing.S })

                Row() {
                  Column() {
                    Text(o.productName)
                      .fontSize(FontSizes.BODY)
                      .fontColor(Colors.TEXT_PRIMARY)
                    Text(`¥${(o.payAmountCents / 100).toFixed(2)}`)
                      .fontSize(20)
                      .fontWeight(FontWeight.Bold)
                      .fontColor(Colors.PRIMARY)
                      .margin({ top: 6 })
                  }
                  .layoutWeight(1)
                  .alignItems(HorizontalAlign.Start)

                  if (o.status === 0) {
                    Button("去支付")
                      .fontSize(FontSizes.BODY_S)
                      .fontColor(Colors.TEXT_WHITE)
                      .backgroundColor(Colors.PRIMARY)
                      .height(32)
                      .borderRadius(16)
                      .padding({ left: 12, right: 12 })
                      .onClick(() => router.pushUrl({ url: "pages/me/OrderDetail", params: { orderId: o.id } }))
                  }
                }
                .width("100%")
              }
              .width("100%")
              .padding(Spacing.M)
              .backgroundColor(Colors.BG_CARD)
              .borderRadius(Radius.MD)
              .onClick(() => router.pushUrl({ url: "pages/me/OrderDetail", params: { orderId: o.id } }))
            }
            .padding({ left: Spacing.L, right: Spacing.L, bottom: Spacing.S })
          })
        }
        .layoutWeight(1)
        .backgroundColor(Colors.BG_PAGE)
      }
    }
    .width("100%")
    .height("100%")
    .backgroundColor(Colors.BG_PAGE)
  }

  statusName(s: number): string {
    return ["待支付", "已支付", "已完成", "已关闭", "退款中", "已退款", "拒绝"][s] || ""
  }
  statusType(s: number): 'default' | 'primary' | 'warning' | 'success' | 'error' | 'info' {
    return (["warning", "success", "success", "info", "warning", "error", "info"][s] || "default") as 'default' | 'primary' | 'warning' | 'success' | 'error' | 'info'
  }
}

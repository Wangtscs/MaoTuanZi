/**
 * 卡券中心
 *
 * 阶段2（会员对齐）：接 POST /v1/coupons/claim 领取卡券。
 * 后端暂无「卡券列表」查询接口（仅提供 claim），故本页以可领取的示例卡券列表驱动，
 * 点击「领取」调用 couponClaim；已领取的卡券记录在本地状态中。
 * 后端补充「我的卡券列表」接口后，可将 loadAvailable 替换为真实列表查询。
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing, Radius } from "../../common/Constants";
import { TopBar, EmptyState } from "../../components/CommonComponents";
import { ApiService } from "../../service/ApiService";

/** 可领取卡券结构（后端无列表接口时的示例数据） */
interface CouponItem {
  id: string;
  title: string;
  desc: string;
  valueText: string;
}

// 示例可领取卡券（后端 /v1/coupons 列表就绪后替换为真实数据）
const AVAILABLE_COUPONS: CouponItem[] = [
  { id: "welcome_5", title: "新人立减券", desc: "全场满 19 减 5", valueText: "¥5" },
  { id: "vip_3days", title: "会员体验券", desc: "邀请好友可得 3 天 VIP", valueText: "3天" },
  { id: "hd_1", title: "高清额度券", desc: "解锁 1 张高清画像", valueText: "1张" }
]

@Entry
@Component
struct CouponsPage {
  @State claimedIds: string[] = []
  @State claiming: boolean = false

  /** 判断卡券是否已领取 */
  isClaimed(id: string): boolean {
    return this.claimedIds.indexOf(id) >= 0
  }

  /** 领取卡券：调 POST /v1/coupons/claim */
  async onClaim(coupon: CouponItem) {
    if (this.isClaimed(coupon.id) || this.claiming) {
      return
    }
    this.claiming = true
    try {
      const res: any = await ApiService.couponClaim(coupon.id)
      // 契约返回 {id, status:"CLAIMED"}
      if (res?.status === "CLAIMED" || res?.id) {
        this.claimedIds = this.claimedIds.concat([coupon.id])
        promptAction.showToast({ message: "领取成功，已放入卡包", duration: 1500 })
      } else {
        promptAction.showToast({ message: "领取失败，请稍后重试", duration: 1500 })
      }
    } catch (e: any) {
      // 后端未就绪时降级为本地领取成功，保证可演示
      this.claimedIds = this.claimedIds.concat([coupon.id])
      promptAction.showToast({ message: "已领取（演示模式）", duration: 1500 })
    } finally {
      this.claiming = false
    }
  }

  build() {
    Column() {
      TopBar({ title: "卡券中心", showBack: true, onBack: () => router.back() })

      if (AVAILABLE_COUPONS.length === 0) {
        EmptyState({
          iconName: "crown",
          title: "暂无卡券",
          desc: "优惠券和福利将在这里展示"
        })
      } else {
        Scroll() {
          Column() {
            Text("可领取福利")
              .fontSize(FontSizes.H2)
              .fontWeight(FontWeight.Medium)
              .fontColor(Colors.TEXT_PRIMARY)
              .width("100%")
              .margin({ top: Spacing.L, bottom: Spacing.M, left: Spacing.L })

            ForEach(AVAILABLE_COUPONS, (coupon: CouponItem) => {
              Row() {
                // 左侧：面值/价值
                Column() {
                  Text(coupon.valueText)
                    .fontSize(24)
                    .fontWeight(FontWeight.Bold)
                    .fontColor(Colors.PRIMARY)
                }
                .width(72)
                .height(72)
                .borderRadius(Radius.MD)
                .backgroundColor("rgba(255,182,193,0.12)")
                .justifyContent(FlexAlign.Center)
                .alignItems(HorizontalAlign.Center)

                // 中间：标题 + 说明
                Column() {
                  Text(coupon.title)
                    .fontSize(FontSizes.BODY)
                    .fontWeight(FontWeight.Medium)
                    .fontColor(Colors.TEXT_PRIMARY)
                  Text(coupon.desc)
                    .fontSize(FontSizes.CAPTION)
                    .fontColor(Colors.TEXT_SECONDARY)
                    .margin({ top: 4 })
                }
                .layoutWeight(1)
                .alignItems(HorizontalAlign.Start)
                .margin({ left: Spacing.M })

                // 右侧：领取按钮 / 已领取
                Button(this.isClaimed(coupon.id) ? "已领取" : "领取")
                  .height(32)
                  .fontSize(FontSizes.BODY_S)
                  .fontColor(this.isClaimed(coupon.id) ? Colors.TEXT_TERTIARY : Colors.TEXT_WHITE)
                  .backgroundColor(this.isClaimed(coupon.id) ? Colors.BG_INPUT : Colors.PRIMARY)
                  .borderRadius(Radius.SM)
                  .enabled(!this.isClaimed(coupon.id) && !this.claiming)
                  .onClick(() => this.onClaim(coupon))
              }
              .width("calc(100% - 32vp)")
              .padding(Spacing.M)
              .backgroundColor(Colors.BG_CARD)
              .borderRadius(Radius.LG)
              .margin({ left: Spacing.L, right: Spacing.L, bottom: Spacing.M })
            })
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
}

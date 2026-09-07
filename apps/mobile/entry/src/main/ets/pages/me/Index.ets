/**
 * 我的 Tab
 */
import router from "@ohos.router";
import { Colors, FontSizes, Spacing, Radius, Shadow, shadowOf } from "../../common/Constants";
import { ApiService } from "../../service/ApiService";
import { HttpClient } from "../../service/HttpClient";
import { CrownIcon, BellIcon, SparkleIcon, ChevronRightIcon } from "../../components/icons/Icons";

@Component
export struct MeTab {
  @State user: any = {}
  @State vipInfo: any = {}        // 来自 /v1/vip/me 的会员状态（到期日 / 权益）
  @State unreadCount: number = 0

  aboutToAppear() {
    this.loadAll()
  }

  async loadAll() {
    try {
      this.user = await ApiService.me()
    } catch (e) {
      router.replaceUrl({ url: "pages/auth/Login" })
      return
    }
    try {
      const data: any = await ApiService.getUnreadCount()
      this.unreadCount = data?.count || 0
    } catch (e) {
      // 未读数获取失败不影响主流程，静默处理
    }
    this.loadVip()
  }

  /** 读取当前会员状态（到期日 / 权益），与 /users/me 的 vipLevel 互补 */
  async loadVip() {
    try {
      const data: any = await ApiService.vipMe()
      this.vipInfo = data || {}
    } catch (e) {
      this.vipInfo = {}
    }
  }

  async onLogout() {
    try {
      await ApiService.logout()
    } catch (e) {
      // 登出API失败仍执行本地登出
    }
    HttpClient.getInstance().setToken("")
    router.replaceUrl({ url: "pages/auth/Login" })
  }

  build() {
    Scroll() {
      Column() {
        // 用户卡片
        Column() {
          Row() {
            Image(this.user.avatarUrl || $r("app.media.icon_user"))
              .width(60)
              .height(60)
              .borderRadius(30)
              .fillColor(Colors.PRIMARY)
              .backgroundColor(Colors.BG_INPUT)
            Column() {
              Text(this.user.nickname || "毛团子用户")
                .fontSize(FontSizes.H2)
                .fontWeight(FontWeight.Medium)
                .fontColor(Colors.TEXT_PRIMARY)
              Row() {
                if (this.user.vipLevel > 0) {
                  CrownIcon({ size: 14, color: "#FAAD14" })
                  Text(this.vipName(this.user.vipLevel))
                    .fontSize(FontSizes.CAPTION)
                    .fontColor("#FAAD14")
                    .margin({ left: 2 })
                } else {
                  Text("开通会员解锁更多专属功能")
                    .fontSize(FontSizes.CAPTION)
                    .fontColor(Colors.TEXT_TERTIARY)
                }
              }
              .margin({ top: 4 })
            }
            .layoutWeight(1)
            .margin({ left: Spacing.M })
            .alignItems(HorizontalAlign.Start)

            if (this.unreadCount > 0) {
              Text(`${this.unreadCount}`)
                .fontSize(FontSizes.TINY)
                .fontColor(Colors.TEXT_WHITE)
                .backgroundColor("#FF4D4F")
                .borderRadius(8)
                .padding({ left: 6, right: 6, top: 1, bottom: 1 })
            }
            BellIcon({ size: 20, color: Colors.TEXT_PRIMARY })
              .onClick(() => router.pushUrl({ url: "pages/me/Notifications" }))
          }
          .width("100%")
          .padding(Spacing.L)
        }
        .shadow(shadowOf(Shadow.SM))

        // VIP Banner
        Stack() {
          Row() {
            Column() {
              Row() {
                SparkleIcon({ size: 18, color: "#FFFFFF" })
                  .margin({ right: 6 })
                Text("升级会员")
                  .fontSize(FontSizes.H2)
                  .fontWeight(FontWeight.Bold)
                  .fontColor("#FFFFFF")
              }
              Text("畅享专属风格 + 每月 5 张高清额度")
                .fontSize(FontSizes.CAPTION)
                .fontColor("rgba(255,255,255,0.88)")
                .margin({ top: 4 })
              if (this.user.vipLevel > 0 && this.vipInfo?.vipExpireAt) {
                Text(`会员有效期至 ${this.formatExpire(this.vipInfo.vipExpireAt)}`)
                  .fontSize(FontSizes.TINY)
                  .fontColor("rgba(255,255,255,0.92)")
                  .margin({ top: 4 })
              }
              if (this.benefitsText() !== "") {
                Text(`权益：${this.benefitsText()}`)
                  .fontSize(FontSizes.TINY)
                  .fontColor("rgba(255,255,255,0.85)")
                  .maxLines(2)
                  .textOverflow({ overflow: TextOverflow.Ellipsis })
                  .margin({ top: 4 })
              }
            }
            .layoutWeight(1)
            .alignItems(HorizontalAlign.Start)

            Row() {
              Text("¥19.9")
                .fontSize(16)
                .fontColor("#FFFFFF")
              Text("/月")
                .fontSize(FontSizes.CAPTION)
                .fontColor("rgba(255,255,255,0.78)")
                .margin({ left: 2 })
            }
          }
          .width("100%")
          .padding(Spacing.L)
          .justifyContent(FlexAlign.SpaceBetween)
          .alignItems(VerticalAlign.Center)

          // 右上角装饰圆
          Column()
            .width(60).height(60).borderRadius(30)
            .backgroundColor("rgba(255,255,255,0.16)")
            .position({ x: "82%", y: -10 })
        }
        .linearGradient({ angle: 135, colors: [[0xffB6C1], [0xffAA80]] })
        .width("calc(100% - 32vp)")
        .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.S })
        .borderRadius(Radius.LG)
        .shadow(shadowOf(Shadow.MD))
        .onClick(() => router.pushUrl({ url: "pages/me/Vip" }))

        // 功能列表
        Column() {
          this.row("我的作品", "查看创作记录", $r("app.media.icon_paint"), () => router.pushUrl({ url: "pages/me/Works" }))
          this.divider()
          this.row("我的订单", "购买与订阅记录", $r("app.media.icon_list"), () => router.pushUrl({ url: "pages/me/OrderList" }))
          this.divider()
          this.row("卡券中心", "优惠券与福利", $r("app.media.icon_coupon"), () => router.pushUrl({ url: "pages/me/Coupons" }))
          this.divider()
          this.row("邀请有礼", "邀好友得 VIP", $r("app.media.icon_gift"), () => router.pushUrl({ url: "pages/me/Invite" }))
          this.divider()
          this.row("我的宠物", "毛孩子档案", $r("app.media.icon_pet"), () => router.pushUrl({ url: "pages/tools/PetList" }))
          this.divider()
          this.row("收藏与点赞", "我的互动", $r("app.media.icon_heart"), () => router.pushUrl({ url: "pages/me/Favorites" }))
          this.divider()
          this.row("联系客服", "帮助与反馈", $r("app.media.icon_user"), () => {})
        }
        .padding({ left: Spacing.L, right: Spacing.L, top: Spacing.M })

        Column() {
          this.row("设置", "隐私、协议、注销", $r("app.media.icon_setting"), () => router.pushUrl({ url: "pages/me/Settings" }))
        }
        .padding({ left: Spacing.L, right: Spacing.L, top: Spacing.M, bottom: Spacing.XL })
      }
      .width("100%")
    }
    .scrollBar(BarState.Off)
    .backgroundColor(Colors.BG_PAGE)
  }

  @Builder row(title: string, desc: string, icon: Resource, onTap: () => void) {
    Row() {
      Column() {
        Image(icon)
          .width(18).height(18)
          .fillColor("#FFFFFF")
      }
      .width(32).height(32)
      .borderRadius(8)
      .backgroundColor(Colors.PRIMARY_LIGHT)
      .justifyContent(FlexAlign.Center)
      .alignItems(HorizontalAlign.Center)
      .margin({ right: Spacing.M })

      Column() {
        Text(title)
          .fontSize(FontSizes.BODY)
          .fontColor(Colors.TEXT_PRIMARY)
        Text(desc)
          .fontSize(FontSizes.CAPTION)
          .fontColor(Colors.TEXT_TERTIARY)
          .margin({ top: 2 })
      }
      .layoutWeight(1)
      .alignItems(HorizontalAlign.Start)

      ChevronRightIcon({ size: 16, color: "#CCCCCC" })
    }
    .width("100%")
    .height(56)
    .padding({ left: Spacing.M, right: Spacing.M })
    .backgroundColor(Colors.BG_CARD)
    .alignItems(VerticalAlign.Center)
    .onClick(() => onTap())
  }

  @Builder divider() {
    Divider().color(Colors.DIVIDER).margin({ left: Spacing.M + 32, right: Spacing.M })
  }

  vipName(v: number): string {
    return ["普通", "月卡会员", "季卡会员", "年卡会员"][v] || ""
  }

  formatExpire(iso: string): string {
    try {
      return new Date(iso).toLocaleDateString("zh-CN")
    } catch (e) {
      return iso
    }
  }

  /** 拼接会员权益文案（来自 /v1/vip/me 的 benefits[]） */
  benefitsText(): string {
    const benefits = this.vipInfo?.benefits
    if (Array.isArray(benefits) && benefits.length > 0) {
      return benefits.join(" · ")
    }
    return ""
  }
}

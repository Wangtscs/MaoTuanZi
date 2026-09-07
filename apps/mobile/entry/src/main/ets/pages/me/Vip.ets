/**
 * 会员中心
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing, Radius, Shadow, shadowOf } from "../../common/Constants";
import { TopBar, PrimaryButton, EmptyState, StateView } from "../../components/CommonComponents";
import { ApiService } from "../../service/ApiService";
import { PaymentManager } from "../../utils/PaymentManager";
import type { PayChannel } from "../../utils/PaymentManager";
import { CrownIcon, CheckIcon } from "../../components/icons/Icons";

// 默认套餐（后端 /v1/vip/plans 不可用时的降级展示）
const DEFAULT_PRODUCTS = [
  { id: "vip_month", name: "月卡会员", priceCents: 1990, durationDays: 30, originalPriceCents: 2990, highlight: false },
  { id: "vip_quarter", name: "季卡会员", priceCents: 4990, durationDays: 90, originalPriceCents: 6990, highlight: true },
  { id: "vip_year", name: "年卡会员", priceCents: 15990, durationDays: 365, originalPriceCents: 19990, highlight: false }
]

// 权益对比表
const BENEFITS = [
  { label: "高清画像额度", free: "0 张", quarter: "15 张/月", year: "50 张/月" },
  { label: "专属风格", free: "3 种", quarter: "全部解锁", year: "全部解锁" },
  { label: "3D 手办", free: "不支持", quarter: "每月 1 次", year: "每月 3 次" },
  { label: "优先体验新功能", free: false, quarter: true, year: true },
  { label: "专属客服", free: false, quarter: "24h 响应", year: "1v1 专属" },
  { label: "养宠工具", free: "基础版", quarter: "高级版", year: "专业版" }
]

// FAQ 数据
const FAQ_ITEMS = [
  { q: "如何开通会员？", a: "选择套餐后完成支付即可立即开通，支持华为支付、微信支付。" },
  { q: "会员到期后权益怎么办？", a: "到期后未使用的额度将清零，但已生成的图片可正常查看。" },
  { q: "可以退款吗？", a: "虚拟商品一经购买不支持退款，请知悉。" }
]

@Entry
@Component
struct VipPage {
  @State products: any[] = []
  @State selectedId: string = "vip_quarter"
  @State loading: boolean = false
  @State countdownSec: number = 86400  // 24h = 86400s
  @State expandedFaq: number = -1
  @State vipInfo: any = {}               // 来自 /v1/vip/me 的当前会员状态
  @State payChannel: PayChannel = "HUAWEI" // 华为为主、微信兜底
  @State paying: boolean = false
  private countdownTimer: number = -1

  aboutToAppear() {
    this.loadProducts()
    this.loadVip()
    this.loadCountdown()
    this.startCountdown()
  }

  aboutToDisappear() {
    if (this.countdownTimer > 0) clearInterval(this.countdownTimer)
  }

  startCountdown() {
    this.countdownTimer = setInterval(() => {
      if (this.countdownSec > 0) {
        this.countdownSec -= 1
      }
    }, 1000)
  }

  async loadCountdown() {
    try {
      const data: any = await ApiService.getActivityCountdown()
      if (data && data.endTime) {
        const remaining = Math.floor((data.endTime - Date.now()) / 1000)
        this.countdownSec = remaining > 0 ? remaining : 86400
      }
    } catch (e) {
      // 降级到默认 24h
    }
  }

  formatCountdown(sec: number): string {
    const h = Math.floor(sec / 3600)
    const m = Math.floor((sec % 3600) / 60)
    const s = sec % 60
    return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
  }

  /**
   * 读取会员套餐：优先 /v1/vip/plans（后端契约），失败降级硬编码 DEFAULT_PRODUCTS。
   * 字段映射：plan{id, productType, name, priceCents, durationDays, tag}
   *   → product{id, name, priceCents, originalPriceCents, durationDays, highlight}
   */
  async loadProducts() {
    this.loading = true
    try {
      const data: any = await ApiService.vipPlans()
      const list: any[] = (data || []).map((p: any) => ({
        id: p.id,
        name: p.name,
        priceCents: p.priceCents,
        originalPriceCents: p.originalPriceCents || 0,
        durationDays: p.durationDays,
        highlight: p.tag === "热门" || p.tag === "推荐",
        tag: p.tag || ""
      }))
      this.products = list.length > 0 ? list : DEFAULT_PRODUCTS
      if (this.products.length > 0 && !this.products.some((x) => x.id === this.selectedId)) {
        this.selectedId = this.products[0].id
      }
    } catch (e: any) {
      this.products = DEFAULT_PRODUCTS
      promptAction.showToast({ message: "加载套餐失败，展示示例", duration: 1500 })
    } finally {
      this.loading = false
    }
  }

  /** 读取当前会员状态（来自 /v1/vip/me） */
  async loadVip() {
    try {
      const data: any = await ApiService.vipMe()
      this.vipInfo = data || {}
    } catch (e) {
      this.vipInfo = {}
    }
  }

  async onPurchase(product: any) {
    if (this.paying) {
      return
    }
    this.paying = true
    try {
      const order: any = await ApiService.createOrder({ productId: product.id })
      promptAction.showToast({ message: "订单已创建，正在调起支付", duration: 1500 })

      const result = await PaymentManager.pay(order.id as number, this.payChannel)
      if (result.success) {
        promptAction.showToast({ message: "支付成功，会员已开通", duration: 2000 })
        await this.loadVip()
      } else {
        promptAction.showToast({ message: result.message || "支付未完成", duration: 1500 })
      }
    } catch (e: any) {
      promptAction.showToast({ message: e?.message || "操作失败", duration: 1500 })
    } finally {
      this.paying = false
    }
  }

  build() {
    Column() {
      TopBar({
        title: "会员中心",
        showBack: true,
        onBack: () => router.back()
      })

      if (this.loading) {
        StateView({ mode: 'loading' })
      } else if (this.products.length === 0) {
        EmptyState({
          iconName: "crown",
          title: "暂无可用套餐",
          desc: "稍后再来看看"
        })
          .layoutWeight(1)
      } else {
        Scroll() {
          Column() {
            // 顶部皇冠 Banner + 倒计时
            Stack() {
              Column() {
                Image($r("app.media.icon_crown"))
                  .width(48)
                  .height(48)
                  .fillColor("#FAAD14")
                Text("毛团子会员")
                  .fontSize(22)
                  .fontWeight(FontWeight.Bold)
                  .fontColor(Colors.TEXT_PRIMARY)
                  .margin({ top: Spacing.S })
                Text("解锁专属风格 + 高清额度")
                  .fontSize(FontSizes.BODY_S)
                  .fontColor(Colors.TEXT_SECONDARY)
                  .margin({ top: 4 })

                // 当前会员状态（来自 /v1/vip/me）
                if (this.vipInfo && this.vipInfo.vipLevel > 0) {
                  Text(this.vipStatusText())
                    .fontSize(FontSizes.CAPTION)
                    .fontColor("#FAAD14")
                    .margin({ top: 6 })
                    .padding({ left: 10, right: 10, top: 3, bottom: 3 })
                    .backgroundColor("rgba(250,173,20,0.12)")
                    .borderRadius(12)
                }

                // 倒计时
                Row() {
                  Text("限时优惠 ")
                    .fontSize(FontSizes.CAPTION)
                    .fontColor(Colors.TEXT_SECONDARY)
                  Text(this.formatCountdown(this.countdownSec))
                    .fontSize(FontSizes.CAPTION)
                    .fontColor(Colors.WARNING)
                    .fontWeight(FontWeight.Bold)
                }
                .padding({ left: 12, right: 12, top: 6, bottom: 6 })
                .backgroundColor("rgba(250,173,20,0.1)")
                .borderRadius(12)
                .margin({ top: Spacing.S })
              }
              .width("100%")
              .padding(Spacing.XL)
              .alignItems(HorizontalAlign.Center)
              // 装饰圆
              Column()
                .width(80).height(80).borderRadius(40)
                .backgroundColor("rgba(250,173,20,0.08)")
                .position({ x: "80%", y: -20 })
            }
            .width("100%")
            .linearGradient({
              angle: 135,
              colors: [["rgba(255,240,220,1)"], ["rgba(255,235,220,1)"]]
            })
            .borderRadius(Radius.LG)
            .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.L })

            // 权益对比表
            Column() {
              Text("会员权益")
                .fontSize(FontSizes.H2)
                .fontWeight(FontWeight.Medium)
                .fontColor(Colors.TEXT_PRIMARY)
                .width("100%")
                .margin({ bottom: Spacing.M })

              // 表头
              Row() {
                Text("权益")
                  .fontSize(FontSizes.CAPTION)
                  .fontColor(Colors.TEXT_TERTIARY)
                  .fontWeight(FontWeight.Medium)
                  .layoutWeight(1)
                Text("免费")
                  .fontSize(FontSizes.CAPTION)
                  .fontColor(Colors.TEXT_TERTIARY)
                  .fontWeight(FontWeight.Medium)
                  .width(70)
                  .textAlign(TextAlign.Center)
                Text("季卡")
                  .fontSize(FontSizes.CAPTION)
                  .fontColor(Colors.PRIMARY)
                  .fontWeight(FontWeight.Bold)
                  .width(70)
                  .textAlign(TextAlign.Center)
                Text("年卡")
                  .fontSize(FontSizes.CAPTION)
                  .fontColor(Colors.TEXT_TERTIARY)
                  .fontWeight(FontWeight.Medium)
                  .width(70)
                  .textAlign(TextAlign.Center)
              }
              .width("100%")
              .padding({ left: Spacing.S, right: Spacing.S, bottom: Spacing.S })

              Divider().color(Colors.DIVIDER)

              // 表行
              ForEach(BENEFITS, (b: any, idx: number) => {
                Column() {
                  Row() {
                    Text(b.label)
                      .fontSize(FontSizes.BODY_S)
                      .fontColor(Colors.TEXT_PRIMARY)
                      .layoutWeight(1)
                    this.benefitCell(b.free)
                    this.benefitCell(b.quarter)
                    this.benefitCell(b.year)
                  }
                  .width("100%")
                  .padding({ top: 8, bottom: 8 })

                  if (idx < BENEFITS.length - 1) {
                    Divider().color(Colors.DIVIDER)
                  }
                }
              })
            }
            .padding(Spacing.L)
            .backgroundColor(Colors.BG_CARD)
            .borderRadius(Radius.LG)
            .shadow(shadowOf(Shadow.SM))
            .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.M })

            // 套餐选择
            Column() {
              Text("选择套餐")
                .fontSize(FontSizes.H2)
                .fontWeight(FontWeight.Medium)
                .fontColor(Colors.TEXT_PRIMARY)
                .width("100%")
                .margin({ bottom: Spacing.M })

              ForEach(this.products, (p: any) => {
                this.productCard(p)
              })
            }
            .padding({ left: Spacing.L, right: Spacing.L, top: Spacing.M })

            // 常见问题
            Column() {
              Text("常见问题")
                .fontSize(FontSizes.H2)
                .fontWeight(FontWeight.Medium)
                .fontColor(Colors.TEXT_PRIMARY)
                .width("100%")
                .margin({ bottom: Spacing.M })

              ForEach(FAQ_ITEMS, (item: any, idx: number) => {
                Column() {
                  Row() {
                    Text(`Q: ${item.q}`)
                      .fontSize(FontSizes.BODY_S)
                      .fontColor(Colors.TEXT_PRIMARY)
                      .fontWeight(FontWeight.Medium)
                      .layoutWeight(1)
                    Text(this.expandedFaq === idx ? "▲" : "▼")
                      .fontSize(12)
                      .fontColor(Colors.TEXT_TERTIARY)
                  }
                  .width("100%")
                  .padding({ top: Spacing.S, bottom: Spacing.S })
                  .onClick(() => {
                    animateTo({ duration: 200 }, () => {
                      this.expandedFaq = this.expandedFaq === idx ? -1 : idx
                    })
                  })

                  if (this.expandedFaq === idx) {
                    Text(item.a)
                      .fontSize(FontSizes.BODY_S)
                      .fontColor(Colors.TEXT_SECONDARY)
                      .width("100%")
                      .padding({ bottom: Spacing.S })
                      .transition(TransitionEffect.OPACITY.animation({ duration: 200 }))
                  }
                }
                .width("100%")

                if (idx < FAQ_ITEMS.length - 1) {
                  Divider().color(Colors.DIVIDER).margin({ top: Spacing.XS })
                }
              })
            }
            .padding(Spacing.L)
            .backgroundColor(Colors.BG_CARD)
            .borderRadius(Radius.LG)
            .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.M, bottom: Spacing.XL })
            .shadow(shadowOf(Shadow.SM))

            // 渠道选择 + 立即开通
            this.channelSelector()
            PrimaryButton({ text: this.paying ? "支付中..." : "立即开通", block: true })
              .onClick(() => {
                const p = this.products.find((x: any) => x.id === this.selectedId)
                if (p) this.onPurchase(p)
              })
              .margin({ left: Spacing.L, right: Spacing.L, bottom: Spacing.XXL })
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

  /** 渠道选择（华为为主、微信兜底） */
  @Builder channelSelector() {
    Row({ space: Spacing.S }) {
      this.channelPill("华为支付", "HUAWEI", $r("app.media.icon_crown"))
      this.channelPill("微信支付", "WECHAT", $r("app.media.icon_coupon"))
    }
    .width("100%")
    .padding({ left: Spacing.L, right: Spacing.L, bottom: Spacing.M })
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

  vipStatusText(): string {
    const level = this.vipInfo.vipLevel as number
    const name = ["普通", "月卡会员", "季卡会员", "年卡会员"][level] || "会员"
    const expire = this.vipInfo.vipExpireAt as string
    if (expire) {
      return `${name} · 有效期至 ${this.formatExpire(expire)}`
    }
    return `${name}`
  }

  formatExpire(iso: string): string {
    try {
      return new Date(iso).toLocaleDateString("zh-CN")
    } catch (e) {
      return iso
    }
  }

  @Builder benefitCell(value: any) {
    Row() {
      if (value === true) {
        Text("✓")
          .fontSize(14)
          .fontColor(Colors.SUCCESS)
      } else if (value === false) {
        Text("-")
          .fontSize(14)
          .fontColor(Colors.TEXT_TERTIARY)
      } else {
        Text(value as string)
          .fontSize(FontSizes.CAPTION)
          .fontColor(Colors.TEXT_SECONDARY)
          .textAlign(TextAlign.Center)
      }
    }
    .width(70)
    .justifyContent(FlexAlign.Center)
  }

  @Builder productCard(p: any) {
    Column() {
      Row() {
        // 左侧：名称+价格
        Column() {
          Row() {
            Text(p.name)
              .fontSize(FontSizes.BODY)
              .fontWeight(FontWeight.Medium)
              .fontColor(Colors.TEXT_PRIMARY)
            // 推荐标签
            if (p.highlight) {
              Text("推荐")
                .fontSize(FontSizes.TINY)
                .fontColor("#FFFFFF")
                .fontWeight(FontWeight.Bold)
                .padding({ left: 5, right: 5, top: 1, bottom: 1 })
                .backgroundColor(Colors.PRIMARY)
                .borderRadius(6)
                .margin({ left: 6 })
            }
          }
          Text(`¥${(p.priceCents / 100).toFixed(2)} / ${p.durationDays} 天`)
            .fontSize(FontSizes.CAPTION)
            .fontColor(Colors.TEXT_TERTIARY)
            .margin({ top: 2 })
        }
        .layoutWeight(1)
        .alignItems(HorizontalAlign.Start)

        // 右侧：划线价 + 现价
        Row() {
          if (p.originalPriceCents) {
            Text(`¥${(p.originalPriceCents / 100).toFixed(2)}`)
              .fontSize(FontSizes.CAPTION)
              .fontColor(Colors.TEXT_TERTIARY)
              .decoration({ type: TextDecorationType.LineThrough })
              .margin({ right: 6 })
          }
          Text(`¥${(p.priceCents / 100).toFixed(2)}`)
            .fontSize(20)
            .fontWeight(FontWeight.Bold)
            .fontColor(this.selectedId === p.id ? Colors.PRIMARY : Colors.TEXT_PRIMARY)
        }
      }
    }
    .width("100%")
    .padding(Spacing.M)
    .backgroundColor(this.selectedId === p.id ? "rgba(255,182,193,0.12)" : Colors.BG_CARD)
    .borderRadius(Radius.MD)
    .border(this.selectedId === p.id ? { width: 2, color: Colors.PRIMARY } : { width: 1, color: Colors.DIVIDER })
    .shadow(this.selectedId === p.id ? shadowOf(Shadow.MD) : shadowOf(Shadow.SM))
    .margin({ bottom: Spacing.S })
    .onClick(() => {
      animateTo({ duration: 200 }, () => {
        this.selectedId = p.id
      })
    })
  }
}

/**
 * AI 创作 Tab
 */
import router from "@ohos.router";
import { Colors, FontSizes, Spacing, Radius, Shadow, shadowOf } from "../../common/Constants";
import { GradientPlaceholder } from "../../components/CommonComponents";
import { SparkleIcon, CrownIcon, MagicIcon } from "../../components/icons/Icons";

interface StyleItem {
  key: string
  name: string
  desc: string
  fromColor: string
  toColor: string
  vipOnly: boolean
  tag: string
}

@Component
export struct AiTab {
  @State selectedTab: string = "2d"
  @State selectedStyle: string = ""
  private styles: StyleItem[] = [
    { key: 'oil',  name: '油画',  desc: '经典优雅', fromColor: '#FFD1DC', toColor: '#FFB6C1', vipOnly: true,  tag: '经典' },
    { key: 'disn', name: '迪士尼', desc: '可爱萌系', fromColor: '#FFD3B6', toColor: '#FFAA80', vipOnly: false, tag: '热卖' },
    { key: 'wcol', name: '水彩',  desc: '清新手绘', fromColor: '#C6E2FF', toColor: '#B5EAD7', vipOnly: false, tag: '清新' },
    { key: 'cybr', name: '赛博',  desc: '未来霓虹', fromColor: '#C8B6FF', toColor: '#9D7BFF', vipOnly: true,  tag: '潮酷' },
    { key: 'cn',   name: '国风',  desc: '水墨意境', fromColor: '#E0C3FC', toColor: '#C8B6FF', vipOnly: true,  tag: '国风' },
    { key: 'anim', name: '动漫',  desc: '二次元',   fromColor: '#FFE9A0', toColor: '#FFD93D', vipOnly: true,  tag: '二次元' }
  ]

  build() {
    Scroll() {
      Column() {
        // 顶部 Tab + 下划线指示器
        Column() {
          Row() {
            this.headerTab("2D 画像", "2d")
            this.headerTab("3D 手办", "3d")
            this.headerTab("表情包", "emoji")
            this.headerTab("证件卡", "card")
            this.headerTab("文案", "text")
          }
          .width("100%")
          .padding({ left: Spacing.L, right: Spacing.L, top: Spacing.S })

          // 下划线指示器
          Row() {
            this.tabIndicator()
          }
          .width("100%")
          .padding({ left: Spacing.L })
        }
        .backgroundColor(Colors.BG_CARD)

        // 顶部 Banner：渐变 + 阴影 + 图标
        Stack() {
          Column() {
            Row() {
              SparkleIcon({ size: 18, color: "#FFFFFF" })
                .margin({ right: 6 })
              Text("用 AI 创造你的毛孩子")
                .fontSize(20)
                .fontWeight(FontWeight.Bold)
                .fontColor("#FFFFFF")
            }
            Text("选择风格、上传照片，秒变艺术品")
              .fontSize(FontSizes.BODY_S)
              .fontColor("rgba(255,255,255,0.88)")
              .margin({ top: 6 })
          }
          .width("100%")
          .height(100)
          .padding(Spacing.L)
          .alignItems(HorizontalAlign.Start)
          .justifyContent(FlexAlign.Center)
          // 右上角装饰圆
          Column()
            .width(60).height(60).borderRadius(30)
            .backgroundColor("rgba(255,255,255,0.18)")
            .position({ x: "78%", y: -10 })
        }
        .linearGradient({
          angle: 135,
          colors: [[0xffB6C1], [0xffAA80]]
        })
        .width("calc(100% - 32vp)")
        .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.M })
        .borderRadius(Radius.LG)
        .shadow(shadowOf(Shadow.MD))

        // 风格列表（按 selectedTab 真实驱动）
        Column() {
          if (this.selectedTab === '2d') {
            Text("热门风格")
              .fontSize(FontSizes.H2)
              .fontWeight(FontWeight.Medium)
              .fontColor(Colors.TEXT_PRIMARY)
              .width("100%")
              .margin({ bottom: Spacing.M })

            Grid() {
              ForEach(this.styles, (s: StyleItem) => {
                GridItem() {
                  this.styleCard(s)
                }
              })
            }
            .columnsTemplate("1fr 1fr 1fr")
            .columnsGap(Spacing.M)
            .rowsGap(Spacing.M)
            .width("100%")
          } else if (this.selectedTab === '3d') {
            this.comingSoonBlock("3D 萌宠数字手办", "上传照片生成可旋转 3D 模型", true)
          } else if (this.selectedTab === 'emoji') {
            this.comingSoonBlock("表情包", "一键生成萌宠表情", false)
          } else if (this.selectedTab === 'card') {
            this.comingSoonBlock("证件卡", "专属萌宠身份证", false)
          } else {
            this.comingSoonBlock("文案", "AI 生成萌宠朋友圈文案", false)
          }
        }
        .padding({ left: Spacing.L, right: Spacing.L, top: Spacing.M })

        // 上传入口
        Column() {
          Button("立即上传 →")
            .width("100%")
            .height(48)
            .fontSize(FontSizes.BODY)
            .fontColor(Colors.TEXT_WHITE)
            .backgroundColor(Colors.PRIMARY)
            .borderRadius(Radius.MD)
            .onClick(() => router.pushUrl({ url: "pages/ai/Upload" }))
          Text("支持 JPG / PNG，单张 ≤ 10MB")
            .fontSize(FontSizes.CAPTION)
            .fontColor(Colors.TEXT_TERTIARY)
            .margin({ top: Spacing.S })
        }
        .padding({ left: Spacing.L, right: Spacing.L, top: Spacing.XL, bottom: Spacing.XL })
      }
      .width("100%")
    }
    .scrollBar(BarState.Off)
  }

  @Builder tabIndicator() {
    // Tab 下划线指示器（跟随选中态）
    Stack() {
      Row() {
        Blank()
      }
      .width("100%")

      Row() {
        Column()
          .width(this.underlineWidth())
          .height(3)
          .borderRadius(2)
          .backgroundColor(Colors.PRIMARY)
          .transition(TransitionEffect.translate({ x: this.underlineOffset() }).animation({ duration: 250, curve: Curve.EaseOut }))
      }
    }
    .height(3)
  }

  underlineWidth(): string {
    return "72vp"
  }

  underlineOffset(): number {
    const tabs = ["2d", "3d", "emoji", "card", "text"]
    const idx = tabs.indexOf(this.selectedTab)
    const tabWidth = 72
    return idx * (tabWidth + Spacing.S)
  }

  @Builder headerTab(label: string, key: string) {
    Column() {
      Text(label)
        .fontSize(FontSizes.BODY_S)
        .fontWeight(this.selectedTab === key ? FontWeight.Bold : FontWeight.Normal)
        .fontColor(this.selectedTab === key ? Colors.PRIMARY : Colors.TEXT_SECONDARY)
        .padding({ left: Spacing.M, right: Spacing.M, top: Spacing.S, bottom: Spacing.S })
        .onClick(() => {
          animateTo({ duration: 200 }, () => {
            this.selectedTab = key
          })
        })
    }
    .margin({ right: Spacing.S })
    .alignItems(HorizontalAlign.Center)
  }

  @Builder styleCard(s: StyleItem) {
    Column() {
      Stack() {
        // 渐变占位 + 图标风格预览
        Column() {
          SparkleIcon({ size: 32, color: "#FFFFFF" })
            .margin({ top: 12 })
          Text(s.name)
            .fontSize(FontSizes.BODY_S)
            .fontColor(Colors.TEXT_PRIMARY)
            .fontWeight(FontWeight.Medium)
            .margin({ top: 4 })
        }
        .width("100%")
        .height("100%")
        .aspectRatio(1)
        .linearGradient({
          angle: 135,
          colors: [[s.fromColor], [s.toColor]]
        })
        .borderRadius(Radius.MD)
        .justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)

        // 标签
        Row() {
          Text(s.tag)
            .fontSize(FontSizes.TINY)
            .fontColor(Colors.TEXT_PRIMARY)
            .fontWeight(FontWeight.Medium)
        }
        .padding({ left: 6, right: 6, top: 2, bottom: 2 })
        .backgroundColor("rgba(255,255,255,0.85)")
        .borderRadius(8)
        .position({ x: 4, y: 4 })

        // VIP 角标 + 皇冠
        if (s.vipOnly) {
          Row() {
            CrownIcon({ size: 12, color: "#FAAD14" })
            Text("VIP")
              .fontSize(FontSizes.TINY)
              .fontColor("#FAAD14")
              .margin({ left: 2 })
          }
          .padding({ left: 4, right: 4, top: 2, bottom: 2 })
          .backgroundColor("rgba(255,255,255,0.92)")
          .borderRadius(8)
          .position({ x: 4, y: 28 })
        }

        // 选中态边框
        if (this.selectedStyle === s.key) {
          Column()
            .width("100%")
            .height("100%")
            .border({ width: 2.5, color: Colors.PRIMARY })
            .borderRadius(Radius.MD)
        }
      }
      .width("100%")
      .scale({ x: 1, y: 1 })
      .transition(TransitionEffect.scale({ x: 0.92, y: 0.92 }).animation({ duration: 150 }))
      .stateStyles({
        pressed: {
          Column() { }
            .scale({ x: 0.95, y: 0.95 })
        },
        normal: {
          Column() { }
            .scale({ x: 1, y: 1 })
        }
      })

      Text(s.desc)
        .fontSize(FontSizes.TINY)
        .fontColor(Colors.TEXT_TERTIARY)
        .margin({ top: 4 })
    }
    .width("100%")
    .onClick(() => {
      animateTo({ duration: 150 }, () => {
        this.selectedStyle = this.selectedStyle === s.key ? "" : s.key
      })
      router.pushUrl({ url: "pages/ai/StyleSelect", params: { styleKey: s.key } })
    })
  }

  /** 诚实区块：未上线的能力（3D 可跳转上传，其余标注开发中） */
  @Builder comingSoonBlock(title: string, desc: string, go3d: boolean) {
    Column() {
      GradientPlaceholder({ label: '', fromColor: '#FFE4E1', toColor: '#FFE4B5' })
        .height(140)
        .margin({ bottom: Spacing.M })

      Text(title)
        .fontSize(FontSizes.H2)
        .fontWeight(FontWeight.Medium)
        .fontColor(Colors.TEXT_PRIMARY)
        .margin({ bottom: Spacing.XS })

      Text(desc)
        .fontSize(FontSizes.BODY_S)
        .fontColor(Colors.TEXT_SECONDARY)
        .margin({ bottom: Spacing.M })

      if (go3d) {
        Button("去生成 3D")
          .width("100%")
          .height(44)
          .fontSize(FontSizes.BODY)
          .fontColor(Colors.TEXT_WHITE)
          .backgroundColor(Colors.PRIMARY)
          .borderRadius(Radius.MD)
          .onClick(() => router.pushUrl({ url: "pages/ai/Upload", params: { mode: "3d" } }))
      } else {
        Text("功能开发中，敬请期待")
          .fontSize(FontSizes.BODY_S)
          .fontColor(Colors.TEXT_TERTIARY)
          .padding({ top: Spacing.S, bottom: Spacing.S })
      }
    }
    .width("100%")
    .padding(Spacing.L)
    .backgroundColor(Colors.BG_CARD)
    .borderRadius(Radius.MD)
  }
}

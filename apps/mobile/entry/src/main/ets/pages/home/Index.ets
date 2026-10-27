/**
 * 首页 Tab
 * - 顶部问候 + Swiper 轮播 Banner（真实宠物图）
 * - AI 工具宫格：4 列，每格 56×56 渐变色块 + 白色图标
 * - 养宠工具区（列表）
 * - 推荐流（真实宠物图片 + ImagePreview 预览）
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing, Radius, Shadow, shadowOf } from "../../common/Constants";
import { Card, GradientPlaceholder, RemoteImage } from "../../components/CommonComponents";
import { ApiService } from "../../service/ApiService";
import { MagicIcon, PaintIcon, SparkleIcon, HeartPulseIcon, CrownIcon, CameraIcon, ChartIcon, BellIcon, ChevronRightIcon } from "../../components/icons/Icons";

// Banner 数据（真实宠物图片 + 活动文案）
interface BannerItem {
  id: number
  imageUrl: string
  title: string
  subtitle: string
  actionText: string
  tagText: string
  actionUrl: string
}

const BANNERS: BannerItem[] = [
  {
    id: 1,
    imageUrl: "https://images.unsplash.com/photo-1514888451691-3a165f5f62e4?w=800&q=80",
    title: "限时活动",
    subtitle: "首单 1 分钱体验 AI 画像",
    actionText: "立即领取 →",
    tagText: "热卖",
    actionUrl: "pages/ai/Upload"
  },
  {
    id: 2,
    imageUrl: "https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=800&q=80",
    title: "新品上线",
    subtitle: "3D 手办模型免费体验",
    actionText: "去试试 →",
    tagText: "NEW",
    actionUrl: "pages/ai/Upload"
  },
  {
    id: 3,
    imageUrl: "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=800&q=80",
    title: "萌宠广场",
    subtitle: "分享你家毛孩子的可爱瞬间",
    actionText: "去逛逛 →",
    tagText: "精选",
    actionUrl: "pages/square/Index"
  }
]

// 精选作品数据（真实宠物图）
interface WorkItem {
  id: number
  title: string
  style: string
  author: string
  likes: number
  imageUrl: string
  previewUrl: string
}

const WORKS: WorkItem[] = [
  { id: 1, title: "橘猫橘橘", style: "油画风", author: "团子妈", likes: 328, imageUrl: "https://images.unsplash.com/photo-1514888451691-3a165f5f62e4?w=400&q=80", previewUrl: "https://images.unsplash.com/photo-1514888451691-3a165f5f62e4?w=1200&q=90" },
  { id: 2, title: "柴犬阿福", style: "迪士尼风", author: "铲屎官阿明", likes: 256, imageUrl: "https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=400&q=80", previewUrl: "https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=1200&q=90" },
  { id: 3, title: "英短豆豆", style: "水彩风", author: "猫咪奴小雅", likes: 189, imageUrl: "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=400&q=80", previewUrl: "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=1200&q=90" },
  { id: 4, title: "金毛Leo", style: "动漫风", author: "养狗日记", likes: 412, imageUrl: "https://images.unsplash.com/photo-1518717758536-85ae29035b6d?w=400&q=80", previewUrl: "https://images.unsplash.com/photo-1518717758536-85ae29035b6d?w=1200&q=90" }
]

@Component
export struct HomeTab {
  @State petName: string = "毛团子"
  @State greeting: string = ""
  @State currentBanner: number = 0
  @State activeTab: string = "recommend"
  @State recommendWorks: WorkItem[] = []
  @State latestWorks: WorkItem[] = []
  @State feedLoaded: boolean = false

  aboutToAppear() {
    this.updateGreeting()
    this.loadFeed()
  }

  /**
   * 加载广场帖子，映射为推荐流 WorkItem
   * recommendWorks 用原始顺序；latestWorks 按 createdAt 倒序
   * 无论成功失败都兜底为硬编码 WORKS，保证离线有内容
   */
  async loadFeed() {
    try {
      const data: any = await ApiService.listPosts('', 1, 8)
      const list: any[] = data?.list || []
      if (list.length > 0) {
        const mapped: WorkItem[] = list.map((item: any) => ({
          id: item.id,
          title: item.title || '萌宠作品',
          style: item.styleName || 'AI 作品',
          author: item.authorName || '毛团子用户',
          likes: item.likeCount || 0,
          imageUrl: item.coverUrl || item.previewUrl || '',
          previewUrl: item.previewUrl || item.coverUrl || ''
        }))
        this.recommendWorks = mapped
        this.latestWorks = mapped.slice().sort((a: WorkItem, b: WorkItem) => {
          return new Date(b.createdAt ? b.createdAt : 0).getTime() - new Date(a.createdAt ? a.createdAt : 0).getTime()
        })
      } else {
        this.recommendWorks = WORKS
        this.latestWorks = WORKS
      }
    } catch (e: any) {
      this.recommendWorks = WORKS
      this.latestWorks = WORKS
    } finally {
      this.feedLoaded = true
    }
  }

  /** 当前 Tab 对应的作品列表（联网显示真实，离线兜底 mock） */
  get displayWorks(): WorkItem[] {
    if (this.activeTab === 'latest') {
      return this.latestWorks.length > 0 ? this.latestWorks : WORKS
    }
    return this.recommendWorks.length > 0 ? this.recommendWorks : WORKS
  }

  updateGreeting() {
    const hour = new Date().getHours()
    if (hour >= 5 && hour < 11) {
      this.greeting = "早上好"
    } else if (hour >= 11 && hour < 14) {
      this.greeting = "中午好"
    } else if (hour >= 14 && hour < 18) {
      this.greeting = "下午好"
    } else {
      this.greeting = "晚上好"
    }
  }

  build() {
    Scroll() {
      Column() {
        // ====== 顶部问候 ======
        Column() {
          Row() {
            Column() {
              Row() {
                Text(this.greeting)
                  .fontSize(FontSizes.CAPTION)
                  .fontColor(Colors.TEXT_SECONDARY)
                SparkleIcon({ size: 12, color: Colors.TEXT_SECONDARY })
                  .margin({ left: 4 })
              }
              Text(this.petName)
                .fontSize(FontSizes.H2)
                .fontWeight(FontWeight.Medium)
                .fontColor(Colors.TEXT_PRIMARY)
                .margin({ top: 2 })
            }
            .layoutWeight(1)
            .alignItems(HorizontalAlign.Start)

            // 通知铃铛
            BellIcon({ size: 24, color: Colors.TEXT_PRIMARY })
              .onClick(() => router.pushUrl({ url: "pages/me/Notifications" }))
          }
          .width("100%")
          .margin({ bottom: Spacing.M })

          // ====== Swiper 轮播 Banner ======
          Stack({ alignContent: Alignment.BottomStart }) {
            // 轮播内容
            Swiper() {
              ForEach(BANNERS, (banner: BannerItem, idx: number) => {
                this.bannerCard(banner, idx)
              })
            }
            .autoPlay(true)
            .interval(4000)
            .indicator(false)
            .loop(true)
            .duration(300)
            .curve(Curve.EaseOut)
            .onChange((idx: number) => {
              animateTo({ duration: 150 }, () => {
                this.currentBanner = idx
              })
            })

            // 指示器点（自定义）
            Row({ space: 6 }) {
              ForEach(BANNERS, (banner: BannerItem, idx: number) => {
                Column()
                  .width(idx === this.currentBanner ? 16 : 6)
                  .height(6)
                  .borderRadius(3)
                  .backgroundColor(idx === this.currentBanner ? "#FFFFFF" : "rgba(255,255,255,0.5)")
                  .transition(idx === this.currentBanner ? TransitionEffect.OPACITY.animation({ duration: 200 }) : TransitionEffect.OPACITY)
              })
            }
            .margin({ left: Spacing.L, bottom: 12 })
          }
          .width("100%")
          .height(160)
          .shadow(shadowOf(Shadow.MD))
        }
        .padding({ left: Spacing.L, right: Spacing.L, top: Spacing.L })

        // ====== AI 工具宫格 ======
        Column() {
          // Tab 切换 + 下划线指示器
          Row() {
            this.homeTab("推荐", "recommend")
            this.homeTab("最新", "latest")
          }
          .width("100%")
          .margin({ bottom: Spacing.M })

          Grid() {
            GridItem() {
              this.toolBlock("AI 画像", "上传照片秒变手办", "#FFFFFF", "#FFB6C1", "#FF8FA8", $r("app.media.icon_magic"), () => {
                router.pushUrl({ url: "pages/ai/Upload" })
              })
            }
            GridItem() {
              this.toolBlock("3D 手办", "单图生成互动模型", "#FFFFFF", "#FFAA80", "#FF8A5C", $r("app.media.icon_sparkle"), () => {
                router.pushUrl({ url: "pages/ai/Upload" })
              })
            }
            GridItem() {
              this.toolBlock("表情包", "一键生成萌宠表情", "#FFFFFF", "#FFD3B6", "#FFB088", $r("app.media.icon_face"), () => {
                router.pushUrl({ url: "pages/ai/Upload" })
              })
            }
            GridItem() {
              this.toolBlock("证件卡", "专属萌宠身份证", "#FFFFFF", "#FFC8D0", "#FFA0B0", $r("app.media.icon_certificate"), () => {
                router.pushUrl({ url: "pages/ai/Upload" })
              })
            }
          }
          .columnsTemplate("1fr 1fr 1fr 1fr")
          .columnsGap(Spacing.S)
          .rowsGap(Spacing.M)
          .width("100%")
        }
        .padding({ left: Spacing.L, right: Spacing.L, top: Spacing.M })

        // ====== 养宠工具区（列表）======
        Column() {
          Text("养宠工具")
            .fontSize(FontSizes.H2)
            .fontWeight(FontWeight.Medium)
            .fontColor(Colors.TEXT_PRIMARY)
            .width("100%")
            .margin({ bottom: Spacing.M })

          Card({ elevation: 'sm' }) {
            Column() {
              this.utilItem("症状查询", "宠物不舒服先来查查", $r("app.media.icon_heart_pulse"), () => {
                router.pushUrl({ url: "pages/tools/Symptom" })
              })
              Divider().color(Colors.DIVIDER)
              this.utilItem("喂养方案", "AI 定制专属方案", $r("app.media.icon_chart"), () => {
                router.pushUrl({ url: "pages/tools/FeedingPlan" })
              })
              Divider().color(Colors.DIVIDER)
              this.utilItem("健康提醒", "驱虫疫苗不忘记", $r("app.media.icon_bell"), () => {
                router.pushUrl({ url: "pages/tools/Reminders" })
              })
            }
          }
          .padding(0)
        }
        .padding({ left: Spacing.L, right: Spacing.L, top: Spacing.M })

        // ====== 推荐流（真实宠物图片）======
        Column() {
          Row() {
            Text("萌宠广场 · 精选")
              .fontSize(FontSizes.H2)
              .fontWeight(FontWeight.Medium)
              .fontColor(Colors.TEXT_PRIMARY)
            Blank()
            Text("更多")
              .fontSize(FontSizes.BODY_S)
              .fontColor(Colors.PRIMARY)
              .onClick(() => router.pushUrl({ url: "pages/square/Index" }))
          }
          .width("100%")
          .margin({ bottom: Spacing.M })

          Grid() {
            ForEach(this.displayWorks, (work: WorkItem) => {
              GridItem() {
                this.workCard(work)
              }
            }, (work: WorkItem) => work.id?.toString())
          }
          .columnsTemplate("1fr 1fr")
          .columnsGap(Spacing.M)
          .rowsGap(Spacing.M)
          .width("100%")
        }
        .padding({ left: Spacing.L, right: Spacing.L, top: Spacing.XL, bottom: Spacing.L })
      }
      .width("100%")
    }
    .scrollBar(BarState.Off)
    .edgeEffect(EdgeEffect.Spring)
  }

  @Builder homeTab(label: string, key: string) {
    Column() {
      Text(label)
        .fontSize(FontSizes.BODY)
        .fontWeight(this.activeTab === key ? FontWeight.Bold : FontWeight.Medium)
        .fontColor(this.activeTab === key ? Colors.PRIMARY : Colors.TEXT_SECONDARY)
        .onClick(() => {
          animateTo({ duration: 200 }, () => {
            this.activeTab = key
          })
        })
      // 下划线指示器
      Column()
        .width(this.activeTab === key ? 24 : 0)
        .height(3)
        .borderRadius(2)
        .backgroundColor(Colors.PRIMARY)
        .transition(this.activeTab === key ? TransitionEffect.OPACITY.animation({ duration: 200 }) : TransitionEffect.OPACITY)
    }
    .padding({ left: Spacing.M, right: Spacing.M, top: Spacing.XS, bottom: Spacing.XS })
    .alignItems(HorizontalAlign.Center)
  }

  @Builder bannerCard(banner: BannerItem, idx: number) {
    Column() {
      Stack() {
        // 背景图片
        Image(banner.imageUrl)
          .width("100%")
          .height("100%")
          .objectFit(ImageFit.Cover)

        // 渐变遮罩
        Column()
          .width("100%")
          .height("100%")
          .linearGradient({
            angle: 180,
            colors: [["rgba(0,0,0,0.05)"], ["rgba(0,0,0,0.55)"]]
          })

        // 内容
        Column() {
          Row() {
            Text(banner.tagText)
              .fontSize(FontSizes.TINY)
              .fontColor("#FFFFFF")
              .fontWeight(FontWeight.Medium)
              .padding({ left: 8, right: 8, top: 3, bottom: 3 })
              .backgroundColor(banner.id === 1 ? Colors.TAG_HOT : banner.id === 2 ? Colors.TAG_NEW : Colors.TAG_FEAT)
              .borderRadius(10)
              .margin({ right: 8 })
            Text(banner.title)
              .fontSize(FontSizes.CAPTION)
              .fontColor("rgba(255,255,255,0.9)")
          }
          Text(banner.subtitle)
            .fontSize(18)
            .fontWeight(FontWeight.Bold)
            .fontColor("#FFFFFF")
            .margin({ top: 6 })
          Text(banner.actionText)
            .fontSize(FontSizes.BODY_S)
            .fontColor("rgba(255,255,255,0.9)")
            .margin({ top: 10 })
            .padding({ left: 14, right: 14, top: 5, bottom: 5 })
            .backgroundColor("rgba(255,255,255,0.2)")
            .borderRadius(12)
        }
        .width("100%")
        .height("100%")
        .padding(Spacing.L)
        .alignItems(HorizontalAlign.Start)
        .justifyContent(FlexAlign.End)
      }
      .width("100%")
      .height(160)
      .borderRadius(Radius.LG)
      .clip(true)
      .onClick(() => {
        // 涟漪点击效果
        router.pushUrl({ url: banner.actionUrl })
      })
    }
  }

  @Builder toolBlock(label: string, desc: string, iconColor: string, from: string, to: string, icon: Resource, onTap: () => void) {
    Column() {
      Column() {
        Image(icon)
          .width(28)
          .height(28)
          .fillColor(iconColor)
      }
      .width(48)
      .height(48)
      .borderRadius(14)
      .linearGradient({
        angle: 135,
        colors: [[from], [to]]
      })
      .justifyContent(FlexAlign.Center)
      .alignItems(HorizontalAlign.Center)
      .shadow(shadowOf(Shadow.SM))

      Text(label)
        .fontSize(FontSizes.BODY_S)
        .fontColor(Colors.TEXT_PRIMARY)
        .fontWeight(FontWeight.Medium)
        .margin({ top: 8 })
      Text(desc)
        .fontSize(FontSizes.TINY)
        .fontColor(Colors.TEXT_TERTIARY)
        .margin({ top: 2 })
        .maxLines(1)
        .textOverflow({ overflow: TextOverflow.Ellipsis })
    }
    .width("100%")
    .padding({ top: Spacing.S, bottom: Spacing.M })
    .backgroundColor(Colors.BG_CARD)
    .borderRadius(Radius.MD)
    .justifyContent(FlexAlign.Center)
    .alignItems(HorizontalAlign.Center)
    .onClick(() => onTap())
    .scale({ x: 1, y: 1 })
    .transition(TransitionEffect.OPACITY.animation({ duration: 150 }).combine(
      TransitionEffect.scale({ x: 0.95, y: 0.95 }).animation({ duration: 150 })
    ))
    .stateStyles({
      pressed: {
        Column() { }.transition(TransitionEffect.scale({ x: 0.95, y: 0.95 }).animation({ duration: 100 }))
          .scale({ x: 0.95, y: 0.95 })
      },
      normal: {
        Column() { }.transition(TransitionEffect.scale({ x: 1, y: 1 }).animation({ duration: 150 }))
          .scale({ x: 1, y: 1 })
      }
    })
  }

  @Builder utilItem(label: string, desc: string, icon: Resource, onTap: () => void) {
    Row() {
      Column() {
        Image(icon)
          .width(20).height(20)
          .fillColor("#FFFFFF")
      }
      .width(32).height(32)
      .borderRadius(8)
      .backgroundColor(Colors.PRIMARY_LIGHT)
      .justifyContent(FlexAlign.Center)
      .alignItems(HorizontalAlign.Center)
      .margin({ right: Spacing.M })

      Column() {
        Text(label)
          .fontSize(FontSizes.BODY)
          .fontColor(Colors.TEXT_PRIMARY)
        Text(desc)
          .fontSize(FontSizes.CAPTION)
          .fontColor(Colors.TEXT_TERTIARY)
          .margin({ top: 2 })
      }
      .layoutWeight(1)
      .alignItems(HorizontalAlign.Start)

      ChevronRightIcon({ size: 16, color: Colors.TEXT_QUATERNARY })
    }
    .width("100%")
    .height(56)
    .padding({ left: Spacing.M, right: Spacing.M })
    .alignItems(VerticalAlign.Center)
    .onClick(() => onTap())
  }

  @Builder workCard(work: WorkItem) {
    Column() {
      // 真实图片，点击预览大图
      Stack({ alignContent: Alignment.TopEnd }) {
        Image(work.imageUrl)
          .width("100%")
          .aspectRatio(1)
          .objectFit(ImageFit.Cover)
          .borderRadius(Radius.SM)
          .scale({ x: 1, y: 1 })
          .onClick(() => {
            router.pushUrl({ url: "pages/common/ImagePreview", params: { url: work.previewUrl } })
          })
          .stateStyles({
            pressed: {
              Image() { }
                .scale({ x: 0.95, y: 0.95 })
            },
            normal: {
              Image() { }
                .scale({ x: 1, y: 1 })
            }
          })

        // 点赞数
        Row() {
          HeartPulseIcon({ size: 11, color: "#FFFFFF" })
            .margin({ right: 2 })
          Text(`${work.likes}`)
            .fontSize(FontSizes.TINY)
            .fontColor("#FFFFFF")
        }
        .padding({ left: 5, right: 5, top: 3, bottom: 3 })
        .backgroundColor("rgba(0,0,0,0.4)")
        .borderRadius(8)
        .margin({ right: 4, top: 4 })
      }

      Text(work.title)
        .fontSize(FontSizes.CAPTION)
        .fontColor(Colors.TEXT_PRIMARY)
        .fontWeight(FontWeight.Medium)
        .margin({ top: 6 })
        .width("100%")
        .maxLines(1)
        .textOverflow({ overflow: TextOverflow.Ellipsis })

      Text(`${work.style} · ${work.author}`)
        .fontSize(FontSizes.TINY)
        .fontColor(Colors.TEXT_TERTIARY)
        .margin({ top: 2 })
        .width("100%")
        .maxLines(1)
        .textOverflow({ overflow: TextOverflow.Ellipsis })
    }
    .width("100%")
  }
}

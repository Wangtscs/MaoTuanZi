/**
 * 广场 Tab
 *
 * 阶段4/5：
 *  - 分享按钮接 ShareSheet.showShareMenu（分享后上报 shareRecord + 奖励 toast）；
 *  - tab 参数语义对齐：latest / hottest / 3d_zone / challenge → 传给 listPostsByTab；
 *  - 3D 专区卡片点击进入 pages/ai/Model3D 预览（modelUrl）；
 *  - 字段映射容错：SquarePost 无 styleTag/imageUrl 单数字段，前端用 imageUrls[0] 兜底、不崩溃。
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing, Radius, Shadow, shadowOf } from "../../common/Constants";
import { GradientPlaceholder } from "../../components/CommonComponents";
import { ApiService } from "../../service/ApiService";
import { showShareMenu } from "../../utils/ShareSheet";
import { HeartFilledIcon, CommentIcon, PlusIcon, ShareIcon } from "../../components/icons/Icons";

// 渐变色板（按 post.id 派生一个，避免全是同色）
const SQUARE_PALETTE: Array<[string, string]> = [
  ['#FFE4E1', '#FFE4B5'],
  ['#FFD6E0', '#FFCBA4'],
  ['#FFEFD5', '#FFDAB9'],
  ['#E6E6FA', '#FFD1DC'],
  ['#FFE0E5', '#FFC1CC'],
  ['#FFF0F5', '#FFD1DC']
]

// 展示 Tab key（UI 用） → 后端 tab 参数（契约值）
const TAB_PARAM_MAP: Record<string, string> = {
  "latest": "latest",
  "hot": "hottest",
  "3d": "3d_zone",
  "challenge": "challenge"
}

@Component
export struct SquareTab {
  @State selectedTab: string = "latest"
  @State posts: any[] = []
  @State loading: boolean = false
  @State loadError: boolean = false
  @State page: number = 1
  @State hasMore: boolean = true

  aboutToAppear() {
    this.loadPosts(true)
  }

  /** 展示 tab → 后端 tab 参数（缺省回退 latest） */
  tabParam(): string {
    return TAB_PARAM_MAP[this.selectedTab] || "latest"
  }

  async loadPosts(reset: boolean = false) {
    if (reset) { this.page = 1; this.posts = []; this.hasMore = true; this.loadError = false }
    if (!this.hasMore) return
    this.loading = true
    try {
      const data: any = await ApiService.listPostsByTab(this.tabParam(), this.page, 20)
      const list = data?.list || []
      this.posts = reset ? list : this.posts.concat(list)
      this.hasMore = data?.pagination?.hasMore || false
      this.page += 1
    } catch (e: any) {
      this.loadError = true
      promptAction.showToast({ message: "加载失败，请下拉重试", duration: 1500 })
    } finally {
      this.loading = false
    }
  }

  build() {
    Stack() {
      Column() {
        // 顶部 Tab + 下划线指示器
        Column() {
          Row() {
            this.headerTab("最新", "latest")
            this.headerTab("最热", "hot")
            this.headerTab("3D 专区", "3d")
            this.headerTab("挑战", "challenge")
          }
          .width("100%")
          .padding({ left: Spacing.L, top: Spacing.S })

          // 下划线指示器
          Row() {
            Column()
              .width(this.squareTabWidth())
              .height(3)
              .borderRadius(2)
              .backgroundColor(Colors.PRIMARY)
              .transition(TransitionEffect.translate({ x: this.squareTabOffset() }).animation({ duration: 220, curve: Curve.EaseOut }))
          }
          .width("100%")
          .padding({ left: Spacing.L })
        }
        .backgroundColor(Colors.BG_CARD)

        // 内容区
        if (this.loading && this.posts.length === 0) {
          this.skeletonGrid()
        } else if (this.posts.length === 0 && !this.loading) {
          if (this.loadError) {
            Column() {
              Text("加载失败")
                .fontSize(FontSizes.BODY)
                .fontColor(Colors.TEXT_PRIMARY)
                .margin({ bottom: Spacing.S })
              Text("点击重试")
                .fontSize(FontSizes.BODY_S)
                .fontColor(Colors.PRIMARY)
                .onClick(() => this.loadPosts(true))
            }
            .width("100%")
            .padding(Spacing.XXL)
            .alignItems(HorizontalAlign.Center)
            .justifyContent(FlexAlign.Center)
            .layoutWeight(1)
          } else {
            // 情感化空状态
            Column() {
              Column() {
                Row() {
                  Column()
                    .width(40).height(40)
                    .borderRadius(12)
                    .backgroundColor("rgba(255,182,193,0.3)")
                    .justifyContent(FlexAlign.Center)
                    .alignItems(HorizontalAlign.Center)
                    .margin({ right: 8 })
                  Column() {
                    Column()
                      .width(28).height(28)
                      .borderRadius(8)
                      .backgroundColor("rgba(255,170,128,0.3)")
                  }
                }
              }
              .margin({ bottom: Spacing.XL })

              Text("还没有人发布内容")
                .fontSize(FontSizes.BODY)
                .fontColor(Colors.TEXT_PRIMARY)
                .fontWeight(FontWeight.Medium)
                .margin({ bottom: Spacing.XS })
              Text("快来分享你家毛孩子的可爱瞬间吧")
                .fontSize(FontSizes.BODY_S)
                .fontColor(Colors.TEXT_TERTIARY)
                .textAlign(TextAlign.Center)

              Button("发布第一篇")
                .height(40)
                .fontSize(FontSizes.BODY_S)
                .fontColor(Colors.TEXT_WHITE)
                .backgroundColor(Colors.PRIMARY)
                .borderRadius(Radius.MD)
                .padding({ left: Spacing.XL, right: Spacing.XL })
                .margin({ top: Spacing.L })
                .onClick(() => router.pushUrl({ url: "pages/square/PostCreate" }))
            }
            .width("100%")
            .padding(Spacing.XXL)
            .alignItems(HorizontalAlign.Center)
            .justifyContent(FlexAlign.Center)
            .layoutWeight(1)
          }
        } else {
          Scroll() {
            Column() {
              Grid() {
                ForEach(this.posts, (post: any, idx?: number) => {
                  GridItem() {
                    this.postCard(post, idx || 0)
                  }
                })
              }
              .columnsTemplate("1fr 1fr")
              .columnsGap(Spacing.S)
              .rowsGap(Spacing.S)
              .padding(Spacing.S)
              .width("100%")

              if (this.hasMore && !this.loading) {
                Text("加载更多...")
                  .fontSize(FontSizes.CAPTION)
                  .fontColor(Colors.TEXT_TERTIARY)
                  .width("100%")
                  .textAlign(TextAlign.Center)
                  .padding(Spacing.L)
                  .onClick(() => this.loadPosts(false))
              }
            }
          }
          .layoutWeight(1)
          .scrollBar(BarState.Off)
          .edgeEffect(EdgeEffect.Spring)
        }
      }
      .width("100%")
      .height("100%")
      .backgroundColor(Colors.BG_PAGE)

      // FAB 发布按钮
      Column() {
        Column() {
          PlusIcon({ size: 24, color: "#FFFFFF" })
        }
        .width(52)
        .height(52)
        .borderRadius(26)
        .backgroundColor(Colors.PRIMARY)
        .justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)
        .shadow(shadowOf(Shadow.MD))
        .onClick(() => router.pushUrl({ url: "pages/square/PostCreate" }))
      }
      .position({ x: "80%", y: "85%" })
    }
    .width("100%")
    .height("100%")
  }

  squareTabWidth(): string {
    return "40vp"
  }

  squareTabOffset(): number {
    const tabs = ["latest", "hot", "3d", "challenge"]
    const idx = tabs.indexOf(this.selectedTab)
    const tabWidth = 56
    return idx * tabWidth
  }

  /** 字段容错：封面优先 imageUrls[0]，回退 imageUrl（SquarePost 无单数 imageUrl 字段） */
  coverOf(post: any): string {
    if (post.imageUrl) return post.imageUrl as string
    if (Array.isArray(post.imageUrls) && post.imageUrls.length > 0) return post.imageUrls[0] as string
    return ""
  }

  /** 风格标签容错：无 styleTag 时回退 topic / 默认文案 */
  tagOf(post: any): string {
    if (post.styleTag) return post.styleTag as string
    if (post.topic) return post.topic as string
    return "AI 作品"
  }

  /** 是否为 3D 卡片（兼容 boolean / 0/1 / 字符串） */
  is3d(post: any): boolean {
    const v = post.is3d
    return v === true || v === 1 || v === "1"
  }

  @Builder skeletonGrid() {
    Column() {
      Grid() {
        ForEach([0, 1, 2, 3], (i: number) => {
          GridItem() {
            Column() {
              Column()
                .width("100%")
                .aspectRatio(1)
                .backgroundColor(Colors.BG_INPUT)
                .borderRadius(Radius.SM)
              Column()
                .width("80%")
                .height(12)
                .backgroundColor(Colors.BG_INPUT)
                .borderRadius(4)
                .margin({ top: 8 })
              Column()
                .width("50%")
                .height(10)
                .backgroundColor(Colors.BG_INPUT)
                .borderRadius(4)
                .margin({ top: 4 })
            }
            .padding(8)
            .backgroundColor(Colors.BG_CARD)
            .borderRadius(Radius.MD)
          }
        })
      }
      .columnsTemplate("1fr 1fr")
      .columnsGap(Spacing.S)
      .rowsGap(Spacing.S)
      .padding(Spacing.S)
      .layoutWeight(1)
    }
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
            this.loadPosts(true)
          })
        })
    }
    .margin({ right: Spacing.S })
    .alignItems(HorizontalAlign.Center)
  }

  @Builder postCard(post: any, idx: number) {
    Column() {
      Stack({ alignContent: Alignment.TopEnd }) {
        if (this.coverOf(post) !== "") {
          Image(this.coverOf(post))
            .width("100%")
            .aspectRatio(1)
            .objectFit(ImageFit.Cover)
            .borderRadius(Radius.SM)
            .onClick(() => {
              if (this.coverOf(post) !== "") {
                router.pushUrl({ url: "pages/common/ImagePreview", params: { url: this.coverOf(post) } })
              }
            })
        } else {
          GradientPlaceholder({
            label: this.tagOf(post),
            fromColor: SQUARE_PALETTE[idx % SQUARE_PALETTE.length][0],
            toColor:   SQUARE_PALETTE[idx % SQUARE_PALETTE.length][1],
            height: 0
          })
        }

        // 分享按钮（分享后上报 shareRecord + 奖励 toast）
        Column() {
          ShareIcon({ size: 14, color: "#FFFFFF" })
        }
        .width(26)
        .height(26)
        .borderRadius(13)
        .backgroundColor("rgba(0,0,0,0.38)")
        .justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)
        .margin({ right: 4, top: 4 })
        .onClick(() => {
          showShareMenu(post.id as number, (post.content as string) || this.tagOf(post))
        })

        // 风格标签
        Text(this.tagOf(post))
          .fontSize(FontSizes.TINY)
          .fontColor("#FFFFFF")
          .fontWeight(FontWeight.Medium)
          .padding({ left: 6, right: 6, top: 3, bottom: 3 })
          .backgroundColor("rgba(0,0,0,0.45)")
          .borderRadius(8)
          .position({ x: 4, y: 4 })

        // 3D 角标
        if (this.is3d(post)) {
          Text("3D")
            .fontSize(FontSizes.TINY)
            .fontColor("#FFFFFF")
            .fontWeight(FontWeight.Bold)
            .padding({ left: 6, right: 6, top: 2, bottom: 2 })
            .backgroundColor("#9D7BFF")
            .borderRadius(8)
            .position({ x: 4, y: 32 })
        }
      }

      Column() {
        Text(post.content || "")
          .fontSize(FontSizes.CAPTION)
          .fontColor(Colors.TEXT_PRIMARY)
          .maxLines(2)
          .textOverflow({ overflow: TextOverflow.Ellipsis })
          .width("100%")
          .margin({ top: 6 })
        Row() {
          HeartFilledIcon({ size: 12, color: Colors.PRIMARY })
            .margin({ right: 2 })
          Text(`${post.likeCount || 0}`)
            .fontSize(FontSizes.TINY)
            .fontColor(Colors.TEXT_TERTIARY)
          Blank()
          CommentIcon({ size: 12, color: Colors.TEXT_TERTIARY })
            .margin({ right: 2 })
          Text(`${post.commentCount || 0}`)
            .fontSize(FontSizes.TINY)
            .fontColor(Colors.TEXT_TERTIARY)
        }
        .margin({ top: 4 })
        .width("100%")
      }
      .alignItems(HorizontalAlign.Start)
      .width("100%")
    }
    .width("100%")
    .padding(8)
    .backgroundColor(Colors.BG_CARD)
    .borderRadius(Radius.MD)
    .shadow(shadowOf(Shadow.SM))
    .onClick(() => {
      if (this.is3d(post)) {
        // 3D 卡片 → Model3D 预览
        router.pushUrl({
          url: "pages/ai/Model3D",
          params: { modelUrl: post.modelUrl || post.model_url || post.gltfUrl || "" }
        })
      } else {
        router.pushUrl({ url: "pages/square/PostDetail", params: { id: post.id } })
      }
    })
  }
}

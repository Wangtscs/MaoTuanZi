/**
 * 广场帖子详情
 * 功能：主图预览 / 点赞（带状态+计数）/ 评论列表（加载更多）/ 分享
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing, Radius } from "../../common/Constants";
import { TopBar } from "../../components/CommonComponents";
import { ApiService } from "../../service/ApiService";
import { HeartFilledIcon, LikeIcon, CommentIcon, ShareIcon, PawIcon } from "../../components/icons/Icons";
import { showShareMenu, getPostShareUrl } from "../../utils/ShareSheet";
import { QrCodeEncoder } from "../../utils/QrCodeGenerator";

// 示例评论数据（API无数据时降级展示）
const MOCK_COMMENTS = [
  { id: 1, content: "太可爱了吧！！我家猫也是这样", createdAt: new Date(Date.now() - 3600000).toISOString() },
  { id: 2, content: "这是什么风格的画像呀，求教程", createdAt: new Date(Date.now() - 7200000).toISOString() },
  { id: 3, content: "团子妈养得真好，毛发好亮", createdAt: new Date(Date.now() - 86400000).toISOString() }
]

@Entry
@Component
struct PostDetailPage {
  @State post: any = {}
  @State comments: any[] = []
  @State loadingPost: boolean = false
  @State loadingComments: boolean = false
  @State postError: boolean = false
  @State liked: boolean = false          // 当前用户是否已点赞
  @State likeCount: number = 0          // 点赞数
  @State commentPage: number = 1        // 评论分页
  @State hasMoreComments: boolean = true // 是否有更多评论
  @State qrMatrix: boolean[][] = []       // 本地生成的二维码矩阵（true=黑模块）
  @State qrSidePx: number = 0             // 二维码画布边长（vp，含静区）
  private qrCell: number = 3              // 单模块边长（vp）
  private id: number = 0
  // 二维码用 Canvas 单组件绘制：相比逐模块 Column（V3 需 841 个组件）大幅降低布局开销
  private qrCtx: CanvasRenderingContext2D =
    new CanvasRenderingContext2D(new RenderingContextSettings(true))

  aboutToAppear() {
    const params = router.getParams() as Record<string, Object>
    this.id = (params?.id as number) || 0
    if (this.id > 0) {
      this.loadPost()
      this.loadComments(true)
    } else {
      // 演示态（id=0）
      this.post = {
        userId: 0,
        content: "这是我家的团子，超可爱！",
        imageUrls: ["https://images.unsplash.com/photo-1514888451691-3a165f5f62e4?w=800&q=80"],
        likeCount: 23,
        commentCount: 5,
        liked: false
      }
      this.liked = false
      this.likeCount = 23
      this.comments = MOCK_COMMENTS
    }
    // 页面加载即生成可扫码的分享二维码（依赖 this.id，两者分支均已赋值）
    this.ensureQrCode()
  }

  async loadPost() {
    this.loadingPost = true
    this.postError = false
    try {
      const data = await ApiService.getPost(this.id)
      this.post = data || {}
      this.liked = this.post.liked || false
      this.likeCount = this.post.likeCount || 0
    } catch (e: any) {
      this.postError = true
      promptAction.showToast({ message: "加载帖子失败", duration: 1500 })
    } finally {
      this.loadingPost = false
    }
  }

  async loadComments(reset: boolean = false) {
    if (reset) {
      this.commentPage = 1
      this.comments = []
      this.hasMoreComments = true
    }
    if (!this.hasMoreComments) return
    this.loadingComments = true
    try {
      const data: any = await ApiService.getComments(this.id) || []
      if (reset) {
        this.comments = data.length > 0 ? data : MOCK_COMMENTS
      } else {
        this.comments = this.comments.concat(data)
      }
      // 演示数据固定3条，无更多
      this.hasMoreComments = data.length >= 3
      this.commentPage += 1
    } catch (e: any) {
      // 失败时展示示例
      if (reset) this.comments = MOCK_COMMENTS
      promptAction.showToast({ message: "加载评论失败", duration: 1500 })
    } finally {
      this.loadingComments = false
    }
  }

  /** 点赞：切换 liked 状态，增/减计数 */
  async onLike() {
    if (this.liked) {
      // 取消点赞
      try {
        await ApiService.unlikePost(this.id)
        this.liked = false
        this.likeCount = Math.max(0, this.likeCount - 1)
      } catch (e: any) {
        promptAction.showToast({ message: "操作失败，请重试", duration: 1500 })
      }
    } else {
      // 点赞
      try {
        await ApiService.likePost(this.id)
        this.liked = true
        this.likeCount += 1
        promptAction.showToast({ message: "已点赞～", duration: 1000 })
      } catch (e: any) {
        promptAction.showToast({ message: "操作失败，请重试", duration: 1500 })
      }
    }
  }

  /** 生成可扫码的分享二维码矩阵（唯一来源），编码失败则降级为空 */
  private ensureQrCode(): void {
    if (this.qrMatrix.length > 0) {
      return   // 已生成，避免重复计算
    }
    const shareUrl: string = getPostShareUrl(this.id)
    try {
      const m: boolean[][] = QrCodeEncoder.encode(shareUrl)
      const quiet: number = 4
      const total: number = m.length + quiet * 2
      // 单模块不低于 3vp，保证真机摄像头可识别
      this.qrCell = Math.max(3, Math.floor(120 / total))
      this.qrSidePx = total * this.qrCell
      this.qrMatrix = m
    } catch (e) {
      this.qrMatrix = []
      this.qrSidePx = 0
    }
  }

  /** 将二维码矩阵绘制到 Canvas（Canvas onReady 时矩阵已就绪） */
  private drawQrCanvas(): void {
    const n: number = this.qrMatrix.length
    if (n === 0 || this.qrSidePx === 0) {
      return
    }
    const quiet: number = 4
    const cell: number = this.qrCell
    const ctx: CanvasRenderingContext2D = this.qrCtx
    // 白底（静区必须为白，否则扫码失败）
    ctx.fillStyle = "#FFFFFF"
    ctx.fillRect(0, 0, this.qrSidePx, this.qrSidePx)
    ctx.fillStyle = "#000000"
    for (let r = 0; r < n; r++) {
      const row: boolean[] = this.qrMatrix[r]
      for (let c = 0; c < n; c++) {
        if (row[c]) {
          ctx.fillRect((quiet + c) * cell, (quiet + r) * cell, cell, cell)
        }
      }
    }
  }

  /** 分享：生成分享卡（含本地二维码） → 系统分享 */
  async onShare() {
    const content = this.post.content || "来看看毛孩子的可爱瞬间"
    this.ensureQrCode()
    // 弹出分享选项
    await showShareMenu(this.id, content)
  }

  /** 评论成功后刷新列表 */
  async onCommentPosted() {
    await this.loadComments(true)
  }

  build() {
    Column() {
      TopBar({ title: "帖子详情", showBack: true, onBack: () => router.back() })

      Scroll() {
        Column() {
          // ====== 主图 ======
          if (this.loadingPost) {
            Column()
              .width("100%")
              .height(320)
              .backgroundColor(Colors.BG_INPUT)
          } else {
            Stack({ alignContent: Alignment.Bottom }) {
              if (this.post.imageUrls?.length > 0) {
                Image(this.post.imageUrls[0])
                  .width("100%")
                  .height(320)
                  .objectFit(ImageFit.Cover)
                  .onClick(() => {
                    const url = this.post.imageUrls[0]
                    if (url) {
                      router.pushUrl({ url: "pages/common/ImagePreview", params: { url } })
                    }
                  })
              } else {
                this.postPlaceholder()
              }

              // 图片页码指示
              if (this.post.imageUrls?.length > 1) {
                Text(`${this.post.imageUrls.length} 张`)
                  .fontSize(FontSizes.TINY)
                  .fontColor("#FFFFFF")
                  .padding({ left: 8, right: 8, top: 4, bottom: 4 })
                  .backgroundColor("rgba(0,0,0,0.5)")
                  .borderRadius(10)
                  .margin({ right: 12, bottom: 12 })
                  .alignSelf(HorizontalAlign.End)
              }
            }
            .width("100%")
            .height(320)
            .clip(true)
            .borderRadius(Radius.SM)
          }

          // ====== 内容 + 操作栏 ======
          Column() {
            Text(this.post.content || "暂无内容")
              .fontSize(FontSizes.BODY)
              .fontColor(Colors.TEXT_PRIMARY)
              .lineHeight(22)
              .width("100%")

            Text(this.formatDate(this.post.createdAt))
              .fontSize(FontSizes.CAPTION)
              .fontColor(Colors.TEXT_TERTIARY)
              .margin({ top: Spacing.S })

            // 操作栏
            Divider().color(Colors.DIVIDER).margin({ top: Spacing.M, bottom: Spacing.M })

            Row() {
              // 点赞按钮
              Row() {
                if (this.liked) {
                  HeartFilledIcon({ size: 16, color: "#FF4D4F" })
                } else {
                  LikeIcon({ size: 16, color: Colors.TEXT_SECONDARY })
                }
                Text(`${this.likeCount}`)
                  .fontSize(FontSizes.BODY_S)
                  .fontColor(this.liked ? "#FF4D4F" : Colors.TEXT_SECONDARY)
                  .margin({ left: 4 })
              }
              .padding({ left: Spacing.M, right: Spacing.M, top: 6, bottom: 6 })
              .backgroundColor(this.liked ? "rgba(255,77,79,0.08)" : Colors.BG_INPUT)
              .borderRadius(Radius.SM)
              .onClick(() => this.onLike())

              Blank().width(Spacing.M)

              // 评论按钮
              Row() {
                CommentIcon({ size: 16, color: Colors.TEXT_SECONDARY })
                Text(`${this.comments.length}`)
                  .fontSize(FontSizes.BODY_S)
                  .fontColor(Colors.TEXT_SECONDARY)
                  .margin({ left: 4 })
              }
              .padding({ left: Spacing.M, right: Spacing.M, top: 6, bottom: 6 })
              .backgroundColor(Colors.BG_INPUT)
              .borderRadius(Radius.SM)
              .onClick(() => this.addComment())

              Blank().width(Spacing.M)

              // 分享按钮（点击弹出分享选项）
              Row() {
                ShareIcon({ size: 16, color: Colors.TEXT_SECONDARY })
                Text("分享")
                  .fontSize(FontSizes.BODY_S)
                  .fontColor(Colors.TEXT_SECONDARY)
                  .margin({ left: 4 })
              }
              .padding({ left: Spacing.M, right: Spacing.M, top: 6, bottom: 6 })
              .backgroundColor(Colors.BG_INPUT)
              .borderRadius(Radius.SM)
              .onClick(() => this.onShare())
            }
            .width("100%")
          }
          .padding(Spacing.L)
          .backgroundColor(Colors.BG_CARD)

          // ====== 二维码分享入口 ======
          if (this.qrMatrix.length > 0) {
            Column() {
              Row() {
                // 二维码（本地生成，含静区）
                this.qrCodeView()

                Column() {
                  Text("扫码查看原文")
                    .fontSize(FontSizes.BODY_S)
                    .fontColor(Colors.TEXT_PRIMARY)
                    .fontWeight(FontWeight.Medium)
                  Text(getPostShareUrl(this.id))
                    .fontSize(FontSizes.TINY)
                    .fontColor(Colors.TEXT_TERTIARY)
                    .maxLines(1)
                    .textOverflow({ overflow: TextOverflow.Ellipsis })
                    .margin({ top: 4 })
                  Text("截图保存后分享给好友")
                    .fontSize(FontSizes.TINY)
                    .fontColor(Colors.TEXT_SECONDARY)
                    .margin({ top: 4 })
                }
                .layoutWeight(1)
                .margin({ left: Spacing.M })
                .alignItems(HorizontalAlign.Start)
              }
              .width("100%")
              .padding(Spacing.M)
            }
            .width("100%")
            .backgroundColor("rgba(255,182,193,0.06)")
            .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.S })
            .borderRadius(Radius.MD)
          }

          Divider().color(Colors.DIVIDER).margin({ top: Spacing.S })

          // ====== 评论列表 ======
          Column() {
            Row() {
              Text("评论")
                .fontSize(FontSizes.BODY)
                .fontWeight(FontWeight.Medium)
                .fontColor(Colors.TEXT_PRIMARY)
              if (this.comments.length > 0) {
                Text(`（${this.comments.length}）`)
                  .fontSize(FontSizes.BODY)
                  .fontColor(Colors.TEXT_SECONDARY)
              }
              Blank()
              // 写评论入口
              Text("写评论...")
                .fontSize(FontSizes.BODY_S)
                .fontColor(Colors.TEXT_TERTIARY)
                .onClick(() => this.addComment())
            }
            .width("100%")
            .padding({ left: Spacing.L, right: Spacing.L, top: Spacing.M, bottom: Spacing.S })

            if (this.loadingComments && this.comments.length === 0) {
              // 骨架
              Column() {
                SkeletonCommentItem()
                SkeletonCommentItem()
              }
              .padding({ left: Spacing.L, right: Spacing.L })
            } else if (this.comments.length === 0) {
              Column() {
                Text("还没有评论")
                  .fontSize(FontSizes.BODY_S)
                  .fontColor(Colors.TEXT_TERTIARY)
                  .textAlign(TextAlign.Center)
                Text("快来抢沙发吧")
                  .fontSize(FontSizes.CAPTION)
                  .fontColor(Colors.TEXT_TERTIARY)
                  .textAlign(TextAlign.Center)
                  .margin({ top: 4 })
                  .onClick(() => this.addComment())
              }
              .width("100%")
              .padding(Spacing.XL)
              .alignItems(HorizontalAlign.Center)
            } else {
              // 评论项
              ForEach(this.comments, (c: any, idx: number) => {
                Column() {
                  Row() {
                    Image($r("app.media.icon_user"))
                      .width(32)
                      .height(32)
                      .fillColor(Colors.PRIMARY)
                      .backgroundColor(Colors.BG_INPUT)
                      .borderRadius(16)
                    Column() {
                      Text(`用户${c.id || idx + 1}`)
                        .fontSize(FontSizes.BODY_S)
                        .fontColor(Colors.TEXT_SECONDARY)
                      Text(c.content)
                        .fontSize(FontSizes.BODY)
                        .fontColor(Colors.TEXT_PRIMARY)
                        .lineHeight(20)
                        .margin({ top: 2 })
                      Text(this.formatDate(c.createdAt))
                        .fontSize(FontSizes.TINY)
                        .fontColor(Colors.TEXT_TERTIARY)
                        .margin({ top: 4 })
                    }
                    .layoutWeight(1)
                    .margin({ left: Spacing.S })
                    .alignItems(HorizontalAlign.Start)
                  }
                  .width("100%")
                  .alignItems(VerticalAlign.Top)

                  // 举报/回复
                  Row() {
                    Text("回复")
                      .fontSize(FontSizes.TINY)
                      .fontColor(Colors.TEXT_TERTIARY)
                      .padding({ left: 40 })
                    Text(" · ")
                      .fontSize(FontSizes.TINY)
                      .fontColor(Colors.TEXT_TERTIARY)
                    Text("举报")
                      .fontSize(FontSizes.TINY)
                      .fontColor(Colors.TEXT_TERTIARY)
                  }
                  .margin({ top: 6, left: 40 })
                }
                .width("100%")
                .padding({ left: Spacing.L, right: Spacing.L, top: Spacing.M, bottom: Spacing.S })

                if (idx < this.comments.length - 1) {
                  Divider()
                    .color(Colors.DIVIDER)
                    .margin({ left: Spacing.L + 40 })
                }
              })

              // 加载更多
              if (this.hasMoreComments) {
                Text(this.loadingComments ? "加载中..." : "加载更多评论")
                  .fontSize(FontSizes.BODY_S)
                  .fontColor(this.loadingComments ? Colors.TEXT_TERTIARY : Colors.PRIMARY)
                  .textAlign(TextAlign.Center)
                  .width("100%")
                  .padding(Spacing.M)
                  .onClick(() => {
                    if (!this.loadingComments) this.loadComments(false)
                  })
              } else {
                Text("— 没有更多了 —")
                  .fontSize(FontSizes.TINY)
                  .fontColor(Colors.TEXT_TERTIARY)
                  .textAlign(TextAlign.Center)
                  .width("100%")
                  .padding(Spacing.M)
              }
            }
          }
          .padding({ bottom: Spacing.XL })
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

  /** 本地二维码渲染：Canvas 单组件绘制，白底 + 4 模块静区 */
  @Builder qrCodeView() {
    if (this.qrMatrix.length > 0 && this.qrSidePx > 0) {
      Canvas(this.qrCtx)
        .width(this.qrSidePx)
        .height(this.qrSidePx)
        .backgroundColor("#FFFFFF")
        .borderRadius(4)
        .onReady(() => this.drawQrCanvas())
    } else {
      Column()
        .width(80)
        .height(80)
        .backgroundColor("#FFFFFF")
        .borderRadius(4)
    }
  }

  @Builder postPlaceholder() {
    Column() {
      PawIcon({ size: 56, color: Colors.TEXT_TERTIARY })
    }
    .width("100%")
    .height("100%")
    .justifyContent(FlexAlign.Center)
    .alignItems(HorizontalAlign.Center)
    .linearGradient({ angle: 135, colors: [[0xFFE4E1], [0xFFE4B5]] })
  }

  formatDate(s: string): string {
    if (!s) return "刚刚"
    try {
      const diff = Date.now() - new Date(s).getTime()
      if (diff < 60000) return "刚刚"
      if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
      if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
      if (diff < 604800000) return `${Math.floor(diff / 86400000)} 天前`
      return new Date(s).toLocaleDateString("zh-CN")
    } catch (e) {
      return "刚刚"
    }
  }

  async addComment() {
    promptAction.showTextInputDialog({
      title: "发表评论",
      placeholderText: "说点什么吧..."
    }).then(async (res) => {
      if (res.value && res.value.trim()) {
        try {
          await ApiService.addComment(this.id, res.value.trim())
          promptAction.showToast({ message: "评论发布成功", duration: 1200 })
          this.onCommentPosted()
        } catch (e: any) {
          promptAction.showToast({ message: e.message || "发布失败", duration: 1500 })
        }
      }
    })
  }
}

// 骨架屏：评论项
@Component
struct SkeletonCommentItem {
  build() {
    Row() {
      Column()
        .width(32)
        .height(32)
        .backgroundColor(Colors.BG_INPUT)
        .borderRadius(16)
      Column() {
        Column()
          .width(80)
          .height(12)
          .backgroundColor(Colors.BG_INPUT)
          .borderRadius(4)
        Column()
          .width("90%")
          .height(14)
          .backgroundColor(Colors.BG_INPUT)
          .borderRadius(4)
          .margin({ top: 6 })
        Column()
          .width(60)
          .height(10)
          .backgroundColor(Colors.BG_INPUT)
          .borderRadius(4)
          .margin({ top: 4 })
      }
      .layoutWeight(1)
      .margin({ left: Spacing.S })
    }
    .width("100%")
    .padding({ top: Spacing.M, bottom: Spacing.M })
  }
}

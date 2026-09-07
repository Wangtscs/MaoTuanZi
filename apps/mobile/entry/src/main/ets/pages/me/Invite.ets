/**
 * 邀请有礼
 *
 * 阶段4（分享奖励/邀请裂变）：
 *  - GET /v1/user/invite → { inviteCode, inviteUrl, rewardRule }
 *  - 复用 QrCodeGenerator 生成「我的邀请码」分享卡（深链 maotuanzi://invite/{code}）
 *  - 支持复制邀请链接（@ohos.pasteboard）与系统分享（ShareSheet.shareViaSystem）
 *
 * 注意：不修改 utils/QrCodeGenerator.ets，仅 import 使用其 QrCodeEncoder。
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import pasteboard from "@ohos.pasteboard";
import { Colors, FontSizes, Spacing, Radius } from "../../common/Constants";
import { TopBar, PrimaryButton } from "../../components/CommonComponents";
import { ApiService } from "../../service/ApiService";
import { shareViaSystem, getInviteShareUrl } from "../../utils/ShareSheet";
import { QrCodeEncoder } from "../../utils/QrCodeGenerator";
import { GiftIcon, CrownIcon } from "../../components/icons/Icons";

// 后端未就绪时的降级数据
const FALLBACK_CODE: string = "MAOTUANZI"
const FALLBACK_RULE: string = "邀请1位得3天VIP"

@Entry
@Component
struct InvitePage {
  @State inviteCode: string = ""
  @State inviteUrl: string = ""
  @State rewardRule: string = ""
  @State loading: boolean = false
  @State qrMatrix: boolean[][] = []   // 本地生成的二维码矩阵（true=黑模块）
  @State qrSidePx: number = 0         // 二维码画布边长（vp，含静区）
  private qrCell: number = 3          // 单模块边长（vp）
  // 二维码用 Canvas 单组件绘制（与 PostDetail.ets 相同方案）
  private qrCtx: CanvasRenderingContext2D =
    new CanvasRenderingContext2D(new RenderingContextSettings(true))

  aboutToAppear() {
    this.loadInvite()
  }

  /** 读取邀请信息（失败降级到示例数据，保证可演示） */
  async loadInvite() {
    this.loading = true
    try {
      const data: any = await ApiService.userInvite()
      const code: string = data?.inviteCode || FALLBACK_CODE
      this.inviteCode = code
      this.inviteUrl = data?.inviteUrl || getInviteShareUrl(code)
      this.rewardRule = data?.rewardRule || FALLBACK_RULE
    } catch (e) {
      this.inviteCode = FALLBACK_CODE
      this.inviteUrl = getInviteShareUrl(FALLBACK_CODE)
      this.rewardRule = FALLBACK_RULE
    } finally {
      this.loading = false
      this.ensureQrCode()
    }
  }

  /** 生成可扫码的邀请二维码矩阵（唯一来源），编码失败则降级为空 */
  private ensureQrCode(): void {
    if (this.qrMatrix.length > 0) {
      return
    }
    if (!this.inviteUrl) {
      this.inviteUrl = getInviteShareUrl(this.inviteCode || FALLBACK_CODE)
    }
    try {
      const m: boolean[][] = QrCodeEncoder.encode(this.inviteUrl)
      const quiet: number = 4
      const total: number = m.length + quiet * 2
      // 单模块不低于 3vp，保证真机摄像头可识别
      this.qrCell = Math.max(3, Math.floor(180 / total))
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

  /** 复制邀请链接到剪贴板 */
  async copyLink() {
    if (!this.inviteUrl) {
      promptAction.showToast({ message: "邀请链接生成中，请稍候", duration: 1200 })
      return
    }
    try {
      const systemPasteboard = pasteboard.getSystemPasteboard()
      const pasteData = pasteboard.createData(pasteboard.MIMETYPE_TEXT_PLAIN, this.inviteUrl)
      await systemPasteboard.setData(pasteData)
      promptAction.showToast({ message: "邀请链接已复制", duration: 1500 })
    } catch (e) {
      promptAction.showToast({ message: `邀请码：${this.inviteCode}`, duration: 2000 })
    }
  }

  /** 系统分享邀请链接 */
  async onShare() {
    await shareViaSystem("毛团子AI · 邀请有礼", `邀好友得 VIP，快来体验萌宠 AI（邀请码 ${this.inviteCode}）`, this.inviteUrl)
  }

  build() {
    Column() {
      TopBar({ title: "邀请有礼", showBack: true, onBack: () => router.back() })

      Scroll() {
        Column() {
          // 顶部说明
          Column() {
            Row() {
              GiftIcon({ size: 22, color: Colors.PRIMARY })
                .margin({ right: Spacing.S })
              Text("邀好友，得 VIP")
                .fontSize(FontSizes.H2)
                .fontWeight(FontWeight.Bold)
                .fontColor(Colors.TEXT_PRIMARY)
            }
            Text(this.rewardRule || "邀请好友得奖励")
              .fontSize(FontSizes.BODY_S)
              .fontColor(Colors.TEXT_SECONDARY)
              .margin({ top: Spacing.S })
              .width("100%")
          }
          .width("100%")
          .padding(Spacing.L)
          .backgroundColor(Colors.BG_CARD)
          .borderRadius(Radius.LG)
          .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.L })
          .alignItems(HorizontalAlign.Start)

          // 邀请码分享卡
          Column() {
            if (this.qrMatrix.length > 0 && this.qrSidePx > 0) {
              Canvas(this.qrCtx)
                .width(this.qrSidePx)
                .height(this.qrSidePx)
                .backgroundColor("#FFFFFF")
                .borderRadius(8)
                .onReady(() => this.drawQrCanvas())
            } else {
              Column()
                .width(180)
                .height(180)
                .backgroundColor("#FFFFFF")
                .borderRadius(8)
                .justifyContent(FlexAlign.Center)
                .alignItems(HorizontalAlign.Center)
            }

            Row() {
              CrownIcon({ size: 16, color: "#FAAD14" })
                .margin({ right: 4 })
              Text(`邀请码：${this.inviteCode}`)
                .fontSize(FontSizes.BODY)
                .fontWeight(FontWeight.Bold)
                .fontColor(Colors.TEXT_PRIMARY)
            }
            .margin({ top: Spacing.M })

            Text("扫码或复制链接，好友注册后双方得奖励")
              .fontSize(FontSizes.CAPTION)
              .fontColor(Colors.TEXT_TERTIARY)
              .margin({ top: Spacing.S })
          }
          .width("100%")
          .padding({ top: Spacing.XL, bottom: Spacing.XL })
          .backgroundColor(Colors.BG_CARD)
          .borderRadius(Radius.LG)
          .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.M })
          .alignItems(HorizontalAlign.Center)

          // 操作按钮
          PrimaryButton({ text: "分享给好友", block: true })
            .onClick(() => this.onShare())
            .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.L })

          Button("复制邀请链接")
            .width("calc(100% - 32vp)")
            .height(48)
            .fontSize(FontSizes.BODY)
            .fontColor(Colors.PRIMARY)
            .backgroundColor(Colors.BG_CARD)
            .borderRadius(Radius.MD)
            .border({ width: 1, color: Colors.PRIMARY })
            .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.S, bottom: Spacing.XXL })
            .onClick(() => this.copyLink())
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
}

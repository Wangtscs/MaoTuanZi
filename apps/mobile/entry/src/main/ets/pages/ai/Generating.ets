/**
 * AI 生成中页（带进度动画 + 留存增强）
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing, Radius, Shadow, shadowOf } from "../../common/Constants";
import { TopBar } from "../../components/CommonComponents";
import { ApiService } from "../../service/ApiService";
import { MagicIcon } from "../../components/icons/Icons";

// 步骤文案轮播
const STEP_TEXTS = [
  "正在识别宠物特征中...",
  "AI 正在发挥创意中...",
  "即将呈现你的专属作品..."
]

// 趣味小知识
const PET_TIPS = [
  { emoji: "🌿", tip: "猫咪每天需要 12-16 小时睡眠哦" },
  { emoji: "💧", tip: "狗狗的饮水量约为体重 × 50ml" },
  { emoji: "🧶", tip: "猫咪磨爪子是在标记领地，别生气哦" }
]

@Entry
@Component
struct GeneratingPage {
  @State progress: number = 0
  @State status: string = "排队中..."
  @State taskId: number = 0
  @State isFailed: boolean = false
  @State stepIdx: number = 0
  @State tipIdx: number = 0
  private timer: number = -1
  private stepTimer: number = -1
  private tipTimer: number = -1
  private simTimer: number = -1
  private startTime: number = 0
  private readonly TIMEOUT_MS: number = 60000  // 60s超时

  aboutToAppear() {
    const params = router.getParams() as Record<string, Object>
    this.taskId = (params?.taskId as number) || 0
    this.startTime = new Date().getTime()
    this.startPolling()
    this.startCarousels()
  }

  aboutToDisappear() {
    this.clearAllTimers()
  }

  clearAllTimers() {
    if (this.timer > 0) clearInterval(this.timer)
    if (this.stepTimer > 0) clearInterval(this.stepTimer)
    if (this.tipTimer > 0) clearInterval(this.tipTimer)
    if (this.simTimer > 0) clearInterval(this.simTimer)
  }

  startCarousels() {
    // 步骤文案轮播，每 5 秒切换
    this.stepTimer = setInterval(() => {
      this.stepIdx = (this.stepIdx + 1) % STEP_TEXTS.length
    }, 5000)

    // 趣味知识轮播，每 5 秒切换
    this.tipTimer = setInterval(() => {
      this.tipIdx = (this.tipIdx + 1) % PET_TIPS.length
    }, 5000)

    // 模拟进度条（0→100%，每 200ms 增加 1%）
    // 真实进度由 API 轮询提供，此处仅作为兜底展示
    this.simTimer = setInterval(() => {
      if (this.progress < 95) {
        this.progress += 1
      }
    }, 200)
  }

  startPolling() {
    this.timer = setInterval(async () => {
      if (new Date().getTime() - this.startTime > this.TIMEOUT_MS) {
        this.status = "任务超时"
        this.isFailed = true
        this.clearAllTimers()
        promptAction.showToast({ message: "生成超时，请稍后重试", duration: 2000 })
        return
      }
      try {
        const task: any = await ApiService.getAiTask(this.taskId)
        if (task) {
          // 优先使用 API 真实进度
          if (task.progress !== undefined) {
            this.progress = task.progress
          }
          if (task.status === 0) {
            this.status = "排队中..."
          } else if (task.status === 1) {
            this.status = "AI 正在创作中..."
          } else if (task.status === 2) {
            this.status = "完成！"
            this.progress = 100
            this.clearAllTimers()
            setTimeout(() => {
              router.replaceUrl({ url: "pages/ai/Preview", params: { workId: task.id } })
            }, 500)
            return
          } else if (task.status === 3) {
            this.status = "生成失败"
            this.isFailed = true
            this.clearAllTimers()
            promptAction.showToast({ message: "生成失败，请重新尝试", duration: 2000 })
            return
          }
        }
      } catch (e) {
        // 网络抖动不影响轮询
      }
    }, 1500)
  }

  onBack() {
    promptAction.showActionMenu({
      title: "确定取消生成？",
      buttons: [
        { text: "继续生成", color: "#FFB6C1" },
        { text: "取消生成", color: "#FF4D4F" }
      ]
    }).then((index) => {
      if (index?.index === 0) return
      if (index?.index === 1) {
        try { ApiService.cancelAiTask(this.taskId) } catch (e) {}
        this.clearAllTimers()
        router.back()
      }
    }).catch(() => {})
  }

  build() {
    Column() {
      TopBar({ title: "生成中", showBack: true, onBack: () => this.onBack() })

      Column() {
        // 圆形进度
        Stack() {
          Circle({ width: 180, height: 180 })
            .stroke(Colors.BG_INPUT, 8)
            .fill(Color.Transparent)

          Circle({ width: 180, height: 180 })
            .stroke(Colors.PRIMARY, 8)
            .fill(Color.Transparent)
            .strokeDashArray([Math.PI * 180 * this.progress / 100, Math.PI * 180])
            .strokeDashOffset(0)
            .rotate({ angle: -90 })

          Column() {
            Text(`${Math.round(this.progress)}%`)
              .fontSize(36)
              .fontWeight(FontWeight.Bold)
              .fontColor(Colors.PRIMARY)
            Text(this.status)
              .fontSize(FontSizes.BODY_S)
              .fontColor(Colors.TEXT_SECONDARY)
              .margin({ top: 8 })
          }
        }
        .width(180)
        .height(180)
        .margin({ top: 32 })

        // 进度条（底部补充）
        Column() {
          Row() {
            Column()
              .width(`${this.progress}%`)
              .height(4)
              .backgroundColor(Colors.PRIMARY)
              .borderRadius(2)
              .transition(TransitionEffect.WIDTH.animation({ duration: 200 }))
          }
          .width("100%")
          .height(4)
          .backgroundColor(Colors.BG_INPUT)
          .borderRadius(2)
        }
        .width("calc(100% - 32vp)")
        .margin({ top: Spacing.L })

        // 步骤文案轮播
        Column() {
          Row() {
            MagicIcon({ size: 16, color: Colors.PRIMARY }).margin({ right: 6 })
            Text(STEP_TEXTS[this.stepIdx])
              .fontSize(FontSizes.BODY_S)
              .fontColor(Colors.TEXT_SECONDARY)
              .transition(TransitionEffect.OPACITY.animation({ duration: 500 }))
          }
        }
        .width("calc(100% - 32vp)")
        .margin({ top: Spacing.M })

        // 趣味小知识卡片
        Column() {
          Column() {
            Row() {
              Text(PET_TIPS[this.tipIdx].emoji)
                .fontSize(24)
                .margin({ right: Spacing.S })
              Text(PET_TIPS[this.tipIdx].tip)
                .fontSize(FontSizes.BODY_S)
                .fontColor(Colors.TEXT_PRIMARY)
                .layoutWeight(1)
                .transition(TransitionEffect.OPACITY.animation({ duration: 500 }))
            }
            .width("100%")
            .alignItems(VerticalAlign.Center)
          }
          .width("100%")
          .padding(Spacing.M)
          .backgroundColor("rgba(255,182,193,0.08)")
          .borderRadius(Radius.MD)
          .transition(TransitionEffect.OPACITY.animation({ duration: 500 }))
        }
        .width("calc(100% - 32vp)")
        .margin({ top: Spacing.L })

        Blank()

        // 底部灰色提示
        Column() {
          Text("生成完成后可解锁高清无水印版本")
            .fontSize(FontSizes.CAPTION)
            .fontColor(Colors.TEXT_TERTIARY)
        }
        .width("calc(100% - 32vp)")
        .margin({ bottom: Spacing.M })

        Button("查看我的作品")
          .width("calc(100% - 32vp)")
          .height(48)
          .fontSize(FontSizes.BODY)
          .fontColor(Colors.PRIMARY)
          .backgroundColor("rgba(255,182,193,0.1)")
          .borderRadius(Radius.MD)
          .margin({ bottom: Spacing.XXL, left: Spacing.L, right: Spacing.L })
          .onClick(() => router.replaceUrl({ url: "pages/me/Works" }))
      }
      .width("100%")
      .layoutWeight(1)
      .alignItems(HorizontalAlign.Center)
      .backgroundColor(Colors.BG_PAGE)
    }
    .width("100%")
    .height("100%")
  }
}

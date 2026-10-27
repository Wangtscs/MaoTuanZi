/**
 * 用户引导轮播页（3页）
 */
import router from "@ohos.router";
import { Colors, FontSizes, Spacing, Radius } from "../../common/Constants";
import { SparkleIcon, CrownIcon, HeartPulseIcon } from "../../components/icons/Icons";

interface OnboardingItem {
  emoji: string
  title: string
  desc: string
  gradientFrom: string
  gradientTo: string
}

const PAGES: OnboardingItem[] = [
  {
    emoji: "🎨",
    title: "AI 画像",
    desc: "用 AI 创造你的毛孩子",
    gradientFrom: "#FFD1DC",
    gradientTo: "#FFB6C1"
  },
  {
    emoji: "✨",
    title: "3D 手办",
    desc: "单图生成互动模型",
    gradientFrom: "#FFD3B6",
    gradientTo: "#FFAA80"
  },
  {
    emoji: "💊",
    title: "养宠工具",
    desc: "科学喂养不焦虑",
    gradientFrom: "#C6E2FF",
    gradientTo: "#B5EAD7"
  }
]

@Entry
@Component
struct OnboardingPage {
  @State currentPage: number = 0
  private swiperController: SwiperController = new SwiperController()

  build() {
    Stack({ alignContent: Alignment.Bottom }) {
      // 轮播内容
      Swiper(this.swiperController) {
        ForEach(PAGES, (item: OnboardingItem, idx: number) => {
          this.pageContent(item, idx)
        })
      }
      .index(this.currentPage)
      .autoPlay(false)
      .indicator(false)
      .loop(false)
      .curve(Curve.EaseOut)
      .onChange((index: number) => {
        animateTo({ duration: 200 }, () => {
          this.currentPage = index
        })
      })
      .width("100%")
      .height("100%")

      // 底部操作区
      Column() {
        // 指示器
        Row({ space: 8 }) {
          ForEach(PAGES, (_, idx: number) => {
            Column()
              .width(idx === this.currentPage ? 24 : 8)
              .height(6)
              .borderRadius(3)
              .backgroundColor(idx === this.currentPage ? Colors.PRIMARY : Colors.TEXT_TERTIARY)
              .transition(idx === this.currentPage
                ? TransitionEffect.WIDTH.animation({ duration: 200 })
                : TransitionEffect.WIDTH.animation({ duration: 200 }))
          })
        }
        .margin({ bottom: Spacing.XL })

        // 按钮
        Row() {
          Text("跳过")
            .fontSize(FontSizes.BODY)
            .fontColor(Colors.TEXT_TERTIARY)
            .onClick(() => this.goHome())

          Blank().layoutWeight(1)

          Button(this.currentPage === PAGES.length - 1 ? "开始体验" : "下一步")
            .height(44)
            .fontSize(FontSizes.BODY)
            .fontColor(Colors.TEXT_WHITE)
            .backgroundColor(Colors.PRIMARY)
            .borderRadius(Radius.MD)
            .padding({ left: Spacing.XL, right: Spacing.XL })
            .onClick(() => {
              if (this.currentPage < PAGES.length - 1) {
                this.swiperController.showNext()
              } else {
                this.goHome()
              }
            })
        }
        .width("100%")
        .padding({ left: Spacing.XL, right: Spacing.XL, bottom: Spacing.XXL })
      }
      .width("100%")
      .backgroundColor(Colors.BG_PAGE)
    }
    .width("100%")
    .height("100%")
  }

  @Builder pageContent(item: OnboardingItem, idx: number) {
    Column() {
      // 顶部留空给状态栏
      Column().height(48)

      // 装饰图形
      Column() {
        Column() {
          Text(item.emoji)
            .fontSize(80)
        }
        .width(180)
        .height(180)
        .borderRadius(90)
        .linearGradient({
          angle: 135,
          colors: [[item.gradientFrom], [item.gradientTo]]
        })
        .justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)

        Text(item.title)
          .fontSize(28)
          .fontWeight(FontWeight.Bold)
          .fontColor(Colors.TEXT_PRIMARY)
          .margin({ top: Spacing.XL })

        Text(item.desc)
          .fontSize(FontSizes.BODY)
          .fontColor(Colors.TEXT_SECONDARY)
          .margin({ top: Spacing.S })
          .textAlign(TextAlign.Center)
      }
      .width("100%")
      .layoutWeight(1)
      .justifyContent(FlexAlign.Center)
    }
    .width("100%")
    .height("100%")
    .backgroundColor(Colors.BG_PAGE)
  }

  goHome() {
    AppStorage.setOrCreate('hasSeenOnboarding', true)
    router.pushUrl({ url: "pages/onboarding/FirstPetGuide" })
  }
}

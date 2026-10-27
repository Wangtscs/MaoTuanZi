/**
 * 通用 UI 组件
 */
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Radius, Shadow, shadowOf, Spacing } from "../common/Constants";
import { WarningIcon, PawIcon, BellIcon, FolderIcon, PaintIcon, HeartIcon, ChartIcon, CrownIcon } from "./icons/Icons";

/**
 * 治愈系主按钮 - 带点击动效
 */
@Component
export struct PrimaryButton {
  @Prop text: string = ""
  @Prop disabled: boolean = false
  @Prop block: boolean = false
  @Prop loading: boolean = false
  onClick: () => void = () => {};

  build() {
    Button(this.loading ? "处理中..." : this.text)
      .width(this.block ? "100%" : "auto")
      .height(48)
      .fontSize(FontSizes.BODY)
      .fontColor(Colors.TEXT_WHITE)
      .backgroundColor(this.disabled ? Colors.TEXT_TERTIARY : Colors.PRIMARY)
      .borderRadius(Radius.MD)
      .enabled(!this.disabled && !this.loading)
      .onClick(() => this.onClick())
  }
}

/**
 * 次要按钮
 */
@Component
export struct SecondaryButton {
  @Prop text: string = ""
  @Prop block: boolean = false
  onClick: () => void = () => {};

  build() {
    Button(this.text)
      .width(this.block ? "100%" : "auto")
      .height(48)
      .fontSize(FontSizes.BODY)
      .fontColor(Colors.PRIMARY)
      .backgroundColor(Colors.BG_CARD)
      .borderRadius(Radius.MD)
      .border({ width: 1, color: Colors.PRIMARY })
      .onClick(() => this.onClick())
  }
}

/**
 * 卡片 - 支持 elevation 浮层阴影
 */
@Component
export struct Card {
  @BuilderParam contentBuilder: () => void = this.defaultBuilder;
  @Prop padding: number = Spacing.L
  @Prop radius: number = Radius.MD
  @Prop elevation: 'none' | 'sm' | 'md' | 'lg' = 'none'

  @State isPressed: boolean = false

  @Builder defaultBuilder() {}

  build() {
    Column() {
      this.contentBuilder()
    }
    .padding(this.padding)
    .backgroundColor(Colors.BG_CARD)
    .borderRadius(this.radius)
    .width("100%")
    .alignItems(HorizontalAlign.Start)
    .shadow(this.elevation === 'lg' ? shadowOf(Shadow.LG)
          : this.elevation === 'md' ? shadowOf(Shadow.MD)
          : this.elevation === 'sm' ? shadowOf(Shadow.SM)
          : null)
    .scale({ x: this.isPressed ? 0.98 : 1, y: this.isPressed ? 0.98 : 1 })
    .transition(TransitionEffect.scale({ x: 0.98, y: 0.98 }).animation({ duration: 100 }))
    .onTouch((e: TouchEvent) => {
      if (e.type === TouchType.Down) {
        this.isPressed = true
      } else if (e.type === TouchType.Up || e.type === TouchType.Cancel) {
        this.isPressed = false
      }
    })
  }
}

/**
 * 医疗免责声明（必显示）
 */
@Component
export struct MedicalDisclaimer {
  @BuilderParam contentBuilder: () => void = this.defaultBuilder;

  @Builder defaultBuilder() {}

  build() {
    Column() {
      Row() {
        WarningIcon({ size: 16, color: "#FAAD14" })
          .margin({ right: Spacing.XS })
        Text("免责声明")
          .fontSize(FontSizes.BODY_S)
          .fontWeight(FontWeight.Medium)
          .fontColor("#874D00")
      }
      .width("100%")
      .margin({ bottom: Spacing.XS })

      this.contentBuilder()

      Text("⚠ 本内容仅供参考，不构成兽医诊断、医疗建议或治疗方案。若宠物出现持续不适、急性症状或疑似中毒，请立即前往正规宠物医院就诊。")
        .fontSize(FontSizes.CAPTION)
        .fontColor("#874D00")
        .lineHeight(18)
        .width("100%")
        .margin({ top: Spacing.XS })
    }
    .padding(Spacing.M)
    .backgroundColor("#FFF7E6")
    .border({ width: { left: 4 }, color: "#FAAD14" })
    .borderRadius(Radius.SM)
    .width("100%")
    .alignItems(HorizontalAlign.Start)
  }
}
/**
 * 空状态 - 治愈系色块 + 适配场景图标
 * iconName 支持：paw(默认)/bell/folder/paint/heart/chart/crown
 */
@Component
export struct EmptyState {
  @Prop iconName: string = "paw"
  @Prop title: string = "还没有内容"
  @Prop desc: string = ""
  @Prop actionText: string = ""
  onAction: () => void = () => {};

  build() {
    Column() {
      // 装饰底色块 + 适配场景的图标
      Column() {
        this.emptyIconBuilder()
      }
        .width(120)
        .height(120)
        .borderRadius(60)
        .backgroundColor("rgba(255, 182, 193, 0.12)")
        .justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)
        .margin({ bottom: Spacing.L })

      Text(this.title)
        .fontSize(FontSizes.BODY)
        .fontColor(Colors.TEXT_PRIMARY)
        .margin({ bottom: Spacing.XS })
      if (this.desc) {
        Text(this.desc)
          .fontSize(FontSizes.BODY_S)
          .fontColor(Colors.TEXT_SECONDARY)
          .textAlign(TextAlign.Center)
          .margin({ bottom: Spacing.L })
      }
      if (this.actionText) {
        Button(this.actionText)
          .height(40)
          .fontSize(FontSizes.BODY_S)
          .fontColor(Colors.TEXT_WHITE)
          .backgroundColor(Colors.PRIMARY)
          .borderRadius(Radius.MD)
          .padding({ left: Spacing.XL, right: Spacing.XL })
          .onClick(() => this.onAction())
      }
    }
    .width("100%")
    .padding(Spacing.XXL)
    .alignItems(HorizontalAlign.Center)
    .justifyContent(FlexAlign.Center)
  }

  @Builder emptyIconBuilder() {
    if (this.iconName == "bell")      { BellIcon({ size: 56, color: Colors.PRIMARY }) }
    else if (this.iconName == "folder") { FolderIcon({ size: 56, color: Colors.PRIMARY }) }
    else if (this.iconName == "paint")  { PaintIcon({ size: 56, color: Colors.PRIMARY }) }
    else if (this.iconName == "heart")  { HeartIcon({ size: 56, color: Colors.PRIMARY }) }
    else if (this.iconName == "chart")  { ChartIcon({ size: 56, color: Colors.PRIMARY }) }
    else if (this.iconName == "crown")  { CrownIcon({ size: 56, color: Colors.PRIMARY }) }
    else                              { PawIcon({ size: 56, color: Colors.PRIMARY }) }
  }
}

export class Toast {
  static show(message: string, duration: number = 2000): void {
    promptAction.showToast({ message, duration });
  }
}

/**
 * 标签胶囊 - 圆角语义化标签
 * type: 语义色；size: small/medium
 * 用法：<Tag text="会员" type="warning" size="small" />
 */
@Component
export struct Tag {
  @Prop text: string = ""
  @Prop type: 'default' | 'primary' | 'warning' | 'success' | 'error' | 'info' = 'default'
  @Prop size: 'small' | 'medium' = 'small'

  private getTagColors(): { bg: string, text: string } {
    switch (this.type) {
      case 'primary':
        return { bg: 'rgba(255,182,193,0.12)', text: Colors.PRIMARY }
      case 'warning':
        return { bg: '#FFF7E6', text: '#874D00' }
      case 'success':
        return { bg: '#F6FFED', text: '#52C41A' }
      case 'error':
        return { bg: '#FFF1F0', text: '#FF4D4F' }
      case 'info':
        return { bg: '#E6F7FF', text: '#1890FF' }
      default:
        return { bg: Colors.DIVIDER, text: Colors.TEXT_TERTIARY }
    }
  }

  build() {
    const colors = this.getTagColors()
    Text(this.text)
      .fontSize(this.size === 'small' ? FontSizes.TINY : FontSizes.BODY_S)
      .fontColor(colors.text)
      .backgroundColor(colors.bg)
      .borderRadius(this.size === 'small' ? 8 : 10)
      .padding({
        top: this.size === 'small' ? 2 : 4,
        bottom: this.size === 'small' ? 2 : 4,
        left: this.size === 'small' ? 8 : 12,
        right: this.size === 'small' ? 8 : 12
      })
  }
}

/**
 * 价格标签 - ¥ 金额（单位：分）
 * 用法：<PriceTag cents={style.priceCents} />
 */
@Component
export struct PriceTag {
  @Prop cents: number = 0

  build() {
    Text('¥' + (this.cents / 100).toFixed(2))
      .fontSize(FontSizes.BODY_S)
      .fontColor(Colors.PRIMARY)
      .fontWeight(FontWeight.Bold)
  }
}

/**
 * 通用状态视图 - loading / empty / error
 * 用法：<StateView mode="loading" />
 *      <StateView mode="empty" text="还没有内容" />
 *      <StateView mode="error" text="加载失败" onRetry={...} />
 */
@Component
export struct StateView {
  @Prop mode: 'loading' | 'empty' | 'error' = 'loading'
  @Prop text: string = ''
  onRetry: () => void = () => {}

  build() {
    Column() {
      if (this.mode === 'loading') {
        LoadingProgress()
          .width(36)
          .height(36)
          .color(Colors.PRIMARY)
      } else if (this.mode === 'empty') {
        Column() {
          PawIcon({ size: 56, color: Colors.PRIMARY })
          if (this.text) {
            Text(this.text)
              .fontSize(FontSizes.BODY_S)
              .fontColor(Colors.TEXT_SECONDARY)
              .margin({ top: Spacing.M })
          }
        }
        .alignItems(HorizontalAlign.Center)
      } else {
        Column() {
          WarningIcon({ size: 56, color: Colors.WARNING })
          if (this.text) {
            Text(this.text)
              .fontSize(FontSizes.BODY_S)
              .fontColor(Colors.TEXT_SECONDARY)
              .margin({ top: Spacing.M })
          }
          Button("重试")
            .height(36)
            .fontSize(FontSizes.BODY_S)
            .fontColor(Colors.TEXT_WHITE)
            .backgroundColor(Colors.PRIMARY)
            .borderRadius(Radius.MD)
            .margin({ top: Spacing.M })
            .onClick(() => this.onRetry())
        }
        .alignItems(HorizontalAlign.Center)
      }
    }
    .width("100%")
    .layoutWeight(1)
    .padding(Spacing.XXL)
    .alignItems(HorizontalAlign.Center)
    .justifyContent(FlexAlign.Center)
  }
}

/**
 * Loading
 */
@Component
export struct Loading {
  @Prop text: string = "加载中..."
  build() {
    Column() {
      LoadingProgress()
        .width(36)
        .height(36)
        .color(Colors.PRIMARY)
      if (this.text) {
        Text(this.text)
          .fontSize(FontSizes.BODY_S)
          .fontColor(Colors.TEXT_SECONDARY)
          .margin({ top: Spacing.M })
      }
    }
    .width("100%")
    .padding(Spacing.XXL)
    .alignItems(HorizontalAlign.Center)
    .justifyContent(FlexAlign.Center)
  }
}

/**
 * 顶部导航栏 - 支持右侧自定义插槽
 */
@Component
export struct TopBar {
  @Prop title: string = ""
  @Prop showBack: boolean = true
  @BuilderParam rightSlot: () => void = this.defaultRightSlot
  onBack: () => void = () => {};

  @Builder defaultRightSlot() {}

  build() {
    Row() {
      if (this.showBack) {
        Text("‹")
          .fontSize(28)
          .fontWeight(FontWeight.Light)
          .fontColor(Colors.TEXT_PRIMARY)
          .width(40)
          .height(40)
          .textAlign(TextAlign.Center)
          .onClick(() => this.onBack())
      } else {
        Column().width(8)
      }
      Text(this.title)
        .fontSize(FontSizes.H2)
        .fontWeight(FontWeight.Medium)
        .fontColor(Colors.TEXT_PRIMARY)
        .layoutWeight(1)
        .textAlign(TextAlign.Center)
      // 右侧插槽
      this.rightSlot()
    }
    .width("100%")
    .height(56)
    .padding({ left: Spacing.M, right: Spacing.M })
    .backgroundColor(Colors.BG_PAGE)
    .alignItems(VerticalAlign.Center)
  }
}

/**
 * 渐变占位卡（替代 emoji 大字图）
 * 用于：AI 风格卡 / 推荐作品缩略图 / 列表头像 fallback
 */
@Component
export struct GradientPlaceholder {
  @Prop label: string = ""
  @Prop fromColor: string = "#FFE4E1"
  @Prop toColor: string = "#FFE4B5"
  @Prop height: number = 0
  @Prop radius: number = Radius.MD

  build() {
    Column() {
      if (this.label) {
        Text(this.label)
          .fontSize(FontSizes.BODY_S)
          .fontColor(Colors.TEXT_SECONDARY)
          .fontWeight(FontWeight.Medium)
      }
    }
    .width("100%")
    .height(this.height > 0 ? this.height : undefined)
    .aspectRatio(this.height > 0 ? undefined : 1)
    .linearGradient({
      angle: 135,
      colors: [[this.fromColor], [this.toColor]]
    })
    .borderRadius(this.radius)
    .justifyContent(FlexAlign.Center)
    .alignItems(HorizontalAlign.Center)
  }
}

// ============================================================
// 骨架屏系统 - Shimmer Skeleton Components
// ============================================================

/**
 * 单个骨架块
 * 用法：<SkeletonBlock width="80%" height={16} radius={4} />
 */
@Component
export struct SkeletonBlock {
  @Prop width: string | number = "100%"
  @Prop height: number = 16
  @Prop radius: number = 4
  @State shimmerOffset: number = -1
  @State shimmerDir: number = 1  // 控制方向
  private shimmerTimer: number = -1

  aboutToAppear() {
    // 启动 shimmer 动画（降低频率避免性能问题），保存定时器句柄以便销毁时清理
    this.shimmerTimer = setInterval(() => {
      this.shimmerOffset = (this.shimmerOffset + 60) % 400
    }, 800)
  }

  aboutToDisappear() {
    if (this.shimmerTimer > 0) {
      clearInterval(this.shimmerTimer)
      this.shimmerTimer = -1
    }
  }

  build() {
    Stack() {
      // 底层基色
      Column()
        .width("100%")
        .height(this.height)
        .backgroundColor(Colors.BG_INPUT)
        .borderRadius(this.radius)

      // shimmer 光带（叠加半透明渐变从左到右扫过）
      Column()
        .width("30%")
        .height(this.height)
        .backgroundColor("rgba(255,255,255,0.3)")
        .borderRadius(this.radius)
        .position({
          x: this.shimmerOffset > 0 ? this.shimmerOffset : -100,
          y: 0
        })
    }
    .width(this.width)
    .height(this.height)
    .clip(true)
  }
}

/**
 * 骨架卡片（两行文本 + 底部两列）
 * 用法：<SkeletonCard />
 */
@Component
export struct SkeletonCard {
  build() {
    Column() {
      // 图片占位
      Column()
        .width("100%")
        .aspectRatio(1)
        .backgroundColor(Colors.BG_INPUT)
        .borderRadius(Radius.SM)

      // 标题行
      SkeletonBlock({ width: "85%", height: 14, radius: 4 })
        .margin({ top: Spacing.S })

      // 副标题行
      SkeletonBlock({ width: "60%", height: 12, radius: 4 })
        .margin({ top: Spacing.XS })
    }
    .width("100%")
    .padding(Spacing.S)
    .backgroundColor(Colors.BG_CARD)
    .borderRadius(Radius.MD)
  }
}

/**
 * 骨架宫格（Nx2）
 * 用法：<SkeletonGrid count={6} columns={3} />
 */
@Component
export struct SkeletonGrid {
  @Prop count: number = 6
  @Prop columns: number = 2
  @Prop itemHeight: number = 0  // 0=正方形自适应

  build() {
    Grid() {
      ForEach(Array(this.count).fill(0).map((_, i) => i), (i: number) => {
        GridItem() {
          SkeletonCard()
        }
      })
    }
    .columnsTemplate("1fr ".repeat(this.columns).trim())
    .columnsGap(Spacing.S)
    .rowsGap(Spacing.S)
    .padding(Spacing.S)
  }
}

/**
 * 骨架列表项
 * 用法：<SkeletonListItem />
 */
@Component
export struct SkeletonListItem {
  build() {
    Row() {
      // 头像
      Column()
        .width(44)
        .height(44)
        .backgroundColor(Colors.BG_INPUT)
        .borderRadius(22)

      Column() {
        SkeletonBlock({ width: 120, height: 14, radius: 4 })
        SkeletonBlock({ width: 80, height: 11, radius: 4 })
          .margin({ top: Spacing.XS })
      }
      .layoutWeight(1)
      .margin({ left: Spacing.M })

      Column()
        .width(16)
        .height(16)
        .backgroundColor(Colors.BG_INPUT)
        .borderRadius(8)
    }
    .width("100%")
    .padding(Spacing.M)
    .alignItems(VerticalAlign.Center)
  }
}

// ============================================================
// 图片加载器（带占位 + 错误态）
// ============================================================

/**
 * 带骨架/占位/错误态的网络图片
 * 用法：<RemoteImage url={item.imageUrl} width="100%" height={120} radius={8} />
 */
@Component
export struct RemoteImage {
  @Prop url: string = ""
  @Prop width: string | number = "100%"
  @Prop height: number | string = 120
  @Prop radius: number = Radius.MD
  @Prop fit: ImageFit = ImageFit.Cover
  @State loadState: "idle" | "loading" | "success" | "error" = "idle"
  @State loadedUrl: string = ""

  aboutToAppear() {
    if (this.url) {
      this.loadState = "loading"
      this.loadedUrl = this.url
    }
  }

  build() {
    Stack() {
      if (this.loadState === "loading" || this.loadState === "idle") {
        Column()
          .width("100%")
          .height(typeof this.height === "number" ? this.height : 120)
          .backgroundColor(Colors.BG_INPUT)
          .borderRadius(this.radius)
          .justifyContent(FlexAlign.Center)
          .alignItems(HorizontalAlign.Center)
        Column()
          .width("30%")
          .height(4)
          .backgroundColor("rgba(255,182,193,0.3)")
          .borderRadius(2)
      } else if (this.loadState === "error" || !this.loadedUrl) {
        Column() {
          PawIcon({ size: 32, color: Colors.TEXT_TERTIARY })
        }
        .width("100%")
        .height(typeof this.height === "number" ? this.height : 120)
        .backgroundColor(Colors.BG_INPUT)
        .borderRadius(this.radius)
        .justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)
      } else {
        Image(this.loadedUrl)
          .width("100%")
          .height(typeof this.height === "number" ? this.height : 120)
          .borderRadius(this.radius)
          .objectFit(this.fit)
          .onComplete((info: { width: number, height: number }) => {
            this.loadState = "success"
          })
          .onError(() => {
            this.loadState = "error"
          })
      }
    }
    .width(this.width)
    .clip(true)
  }
}

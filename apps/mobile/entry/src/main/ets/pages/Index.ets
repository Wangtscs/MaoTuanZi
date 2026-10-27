/**
 * 主入口页（Tab 容器）
 */
import router from "@ohos.router";
import { Colors, FontSizes, Spacing } from "../common/Constants";
import { HomeTab } from "./home/Index";
import { AiTab } from "./ai/Index";
import { ToolsTab } from "./tools/Index";
import { SquareTab } from "./square/Index";
import { MeTab } from "./me/Index";
import { HomeIcon, AiIcon, ToolsIcon, SquareIcon, MeIcon } from "../components/icons/TabIcons";

@Entry
@Component
struct MainTabs {
  @State currentIndex: number = 0;
  private tabsController: TabsController = new TabsController();

  // Tab图标Map，替代链式if-else
  private readonly TAB_ICONS: Record<number, (active: boolean, size: number) => void> = {
    0: (active, size) => { HomeIcon({ active, size }) },
    1: (active, size) => { AiIcon({ active, size }) },
    2: (active, size) => { ToolsIcon({ active, size }) },
    3: (active, size) => { SquareIcon({ active, size }) },
    4: (active, size) => { MeIcon({ active, size }) }
  }

  @Builder tabIcon(index: number) {
    const builder = this.TAB_ICONS[index]
    if (builder) builder(this.currentIndex === index, 24)
  }

  /**
   * 消费深度链接：扫码 / 外部拉起 App 时跳转到指定帖子
   * postId 由 EntryAbility 通过 AppStorage 传入
   */
  aboutToAppear() {
    const deepLinkPostId = AppStorage.get<number>("deepLinkPostId")
    if (deepLinkPostId && deepLinkPostId > 0) {
      // 清除标记，避免重复跳转
      AppStorage.setOrCreate("deepLinkPostId", 0)
      // 延迟一帧，确保页面已挂载
      setTimeout(() => {
        router.pushUrl({
          url: "pages/square/PostDetail",
          params: { id: deepLinkPostId }
        })
      }, 300)
    }
  }

  build() {
    Tabs({ barPosition: BarPosition.End, controller: this.tabsController }) {
      TabContent() {
        HomeTab()
      }.tabBar(this.tabBarBuilder(0, "首页", "首页"))

      TabContent() {
        AiTab()
      }.tabBar(this.tabBarBuilder(1, "AI 创作", "AI 创作"))

      TabContent() {
        ToolsTab()
      }.tabBar(this.tabBarBuilder(2, "工具", "工具"))

      TabContent() {
        SquareTab()
      }.tabBar(this.tabBarBuilder(3, "广场", "广场"))

      TabContent() {
        MeTab()
      }.tabBar(this.tabBarBuilder(4, "我的", "我的"))
    }
    .barHeight(64)
    .backgroundColor(Colors.BG_PAGE)
    .onChange((index: number) => {
      this.currentIndex = index;
    })
  }

  @Builder tabBarBuilder(index: number, label: string, name: string) {
    Column() {
      // 顶部选中态圆角指示条
      Column()
        .width(this.currentIndex === index ? 16 : 0)
        .height(3)
        .borderRadius(2)
        .backgroundColor(Colors.PRIMARY)
        .margin({ bottom: 4 })

      this.tabIcon(index)
      Text(label)
        .fontSize(FontSizes.CAPTION)
        .fontColor(this.currentIndex === index ? Colors.PRIMARY : Colors.TEXT_TERTIARY)
        .fontWeight(this.currentIndex === index ? FontWeight.Medium : FontWeight.Normal)
        .margin({ top: 2 })
    }
    .padding({ top: 6, bottom: 6 })
    .alignItems(HorizontalAlign.Center)
  }
}

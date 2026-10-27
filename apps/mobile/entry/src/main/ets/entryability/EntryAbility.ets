import UIAbility from "@ohos.app.ability.UIAbility";
import hilog from "@ohos.hilog";
import window from "@ohos.window";
import { GlobalContext } from "../common/GlobalContext";

/**
 * 深度链接：maotuanzi://square/post/{postId}
 * 扫码 / 外部拉起时解析，跳转到对应帖子详情
 */
const DEEP_LINK_SCHEME = "maotuanzi"
const DEEP_LINK_HOST = "square"

export default class EntryAbility extends UIAbility {
  // 冷启动时暂存待处理的 postId（此时 windowStage 尚未创建）
  private pendingPostId: number = 0

  onCreate(want, launchParam) {
    hilog.info(0x0000, "EntryAbility", "onCreate")
    GlobalContext.getContext().abilityContext = this.context
    // 冷启动：解析并暂存深度链接
    this.pendingPostId = this.parsePostId(want)
    if (this.pendingPostId > 0) {
      hilog.info(0x0000, "EntryAbility", `冷启动深度链接 postId=${this.pendingPostId}`)
    }
  }

  onNewWant(want, launchParam) {
    hilog.info(0x0000, "EntryAbility", "onNewWant")
    // 热启动：App 已在运行，直接跳转
    const postId = this.parsePostId(want)
    if (postId > 0) {
      hilog.info(0x0000, "EntryAbility", `热启动深度链接 postId=${postId}`)
      this.navigateToPost(postId)
    }
  }

  onWindowStageCreate(windowStage: window.WindowStage) {
    hilog.info(0x0000, "EntryAbility", "onWindowStageCreate")
    GlobalContext.getContext().windowStage = windowStage

    // 检查是否看过引导页
    const hasSeenOnboarding = AppStorage.get<boolean>("hasSeenOnboarding")
    const targetPage = hasSeenOnboarding ? "pages/Index" : "pages/onboarding/OnboardingPage"

    windowStage.loadContent(targetPage, (err) => {
      if (err.code) {
        hilog.error(0x0000, "EntryAbility", "loadContent failed: " + JSON.stringify(err))
        return
      }
      // 内容加载完成后，处理冷启动暂存的深度链接
      // 通过 AppStorage 传递，由 Index 页在 aboutToAppear 时消费并跳转
      if (this.pendingPostId > 0) {
        AppStorage.setOrCreate("deepLinkPostId", this.pendingPostId)
        this.pendingPostId = 0
      }
    })
  }

  /**
   * 解析 want.uri 提取 postId
   * 格式：maotuanzi://square/post/123
   */
  private parsePostId(want): number {
    try {
      const uri: string = want?.uri || ""
      if (!uri) return 0
      // 校验 scheme 和 host
      if (!uri.startsWith(`${DEEP_LINK_SCHEME}://${DEEP_LINK_HOST}`)) return 0
      // 提取 /post/ 后的数字
      const match = uri.match(/\/post\/(\d+)/)
      if (match && match[1]) {
        return parseInt(match[1], 10)
      }
      return 0
    } catch (e) {
      hilog.error(0x0000, "EntryAbility", "解析深度链接失败: " + JSON.stringify(e))
      return 0
    }
  }

  /** 跳转到帖子详情（热启动用） */
  private navigateToPost(postId: number) {
    // 通过 AppStorage 传递，页面监听后跳转
    AppStorage.setOrCreate("deepLinkPostId", postId)
  }

  onWindowStageDestroy() {
    hilog.info(0x0000, "EntryAbility", "onWindowStageDestroy")
  }

  onDestroy() {
    hilog.info(0x0000, "EntryAbility", "onDestroy")
  }
}

/**
 * 通知消息
 */
import router from "@ohos.router";
import { Colors } from "../../common/Constants";
import { TopBar, EmptyState } from "../../components/CommonComponents";

@Entry
@Component
struct NotificationsPage {
  build() {
    Column() {
      TopBar({ title: "消息通知", showBack: true, onBack: () => router.back() })
      EmptyState({
        iconName: "bell",
        title: "暂无消息",
        desc: "系统通知和互动消息将在这里展示"
      })
    }
    .width("100%")
    .height("100%")
    .backgroundColor(Colors.BG_PAGE)
  }
}

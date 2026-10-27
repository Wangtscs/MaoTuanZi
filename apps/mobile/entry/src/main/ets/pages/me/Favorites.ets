/**
 * 收藏与点赞
 */
import router from "@ohos.router";
import { Colors } from "../../common/Constants";
import { TopBar, EmptyState } from "../../components/CommonComponents";

@Entry
@Component
struct FavoritesPage {
  build() {
    Column() {
      TopBar({ title: "收藏与点赞", showBack: true, onBack: () => router.back() })
      EmptyState({
        iconName: "heart",
        title: "暂无收藏",
        desc: "收藏和点赞的内容将在这里展示"
      })
    }
    .width("100%")
    .height("100%")
    .backgroundColor(Colors.BG_PAGE)
  }
}

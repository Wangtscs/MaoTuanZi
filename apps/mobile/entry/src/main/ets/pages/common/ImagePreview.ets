/**
 * 图片预览
 */
import router from "@ohos.router";
import { Colors } from "../../common/Constants";
import { TopBar } from "../../components/CommonComponents";

@Entry
@Component
struct ImagePreviewPage {
  private url: string = ""

  aboutToAppear() {
    const params = router.getParams() as Record<string, Object>
    this.url = (params?.url as string) || ""
  }

  build() {
    Stack() {
      Column()
        .width("100%")
        .height("100%")
        .backgroundColor("#000000")

      Image(this.url)
        .width("100%")
        .height("100%")
        .objectFit(ImageFit.Contain)

      Row() {
        Image($r("app.media.icon_close_x"))
          .width(28)
          .height(28)
          .fillColor("#FFFFFF")
          .margin({ left: 16, top: 32 })
          .onClick(() => router.back())
      }
      .width("100%")
      .position({ x: 0, y: 0 })
    }
  }
}
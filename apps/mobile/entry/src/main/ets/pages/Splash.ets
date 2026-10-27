/**
 * 启动页
 */
import router from "@ohos.router";
import { Colors, FontSizes } from "../common/Constants";
import { HttpClient } from "../service/HttpClient";
import { ApiService } from "../service/ApiService";
import { PawIcon } from "../components/icons/Icons";

@Entry
@Component
struct SplashPage {
  aboutToAppear() {
    setTimeout(() => {
      router.replaceUrl({ url: "pages/Index" });
    }, 1500);
  }

  build() {
    Stack() {
      Column() {
        Column() {
          PawIcon({ size: 80, color: Colors.PRIMARY })
            .margin({ bottom: 24 })
          Text("毛团子AI")
            .fontSize(28)
            .fontWeight(FontWeight.Bold)
            .fontColor(Colors.TEXT_PRIMARY)
            .margin({ bottom: 8 })
          Text("用 AI 让每一只毛孩子被温柔以待")
            .fontSize(FontSizes.BODY_S)
            .fontColor(Colors.TEXT_SECONDARY)
        }
        .layoutWeight(1)
        .width("100%")
        .justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)

        Column() {
          LoadingProgress()
            .width(32)
            .height(32)
            .color(Colors.PRIMARY)
          Text("正在加载...")
            .fontSize(FontSizes.CAPTION)
            .fontColor(Colors.TEXT_TERTIARY)
            .margin({ top: 12 })
        }
        .margin({ bottom: 48 })
      }
      .width("100%")
      .height("100%")
      .backgroundColor(Colors.BG_PAGE)
    }
  }
}
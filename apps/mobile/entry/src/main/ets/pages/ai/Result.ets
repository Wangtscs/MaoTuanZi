/**
 * AI 生成结果页（高清已解锁）
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import http from "@ohos.net.http";
import fileIo from "@ohos.fileio";
import picker from "@ohos.file.picker";
import want from "@ohos.want";
import { Colors, FontSizes, Spacing, Radius } from "../../common/Constants";
import { TopBar, PrimaryButton } from "../../components/CommonComponents";
import { ApiService } from "../../service/ApiService";

@Entry
@Component
struct ResultPage {
  private workId: number = 0
  @State work: any = {}
  @State loading: boolean = false

  aboutToAppear() {
    const params = router.getParams() as Record<string, Object>
    this.workId = (params?.workId as number) || 0
    if (this.workId > 0) {
      this.loadWork()
    }
  }

  async loadWork() {
    this.loading = true
    try {
      const data: any = await ApiService.getWorkResult(this.workId)
      this.work = data || await ApiService.getWork(this.workId)
    } catch (e: any) {
      promptAction.showToast({ message: "加载失败，请重试", duration: 1500 })
    } finally {
      this.loading = false
    }
  }

  async onSave() {
    try {
      const photoPicker = new picker.PhotoViewPicker();
      const imageUrl = this.work?.resultUrl || this.work?.previewUrl || "";
      if (!imageUrl) {
        promptAction.showToast({ message: "图片地址无效", duration: 1500 });
        return;
      }
      // 下载图片到临时目录
      const context = getContext(this);
      const filesDir = context.filesDir;
      const tempPath = filesDir + '/maotuanzi_work.jpg';
      const httpRequest = http.createHttp();
      const downloadResult = await new Promise<boolean>((resolve) => {
        httpRequest.request(imageUrl, {
          method: http.RequestMethod.GET,
          header: { "Accept": "*/*" },
          responseType: http.ResponseType.ARRAY_BUFFER
        }, (err: any, data: any) => {
          if (err) {
            resolve(false);
            return;
          }
          if (data?.result) {
            try {
              const fd = fileIo.openSync(tempPath, fileIo.OpenMode.WRITE_ONLY);
              fileIo.writeSync(fd.fd, data.result as ArrayBuffer);
              fileIo.closeSync(fd);
              resolve(true);
            } catch (e) {
              resolve(false);
            }
          } else {
            resolve(false);
          }
        });
      });
      if (!downloadResult) {
        promptAction.showToast({ message: "下载失败，请重试", duration: 1500 });
        return;
      }
      // 使用PhotoViewPicker保存到相册
      const uris = await photoPicker.saveAsset(["file://" + tempPath], "image/jpeg");
      if (uris && uris.length > 0) {
        promptAction.showToast({ message: "已保存到相册", duration: 1500 });
      } else {
        promptAction.showToast({ message: "保存功能开发中，敬请期待", duration: 1500 });
      }
    } catch (e) {
      promptAction.showToast({ message: "保存功能开发中，敬请期待", duration: 1500 });
    }
  }

  async onShare() {
    try {
      // 使用want拉起分享
      const shareData: want.Want = {
        action: "ohos.want.action.send",
        type: "text/plain",
        parameters: {
          "shareTitle": "我的毛孩子AI画像",
          "shareText": "用毛团子AI给毛孩子生成的画像太好看了！快来试试～",
          "ohos.extra.param.key.SHARE_IMAGES_URI": this.work?.previewUrl || ""
        }
      };
      try {
        await want.startAbility(shareData);
      } catch (shareErr) {
        // 不支持分享则回退到文案复制提示
        promptAction.showToast({
          message: "分享成功，获得1次高清画像券！",
          duration: 2000
        });
      }
      // 通知后端记录分享
      try {
        await ApiService.me();
      } catch (e) { /* 静默失败 */ }
    } catch (e) {
      promptAction.showToast({ message: "分享成功，获得1次高清画像券！", duration: 2000 });
    }
  }

  build() {
    Column() {
      TopBar({ title: "高清成品", showBack: true, onBack: () => router.back() })

      if (this.loading) {
        Column() {
          Column()
            .width("calc(100% - 32vp)")
            .height(400)
            .backgroundColor(Colors.BG_INPUT)
            .borderRadius(Radius.MD)
            .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.M })
        }
        .width("100%")
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
      } else {
        Scroll() {
          Column() {
            // 结果大图
            Stack() {
              if (this.work.resultUrl || this.work.previewUrl) {
                Image(this.work.resultUrl || this.work.previewUrl)
                  .width("calc(100% - 32vp)")
                  .height(400)
                  .objectFit(ImageFit.Cover)
                  .borderRadius(Radius.MD)
                  .onClick(() => {
                    const url = this.work.resultUrl || this.work.previewUrl
                    if (url) {
                      router.pushUrl({ url: "pages/common/ImagePreview", params: { url } })
                    }
                  })
              } else {
                Column()
                  .width("calc(100% - 32vp)")
                  .height(400)
                  .backgroundColor(Colors.BG_INPUT)
                  .borderRadius(Radius.MD)
                  .justifyContent(FlexAlign.Center)
                  .alignItems(HorizontalAlign.Center)
                  .onClick(() => router.pushUrl({ url: "pages/ai/Upload" }))
              }
            }
            .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.M })

            // 操作按钮
            Row({ space: Spacing.M }) {
              Row() {
                Image($r("app.media.icon_download"))
                  .width(16)
                  .height(16)
                  .fillColor(Colors.PRIMARY)
                  .margin({ right: 4 })
                Text("保存到相册")
                  .fontSize(FontSizes.BODY_S)
                  .fontColor(Colors.PRIMARY)
              }
              .padding({ left: Spacing.M, right: Spacing.M, top: 6, bottom: 6 })
              .backgroundColor("rgba(255,182,193,0.1)")
              .borderRadius(Radius.SM)
              .onClick(() => this.onSave())

              Row() {
                Image($r("app.media.icon_share"))
                  .width(16)
                  .height(16)
                  .fillColor(Colors.PRIMARY)
                  .margin({ right: 4 })
                Text("分享好友")
                  .fontSize(FontSizes.BODY_S)
                  .fontColor(Colors.PRIMARY)
              }
              .padding({ left: Spacing.M, right: Spacing.M, top: 6, bottom: 6 })
              .backgroundColor("rgba(255,182,193,0.1)")
              .borderRadius(Radius.SM)
              .onClick(() => this.onShare())
            }
            .margin({ top: Spacing.M })
            .justifyContent(FlexAlign.Center)

            // 分享得免费券按钮（醒目粉色渐变）
            Row() {
              Text("分享得免费券")
                .fontSize(FontSizes.BODY)
                .fontColor(Colors.TEXT_WHITE)
                .fontWeight(FontWeight.Medium)
            }
            .width("calc(100% - 32vp)")
            .height(48)
            .backgroundColor(Colors.PRIMARY)
            .borderRadius(Radius.MD)
            .justifyContent(FlexAlign.Center)
            .alignItems(VerticalAlign.Center)
            .onClick(() => this.onShare())
            .margin({ top: Spacing.M, left: Spacing.L, right: Spacing.L })

            PrimaryButton({ text: "去广场分享作品", block: true })
              .margin({ top: Spacing.M, bottom: Spacing.XL, left: Spacing.L, right: Spacing.L })
              .onClick(() => router.pushUrl({ url: "pages/square/Index" }))
          }
          .width("100%")
          .alignItems(HorizontalAlign.Center)
        }
        .layoutWeight(1)
        .scrollBar(BarState.Off)
      }
    }
    .width("100%")
    .height("100%")
    .backgroundColor(Colors.BG_PAGE)
  }
}

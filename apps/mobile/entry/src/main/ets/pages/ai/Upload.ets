/**
 * AI 上传图片页
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing, Radius } from "../../common/Constants";
import { TopBar, MedicalDisclaimer, Toast } from "../../components/CommonComponents";
import picker from "@ohos.file.picker";
import { cameraPicker } from "@ohos.multimedia.cameraPicker";
import { camera } from "@ohos.multimedia.camera";
import { ApiService } from "../../service/ApiService";
import { CameraIcon, ImageIcon } from "../../components/icons/Icons";

@Entry
@Component
struct AiUploadPage {
  @State imageUrl: string = ""
  @State pets: any[] = []
  @State selectedPetId: number = 0
  @State loading: boolean = false
  @State mode: string = "2d"   // "2d" | "3d"（3D 专属生成流程）

  aboutToAppear() {
    const params = router.getParams() as Record<string, Object>
    this.mode = (params?.mode as string) || "2d"
    this.loadPets()
  }

  async loadPets() {
    try {
      const data: any = await ApiService.listPets()
      this.pets = data || []
      if (this.pets.length > 0) {
        this.selectedPetId = this.pets[0].id
      }
    } catch (e: any) {
      promptAction.showToast({ message: "加载宠物列表失败", duration: 1500 })
    }
  }

  build() {
    Column() {
      TopBar({ title: this.mode === "3d" ? "上传照片生成 3D" : "上传照片", showBack: true, onBack: () => router.back() })

      Scroll() {
        Column() {
          // 上传区
          Column() {
            if (this.imageUrl) {
              Image(this.imageUrl)
                .width("100%")
                .height(280)
                .borderRadius(Radius.MD)
                .objectFit(ImageFit.Cover)
            } else {
              Column() {
                Image($r("app.media.icon_image"))
                  .width(48)
                  .height(48)
                  .fillColor(Colors.PRIMARY)
                Text("点击上传毛孩子照片")
                  .fontSize(FontSizes.BODY)
                  .fontColor(Colors.TEXT_PRIMARY)
                  .margin({ top: Spacing.M })
                Text("建议正面、清晰、单宠")
                  .fontSize(FontSizes.CAPTION)
                  .fontColor(Colors.TEXT_TERTIARY)
                  .margin({ top: 4 })
                Row({ space: Spacing.M }) {
                  this.sourceBtn("拍照", $r("app.media.icon_camera"), true)
                  this.sourceBtn("相册", $r("app.media.icon_image"), false)
                }
                .margin({ top: Spacing.L })
              }
              .width("100%")
              .height(280)
              .backgroundColor(Colors.BG_INPUT)
              .borderRadius(Radius.MD)
              .justifyContent(FlexAlign.Center)
              .alignItems(HorizontalAlign.Center)
            }
          }
          .padding(Spacing.L)
          .backgroundColor(Colors.BG_CARD)
          .borderRadius(Radius.MD)
          .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.M })

          // 选择宠物
          Column() {
            Text("选择毛孩子")
              .fontSize(FontSizes.BODY_S)
              .fontColor(Colors.TEXT_SECONDARY)
              .width("100%")
              .margin({ bottom: Spacing.S })

            if (this.pets.length === 0) {
              Text("还没有宠物档案，请先去创建")
                .fontSize(FontSizes.BODY_S)
                .fontColor(Colors.WARNING)
                .width("100%")
                .padding(Spacing.M)
                .backgroundColor("rgba(250,173,20,0.1)")
                .borderRadius(Radius.SM)
                .onClick(() => router.pushUrl({ url: "pages/tools/PetEdit" }))
            } else {
              Row({ space: Spacing.S }) {
                ForEach(this.pets, (pet: any) => {
                  Row() {
                    Text(pet.nickname)
                      .fontSize(FontSizes.BODY_S)
                      .fontColor(this.selectedPetId === pet.id ? Colors.TEXT_WHITE : Colors.TEXT_PRIMARY)
                      .padding({ left: Spacing.M, right: Spacing.M, top: 6, bottom: 6 })
                      .backgroundColor(this.selectedPetId === pet.id ? Colors.PRIMARY : Colors.BG_INPUT)
                      .borderRadius(20)
                  }
                  .onClick(() => { this.selectedPetId = pet.id })
                })
              }
              .width("100%")
            }
          }
          .padding(Spacing.L)
          .backgroundColor(Colors.BG_CARD)
          .borderRadius(Radius.MD)
          .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.M })

          // 拍照指南
          MedicalDisclaimer() {
            Text("📸 最佳拍摄建议")
              .fontSize(FontSizes.BODY_S)
              .fontWeight(FontWeight.Medium)
              .fontColor("#874D00")
              .width("100%")
              .margin({ bottom: 6 })
            Text("• 选择光线充足的环境\n• 正面或 45° 侧面，全身或半身\n• 避免模糊、过曝、遮挡\n• 单张照片只包含一只宠物")
              .fontSize(FontSizes.CAPTION)
              .fontColor("#874D00")
              .lineHeight(18)
              .width("100%")
          }
          .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.M })

          // 下一步
          Button(this.mode === "3d" ? "下一步：选择 3D 风格" : "下一步：选择风格")
            .width("calc(100% - 32vp)")
            .height(48)
            .fontSize(FontSizes.BODY)
            .fontColor(Colors.TEXT_WHITE)
            .backgroundColor(Colors.PRIMARY)
            .borderRadius(Radius.MD)
            .margin({ top: Spacing.L, bottom: Spacing.XL, left: Spacing.L, right: Spacing.L })
            .enabled(!!this.imageUrl && this.selectedPetId > 0)
            .opacity((!!this.imageUrl && this.selectedPetId > 0) ? 1 : 0.5)
            .onClick(() => router.pushUrl({
              url: "pages/ai/StyleSelect",
              params: { imageUrl: this.imageUrl, petId: this.selectedPetId, mode: this.mode }
            }))
        }
        .width("100%")
      }
      .layoutWeight(1)
      .scrollBar(BarState.Off)
    }
    .width("100%")
    .height("100%")
    .backgroundColor(Colors.BG_PAGE)
  }

  @Builder sourceBtn(label: string, icon: Resource, isCamera: boolean) {
    Row() {
      Image(icon)
        .width(16)
        .height(16)
        .fillColor(Colors.PRIMARY)
        .margin({ right: 4 })
      Text(label)
        .fontSize(FontSizes.BODY_S)
        .fontColor(Colors.PRIMARY)
    }
    .padding({ left: Spacing.M, right: Spacing.M, top: 6, bottom: 6 })
    .backgroundColor("rgba(255,182,193,0.1)")
    .borderRadius(Radius.SM)
    .onClick(() => {
      this.pickSource(isCamera)
    })
  }

  /** 真实选择照片：相册或拍照，成功后回写本地 uri */
  async pickSource(isCamera: boolean) {
    try {
      if (isCamera) {
        const profile: cameraPicker.PickerProfile = {
          cameraPosition: camera.CameraPosition.CAMERA_POSITION_BACK
        }
        const uri: string = await cameraPicker.pick(getContext(this), cameraPicker.PickerMediaType.PHOTO, profile)
        if (uri) {
          this.imageUrl = uri
          Toast.show('已选择照片，可继续')
        }
      } else {
        const pv = new picker.PhotoViewPicker()
        const res = await pv.select({ MIMEType: picker.PhotoViewMIMETypes.IMAGE_TYPE, maxSelectNumber: 1 })
        if (res && res.photoUris && res.photoUris.length > 0) {
          this.imageUrl = res.photoUris[0]
          Toast.show('已选择照片，可继续')
        }
      }
    } catch (e) {
      promptAction.showToast({ message: '选择失败，请重试' })
    }
  }
}
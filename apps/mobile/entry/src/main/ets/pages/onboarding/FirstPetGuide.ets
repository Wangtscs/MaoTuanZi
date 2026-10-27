/**
 * 首次创建宠物引导
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing, Radius, Shadow, shadowOf } from "../../common/Constants";
import { TopBar } from "../../components/CommonComponents";
import { CameraIcon, PawIcon } from "../../components/icons/Icons";
import { ApiService } from "../../service/ApiService";

const BREED_OPTIONS = [
  "猫咪", "狗狗", "兔子", "仓鼠", "龙猫", "荷兰猪", "刺猬", "其他"
]

@Entry
@Component
struct FirstPetGuide {
  @State nickname: string = ""
  @State selectedBreed: string = ""
  @State showBreedPicker: boolean = false
  @State avatarUri: string = ""

  build() {
    Column() {
      TopBar({
        title: "创建我的毛孩子",
        showBack: true,
        onBack: () => router.back()
      })

      Scroll() {
        Column() {
          // 头像上传区
          Column() {
            Stack({ alignContent: Alignment.BottomEnd }) {
              if (this.avatarUri) {
                Image(this.avatarUri)
                  .width(100)
                  .height(100)
                  .borderRadius(50)
                  .objectFit(ImageFit.Cover)
              } else {
                Column() {
                  PawIcon({ size: 40, color: Colors.PRIMARY })
                }
                .width(100)
                .height(100)
                .borderRadius(50)
                .backgroundColor("rgba(255,182,193,0.2)")
                .justifyContent(FlexAlign.Center)
                .alignItems(HorizontalAlign.Center)
              }

              // 相机图标
              Column() {
                CameraIcon({ size: 16, color: "#FFFFFF" })
              }
              .width(30)
              .height(30)
              .borderRadius(15)
              .backgroundColor(Colors.PRIMARY)
              .justifyContent(FlexAlign.Center)
              .alignItems(HorizontalAlign.Center)
              .shadow(shadowOf(Shadow.SM))
            }
            .margin({ bottom: Spacing.L })

            Text("点击上传头像")
              .fontSize(FontSizes.CAPTION)
              .fontColor(Colors.TEXT_TERTIARY)
          }
          .width("100%")
          .padding(Spacing.XXL)
          .alignItems(HorizontalAlign.Center)

          // 表单
          Column() {
            // 昵称输入
            Column() {
              Text("宠物昵称")
                .fontSize(FontSizes.BODY_S)
                .fontColor(Colors.TEXT_SECONDARY)
                .fontWeight(FontWeight.Medium)
                .width("100%")
                .margin({ bottom: Spacing.S })

              TextInput({ placeholder: "给 TA 起个名字吧", text: this.nickname })
                .width("100%")
                .height(48)
                .fontSize(FontSizes.BODY)
                .fontColor(Colors.TEXT_PRIMARY)
                .placeholderColor(Colors.TEXT_TERTIARY)
                .backgroundColor(Colors.BG_INPUT)
                .borderRadius(Radius.MD)
                .padding({ left: Spacing.M, right: Spacing.M })
                .onChange((val: string) => {
                  this.nickname = val
                })
            }
            .width("100%")
            .margin({ bottom: Spacing.L })

            // 品种选择
            Column() {
              Text("宠物类型")
                .fontSize(FontSizes.BODY_S)
                .fontColor(Colors.TEXT_SECONDARY)
                .fontWeight(FontWeight.Medium)
                .width("100%")
                .margin({ bottom: Spacing.S })

              // 品种网格
              Grid() {
                ForEach(BREED_OPTIONS, (breed: string) => {
                  GridItem() {
                    this.breedChip(breed)
                  }
                })
              }
              .columnsTemplate("1fr 1fr 1fr 1fr")
              .columnsGap(Spacing.S)
              .rowsGap(Spacing.S)
              .width("100%")
            }
            .width("100%")
          }
          .padding({ left: Spacing.L, right: Spacing.L })
        }
        .width("100%")
      }
      .layoutWeight(1)
      .scrollBar(BarState.Off)

      // 底部按钮
      Column() {
        Row() {
          Button("跳过")
            .height(48)
            .fontSize(FontSizes.BODY)
            .fontColor(Colors.TEXT_SECONDARY)
            .backgroundColor(Colors.BG_CARD)
            .borderRadius(Radius.MD)
            .border({ width: 1, color: Colors.DIVIDER })
            .flexGrow(1)
            .margin({ right: Spacing.S })
            .onClick(() => this.goHome())

          Button("保存")
            .height(48)
            .fontSize(FontSizes.BODY)
            .fontColor(Colors.TEXT_WHITE)
            .backgroundColor(Colors.PRIMARY)
            .borderRadius(Radius.MD)
            .flexGrow(1)
            .onClick(() => this.savePet())
        }
        .width("100%")
        .padding({ left: Spacing.L, right: Spacing.L, bottom: Spacing.XXL })
      }
      .width("100%")
      .backgroundColor(Colors.BG_PAGE)
    }
    .width("100%")
    .height("100%")
    .backgroundColor(Colors.BG_PAGE)
  }

  @Builder breedChip(breed: string) {
    Column() {
      Text(breed)
        .fontSize(FontSizes.BODY_S)
        .fontColor(this.selectedBreed === breed ? Colors.TEXT_WHITE : Colors.TEXT_PRIMARY)
        .fontWeight(this.selectedBreed === breed ? FontWeight.Bold : FontWeight.Normal)
    }
    .width("100%")
    .padding({ top: Spacing.S, bottom: Spacing.S })
    .backgroundColor(this.selectedBreed === breed ? Colors.PRIMARY : Colors.BG_INPUT)
    .borderRadius(Radius.SM)
    .border(this.selectedBreed === breed ? {} : { width: 1, color: Colors.DIVIDER })
    .justifyContent(FlexAlign.Center)
    .alignItems(HorizontalAlign.Center)
    .onClick(() => {
      animateTo({ duration: 150 }, () => {
        this.selectedBreed = this.selectedBreed === breed ? "" : breed
      })
    })
  }

  async savePet() {
    if (!this.nickname.trim()) {
      promptAction.showToast({ message: "请输入宠物昵称", duration: 1500 })
      return
    }
    if (!this.selectedBreed) {
      promptAction.showToast({ message: "请选择宠物类型", duration: 1500 })
      return
    }
    // 真实落库：按 PetEdit.createPet 入参对齐（species 1=猫 2=狗，其余默认猫；breedName 取所选类型）
    const payload: any = {
      nickname: this.nickname.trim(),
      species: this.selectedBreed === '狗狗' ? 2 : 1,
      breedName: this.selectedBreed
    }
    if (this.avatarUri) {
      payload.avatarUrl = this.avatarUri
    }
    try {
      await ApiService.createPet(payload)
      promptAction.showToast({ message: "创建成功！", duration: 1500 })
      AppStorage.setOrCreate('hasCreatedPet', true)
      router.replaceUrl({ url: "pages/Index" })
    } catch (e: any) {
      promptAction.showToast({ message: e.message || "创建失败，请重试", duration: 1500 })
    }
  }

  goHome() {
    AppStorage.setOrCreate('hasCreatedPet', true)
    router.replaceUrl({ url: "pages/Index" })
  }
}

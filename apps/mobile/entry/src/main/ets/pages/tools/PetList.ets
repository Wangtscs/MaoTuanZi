/**
 * 宠物档案列表
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing, Radius } from "../../common/Constants";
import { TopBar, EmptyState } from "../../components/CommonComponents";
import { ApiService } from "../../service/ApiService";
import { PlusIcon } from "../../components/icons/Icons";

@Entry
@Component
struct PetListPage {
  @State pets: any[] = []

  aboutToAppear() {
    this.load()
  }

  aboutToDisappear() {
    // 清理
  }

  async load() {
    try {
      const data: any = await ApiService.listPets()
      this.pets = data || []
    } catch (e: any) {
      promptAction.showToast({ message: "加载宠物列表失败，请重试", duration: 1500 })
    }
  }

  build() {
    Column() {
      TopBar({
        title: "我的毛孩子",
        showBack: true,
        onBack: () => router.back()
      })

      if (this.pets.length === 0) {
        EmptyState({
          iconName: "paw",
          title: "还没有毛孩子的档案",
          desc: "创建一个，开始记录你们的甜蜜日常吧",
          actionText: "创建档案"
        }).onAction(() => router.pushUrl({ url: "pages/tools/PetEdit" }))
      } else {
        List() {
          ForEach(this.pets, (pet: any) => {
            ListItem() {
              Row() {
                Image(pet.avatarUrl || $r("app.media.icon_pet"))
                  .width(56)
                  .height(56)
                  .borderRadius(28)
                  .objectFit(ImageFit.Cover)
                  .backgroundColor(Colors.BG_INPUT)
                Column() {
                  Text(pet.nickname)
                    .fontSize(FontSizes.BODY)
                    .fontWeight(FontWeight.Medium)
                    .fontColor(Colors.TEXT_PRIMARY)
                  Text(`${pet.breedName || "未填品种"} · ${pet.gender === 1 ? "公" : pet.gender === 2 ? "母" : "未知"}`)
                    .fontSize(FontSizes.CAPTION)
                    .fontColor(Colors.TEXT_TERTIARY)
                    .margin({ top: 4 })
                }
                .layoutWeight(1)
                .margin({ left: Spacing.M })
                .alignItems(HorizontalAlign.Start)

                Image($r("app.media.icon_chevron_right"))
                  .width(16)
                  .height(16)
                  .fillColor("#CCCCCC")
              }
              .width("100%")
              .padding({ left: Spacing.L, right: Spacing.L, top: Spacing.M, bottom: Spacing.M })
              .backgroundColor(Colors.BG_CARD)
              .onClick(() => router.pushUrl({ url: "pages/tools/PetEdit", params: { id: pet.id } }))
            }
            .padding({ left: Spacing.L, right: Spacing.L, top: 6 })
          })
        }
        .width("100%")
        .layoutWeight(1)
        .backgroundColor(Colors.BG_PAGE)
      }

      Button("+ 添加新毛孩子")
        .width("calc(100% - 32vp)")
        .height(48)
        .fontSize(FontSizes.BODY)
        .fontColor(Colors.TEXT_WHITE)
        .backgroundColor(Colors.PRIMARY)
        .borderRadius(Radius.MD)
        .margin({ left: Spacing.L, right: Spacing.L, bottom: Spacing.L })
        .onClick(() => router.pushUrl({ url: "pages/tools/PetEdit" }))
    }
    .width("100%")
    .height("100%")
    .backgroundColor(Colors.BG_PAGE)
  }
}

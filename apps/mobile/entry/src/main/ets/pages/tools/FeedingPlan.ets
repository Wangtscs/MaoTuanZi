/**
 * 喂养方案
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing, Radius } from "../../common/Constants";
import { WarningIcon, BellIcon } from "../../components/icons/Icons";
import { TopBar, MedicalDisclaimer, PrimaryButton, EmptyState } from "../../components/CommonComponents";
import { ApiService } from "../../service/ApiService";

@Entry
@Component
struct FeedingPlanPage {
  @State pets: any[] = []
  @State selectedPetId: number = 0
  @State plan: any = null
  @State loading: boolean = false
  @State generating: boolean = false

  aboutToAppear() {
    this.loadPets()
  }

  async loadPets() {
    try {
      const data: any = await ApiService.listPets()
      this.pets = data || []
      if (this.pets.length > 0) {
        this.selectedPetId = this.pets[0].id
        this.loadPlan()
      }
    } catch (e: any) {
      promptAction.showToast({ message: "加载宠物列表失败", duration: 1500 })
    }
  }

  async loadPlan() {
    if (this.selectedPetId <= 0) return
    this.loading = true
    try {
      const data: any = await ApiService.getFeedingPlan(this.selectedPetId)
      this.plan = data && data.exists ? data : null
    } catch (e: any) {
      this.plan = null
      promptAction.showToast({ message: "加载方案失败，请重试", duration: 1500 })
    } finally {
      this.loading = false
    }
  }

  build() {
    Column() {
      TopBar({ title: "喂养方案", showBack: true, onBack: () => router.back() })

      if (this.pets.length === 0) {
        EmptyState({
          iconName: "paw",
          title: "请先创建宠物档案",
          desc: "喂养方案需要基于毛孩子的档案生成",
          actionText: "去创建"
        }).onAction(() => router.pushUrl({ url: "pages/tools/PetEdit" }))
      } else {
        Scroll() {
          Column() {
            // 宠物选择
            Scroll() {
              Row() {
                ForEach(this.pets, (pet: any) => {
                  Row() {
                    Text(pet.nickname)
                      .fontSize(FontSizes.BODY_S)
                      .fontColor(this.selectedPetId === pet.id ? Colors.TEXT_WHITE : Colors.TEXT_PRIMARY)
                      .padding({ left: Spacing.M, right: Spacing.M, top: 6, bottom: 6 })
                      .backgroundColor(this.selectedPetId === pet.id ? Colors.PRIMARY : Colors.BG_INPUT)
                      .borderRadius(20)
                      .margin({ right: 8 })
                  }
                  .onClick(() => {
                    this.selectedPetId = pet.id
                    this.loadPlan()
                  })
                })
              }
            }
            .scrollBar(BarState.Off)
            .width("100%")
            .padding({ left: Spacing.L, right: Spacing.L })

            if (this.plan) {
              this.renderPlan()
            } else if (!this.loading) {
              Column() {
                Image($r("app.media.icon_chart"))
                  .width(64)
                  .height(64)
                  .fillColor(Colors.PRIMARY)
                Text("还没有专属喂养方案")
                  .fontSize(FontSizes.BODY)
                  .fontColor(Colors.TEXT_PRIMARY)
                  .margin({ top: Spacing.M })
                Text("AI 将基于宠物档案为您生成")
                  .fontSize(FontSizes.BODY_S)
                  .fontColor(Colors.TEXT_SECONDARY)
                  .margin({ top: 4 })
                PrimaryButton({
                  text: this.generating ? "生成中..." : "立即生成方案",
                  block: true,
                  loading: this.generating
                })
                  .width("calc(100% - 64vp)")
                  .margin({ top: Spacing.L })
                  .onClick(() => this.generatePlan())
              }
              .padding(Spacing.XL)
              .backgroundColor(Colors.BG_CARD)
              .borderRadius(Radius.MD)
              .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.M })
              .alignItems(HorizontalAlign.Center)
            }

            MedicalDisclaimer() {}
          }
          .width("100%")
        }
        .layoutWeight(1)
        .scrollBar(BarState.Off)
      }
    }
    .width("100%")
    .height("100%")
    .backgroundColor(Colors.BG_PAGE)
  }

  @Builder renderPlan() {
    Column() {
      Text(this.plan.summary)
        .fontSize(FontSizes.H2)
        .fontWeight(FontWeight.Medium)
        .fontColor(Colors.TEXT_PRIMARY)
        .width("100%")

      // 每日喂食
      Column() {
        Text("每日喂食")
          .fontSize(FontSizes.BODY)
          .fontWeight(FontWeight.Medium)
          .fontColor(Colors.TEXT_PRIMARY)
          .width("100%")
          .margin({ bottom: Spacing.S })

        Row() {
          Column() {
            Text(`${this.plan.daily_food?.amount_g || 0}g`)
              .fontSize(28)
              .fontWeight(FontWeight.Bold)
              .fontColor(Colors.PRIMARY)
            Text("每日总量")
              .fontSize(FontSizes.CAPTION)
              .fontColor(Colors.TEXT_SECONDARY)
          }
          .layoutWeight(1)
          .alignItems(HorizontalAlign.Start)

          Column() {
            Text(`${this.plan.daily_food?.times_per_day || 0} 次`)
              .fontSize(28)
              .fontWeight(FontWeight.Bold)
              .fontColor(Colors.SECONDARY)
            Text("每日次数")
              .fontSize(FontSizes.CAPTION)
              .fontColor(Colors.TEXT_SECONDARY)
          }
          .layoutWeight(1)
          .alignItems(HorizontalAlign.Start)
        }
        .width("100%")
      }
      .padding(Spacing.M)
      .backgroundColor(Colors.BG_PAGE)
      .borderRadius(Radius.SM)
      .margin({ top: Spacing.M })

      // 时间表
      if (this.plan.schedule) {
        Text("喂食时间表")
          .fontSize(FontSizes.BODY)
          .fontWeight(FontWeight.Medium)
          .fontColor(Colors.TEXT_PRIMARY)
          .width("100%")
          .margin({ top: Spacing.M, bottom: Spacing.S })

        ForEach(this.plan.schedule, (item: any) => {
          Row() {
            Text(item.time)
              .fontSize(FontSizes.BODY_S)
              .fontWeight(FontWeight.Medium)
              .fontColor(Colors.PRIMARY)
              .width(60)
            Text(item.action)
              .fontSize(FontSizes.BODY_S)
              .fontColor(Colors.TEXT_PRIMARY)
            Blank()
            BellIcon({ size: FontSizes.BODY, color: Colors.PRIMARY })
              .fontSize(FontSizes.BODY)
          }
          .width("100%")
          .padding({ top: 6, bottom: 6 })
        })
      }

      // 警告
      if (this.plan.warnings) {
        Row() { WarningIcon({ size: FontSizes.BODY, color: "#FF4D4F" }).margin({ right: 4 }) Text("注意事项").fontSize(FontSizes.BODY).fontWeight(FontWeight.Medium).fontColor("#FF4D4F") }
          .fontSize(FontSizes.BODY)
          .fontWeight(FontWeight.Medium)
          .fontColor("#FF4D4F")
          .width("100%")
          .margin({ top: Spacing.M, bottom: Spacing.S })
        ForEach(this.plan.warnings, (w: string) => {
          Row() {
            Text("•")
              .fontColor("#FF4D4F")
              .margin({ right: 8 })
            Text(w)
              .fontSize(FontSizes.BODY_S)
              .fontColor("#874D00")
          }
          .width("100%")
          .padding({ top: 4, bottom: 4 })
        })
      }
    }
    .padding(Spacing.L)
    .backgroundColor(Colors.BG_CARD)
    .borderRadius(Radius.MD)
    .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.M })
  }

  async generatePlan() {
    this.generating = true
    try {
      const data: any = await ApiService.generateFeedingPlan(this.selectedPetId)
      this.plan = data
      promptAction.showToast({ message: "方案已生成", duration: 1200 })
    } catch (e: any) {
      promptAction.showToast({ message: e.message || "生成失败", duration: 1500 })
    } finally {
      this.generating = false
    }
  }
}

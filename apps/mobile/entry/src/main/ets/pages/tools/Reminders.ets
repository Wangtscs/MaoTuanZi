/**
 * 健康提醒
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing, Radius } from "../../common/Constants";
import { TopBar, EmptyState, MedicalDisclaimer } from "../../components/CommonComponents";
import { ApiService } from "../../service/ApiService";

@Entry
@Component
struct RemindersPage {
  @State pets: any[] = []
  @State reminders: any[] = []
  @State petsMap: Map<number, string> = new Map()

  aboutToAppear() {
    this.loadAll()
  }

  async loadAll() {
    try {
      const petsData: any = await ApiService.listPets()
      this.pets = petsData || []
      this.petsMap.clear()
      this.pets.forEach((p: any) => this.petsMap.set(p.id, p.nickname))

      const remData: any = await ApiService.listReminders()
      this.reminders = remData || []
    } catch (e: any) {
      promptAction.showToast({ message: "加载失败，请重试", duration: 1500 })
    }
  }

  build() {
    Column() {
      TopBar({ title: "健康提醒", showBack: true, onBack: () => router.back() })

      Column() {
          Button("+ 新建提醒")
            .width("calc(100% - 32vp)")
            .height(40)
            .fontSize(FontSizes.BODY_S)
            .fontColor(Colors.PRIMARY)
            .backgroundColor("rgba(255,182,193,0.1)")
            .borderRadius(Radius.MD)
            .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.M, bottom: Spacing.M })
            .onClick(() => this.onCreate())
        }

      if (this.reminders.length === 0) {
        EmptyState({
          iconName: "bell",
          title: "暂无提醒",
          desc: "为毛孩子设置驱虫、疫苗、体检等提醒",
          actionText: "新建提醒"
        }).onAction(() => this.onCreate())
      } else {
        List() {
          ForEach(this.reminders, (r: any) => {
            ListItem() {
              Row() {
                Image($r("app.media.icon_bell"))
                  .width(20)
                  .height(20)
                  .fillColor(Colors.PRIMARY)
                  .margin({ right: Spacing.M })
                Column() {
                  Text(r.title)
                    .fontSize(FontSizes.BODY)
                    .fontColor(Colors.TEXT_PRIMARY)
                  Text(`${this.petsMap.get(r.petId) || ""} · 下次：${this.formatDate(r.nextRunAt)}`)
                    .fontSize(FontSizes.CAPTION)
                    .fontColor(Colors.TEXT_TERTIARY)
                    .margin({ top: 2 })
                  if (r.cycleDays) {
                    Text(`每 ${r.cycleDays} 天循环`)
                      .fontSize(FontSizes.TINY)
                      .fontColor(Colors.WARNING)
                      .margin({ top: 2 })
                  }
                }
                .layoutWeight(1)
                .alignItems(HorizontalAlign.Start)

                Image($r("app.media.icon_delete"))
                  .width(20)
                  .height(20)
                  .fillColor("#FF4D4F")
                  .onClick(() => this.onDelete(r))
              }
              .width("100%")
              .padding(Spacing.M)
              .backgroundColor(Colors.BG_CARD)
              .borderRadius(Radius.MD)
              .margin({ bottom: Spacing.S })
            }
            .padding({ left: Spacing.L, right: Spacing.L })
          })
        }
        .layoutWeight(1)
        .backgroundColor(Colors.BG_PAGE)
      }

      MedicalDisclaimer() {}
    }
    .width("100%")
    .height("100%")
    .backgroundColor(Colors.BG_PAGE)
  }

  formatDate(s: string): string {
    return s ? new Date(s).toLocaleDateString("zh-CN") : "-"
  }

  async onCreate() {
    if (this.pets.length === 0) {
      promptAction.showToast({ message: "请先创建宠物档案", duration: 1500 })
      return
    }
    promptAction.showDialog({
      title: "新建提醒",
      message: "请选择类型",
      buttons: [
        { text: "疫苗", color: Colors.PRIMARY },
        { text: "驱虫", color: Colors.PRIMARY },
        { text: "体检", color: Colors.PRIMARY }
      ]
    }).then(async (idx) => {
      const types = ["疫苗", "驱虫", "体检"]
      const titles = ["疫苗接种", "体外驱虫", "健康体检"]
      const type = idx.index + 1
      try {
        await ApiService.createReminder({
          petId: this.pets[0].id,
          type: type,
          title: titles[idx.index],
          cycleDays: 30,
          nextRunAt: new Date(Date.now() + 86400000 * 7).toISOString()
        })
        promptAction.showToast({ message: "已创建", duration: 1200 })
        this.loadAll()
      } catch (e: any) {
        promptAction.showToast({ message: e.message, duration: 1500 })
      }
    })
  }

  async onDelete(r: any) {
    try {
      await ApiService.deleteReminder(r.id)
      promptAction.showToast({ message: "已删除", duration: 1200 })
      this.loadAll()
    } catch (e: any) {
      promptAction.showToast({ message: e.message, duration: 1500 })
    }
  }
}

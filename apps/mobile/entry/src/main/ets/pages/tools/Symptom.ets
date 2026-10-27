/**
 * 症状查询
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing, Radius } from "../../common/Constants";
import { TopBar, MedicalDisclaimer } from "../../components/CommonComponents";
import { ApiService } from "../../service/ApiService";

// 初始推荐分类
const QUICK_CATEGORIES = [
  { label: "消化问题", hint: "软便、呕吐、腹泻" },
  { label: "皮肤问题", hint: "瘙痒、掉毛、皮屑" },
  { label: "呼吸道", hint: "咳嗽、打喷嚏、流涕" },
  { label: "行为异常", hint: "精神差、厌食、异常叫声" },
  { label: "外伤", hint: "跛行、抓伤、割伤" },
  { label: "眼科", hint: "眼屎、眯眼、流泪" }
]

@Entry
@Component
struct SymptomPage {
  @State keyword: string = ""
  @State result: any = null
  @State loading: boolean = false
  @State showInitial: boolean = true  // 新增：显示初始引导态

  build() {
    Column() {
      TopBar({ title: "症状查询", showBack: true, onBack: () => router.back() })

      Scroll() {
        Column() {
          // 搜索
          Row() {
            TextInput({ placeholder: "例：软便、呕吐、咳嗽...", text: this.keyword })
              .height(44)
              .layoutWeight(1)
              .borderRadius(Radius.MD)
              .backgroundColor(Colors.BG_CARD)
              .padding({ left: Spacing.M, right: Spacing.M })
              .onChange((v: string) => { this.keyword = v })

            Button("查询")
              .height(44)
              .fontSize(FontSizes.BODY_S)
              .fontColor(Colors.TEXT_WHITE)
              .backgroundColor(Colors.PRIMARY)
              .borderRadius(Radius.MD)
              .margin({ left: Spacing.S })
              .padding({ left: Spacing.L, right: Spacing.L })
              .onClick(() => this.onSearch())
          }
          .width("100%")
          .padding({ left: Spacing.L, right: Spacing.L, top: Spacing.M })

          // 初始引导态：分类入口
          if (this.showInitial && !this.result && !this.loading) {
            Column() {
              Text("猜你想查")
                .fontSize(FontSizes.BODY_S)
                .fontColor(Colors.TEXT_SECONDARY)
                .width("100%")
                .margin({ bottom: Spacing.S, top: Spacing.M, left: Spacing.L })

              Flex({ wrap: FlexWrap.Wrap }) {
                ForEach(QUICK_CATEGORIES, (cat: any) => {
                  Column() {
                    Text(cat.label)
                      .fontSize(FontSizes.BODY_S)
                      .fontColor(Colors.PRIMARY)
                      .fontWeight(FontWeight.Medium)
                    Text(cat.hint)
                      .fontSize(FontSizes.TINY)
                      .fontColor(Colors.TEXT_TERTIARY)
                      .margin({ top: 2 })
                  }
                  .width("30%")
                  .padding({ left: Spacing.S, right: Spacing.S, top: Spacing.S, bottom: Spacing.S })
                  .backgroundColor("rgba(255,182,193,0.08)")
                  .borderRadius(Radius.SM)
                  .margin({ right: Spacing.S, bottom: Spacing.S })
                  .alignItems(HorizontalAlign.Center)
                  .onClick(() => {
                    this.keyword = cat.hint.split("、")[0]
                    this.onSearch()
                  })
                })
              }
              .padding({ left: Spacing.L, right: Spacing.L })

              Text("或输入症状关键词搜索")
                .fontSize(FontSizes.CAPTION)
                .fontColor(Colors.TEXT_TERTIARY)
                .margin({ top: Spacing.M })
            }
            .width("100%")
          }

          // 常见症状快捷入口（搜索后仍保留）
          if (!this.showInitial || this.result || this.loading) {
            Column() {
              Text("常见症状")
                .fontSize(FontSizes.BODY_S)
                .fontColor(Colors.TEXT_SECONDARY)
                .width("100%")
                .margin({ bottom: Spacing.S, top: Spacing.M, left: Spacing.L })

              Flex({ wrap: FlexWrap.Wrap }) {
                this.quickItem("软便", () => { this.keyword = "软便"; this.onSearch() })
                this.quickItem("呕吐", () => { this.keyword = "呕吐"; this.onSearch() })
                this.quickItem("咳嗽", () => { this.keyword = "咳嗽"; this.onSearch() })
                this.quickItem("拒食", () => { this.keyword = "拒食"; this.onSearch() })
                this.quickItem("精神差", () => { this.keyword = "精神萎靡"; this.onSearch() })
                this.quickItem("皮肤问题", () => { this.keyword = "皮肤"; this.onSearch() })
              }
            }
            .width("100%")
            .padding({ left: Spacing.L, right: Spacing.L })
          }

          // 加载状态
          if (this.loading) {
            LoadingProgress().color(Colors.PRIMARY).margin({ top: 40 })
          }

          // 结果
          if (this.result) {
            Column() {
              Text(this.result.summary)
                .fontSize(FontSizes.BODY)
                .fontColor(Colors.TEXT_PRIMARY)
                .fontWeight(FontWeight.Medium)
                .lineHeight(22)
                .width("100%")
                .padding(Spacing.L)

              this.sectionTitle("可能原因")
              ForEach(this.result.common_causes, (cause: string) => {
                Row() {
                  Text("•")
                    .fontSize(FontSizes.BODY)
                    .fontColor(Colors.PRIMARY)
                    .margin({ right: 8 })
                  Text(cause)
                    .fontSize(FontSizes.BODY_S)
                    .fontColor(Colors.TEXT_PRIMARY)
                }
                .width("100%")
                .padding({ left: Spacing.L, right: Spacing.L, top: 4, bottom: 4 })
              })

              this.sectionTitle("家庭护理建议")
              ForEach(this.result.home_care, (tip: string) => {
                Row() {
                  Text("•")
                    .fontSize(FontSizes.BODY)
                    .fontColor(Colors.PRIMARY)
                    .margin({ right: 8 })
                  Text(tip)
                    .fontSize(FontSizes.BODY_S)
                    .fontColor(Colors.TEXT_PRIMARY)
                }
                .width("100%")
                .padding({ left: Spacing.L, right: Spacing.L, top: 4, bottom: 4 })
              })

              this.sectionTitle("危险信号")
              ForEach(this.result.warning_signs, (sign: string) => {
                Row() {
                  WarningIcon({ size: 14, color: "#FAAD14" })
                    .fontSize(FontSizes.BODY)
                    .fontColor("#FF4D4F")
                    .margin({ right: 8 })
                  Text(sign)
                    .fontSize(FontSizes.BODY_S)
                    .fontColor("#FF4D4F")
                }
                .width("100%")
                .padding({ left: Spacing.L, right: Spacing.L, top: 4, bottom: 4 })
              })

              MedicalDisclaimer() {}
            }
            .margin({ top: Spacing.M, left: Spacing.L, right: Spacing.L })
            .padding({ bottom: Spacing.M })
            .backgroundColor(Colors.BG_CARD)
            .borderRadius(Radius.MD)
          }
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

  @Builder sectionTitle(title: string) {
    Text(title)
      .fontSize(FontSizes.BODY)
      .fontWeight(FontWeight.Medium)
      .fontColor(Colors.TEXT_PRIMARY)
      .width("100%")
      .padding({ left: Spacing.L, right: Spacing.L, top: Spacing.M, bottom: Spacing.S })
  }

  @Builder quickItem(label: string, onTap: () => void) {
    Row() {
      Text(label)
        .fontSize(FontSizes.BODY_S)
        .fontColor(Colors.PRIMARY)
        .padding({ left: Spacing.M, right: Spacing.M, top: 6, bottom: 6 })
        .backgroundColor("rgba(255,182,193,0.1)")
        .borderRadius(20)
        .margin({ right: 8, bottom: 8 })
    }
    .onClick(() => onTap())
  }

  async onSearch() {
    if (!this.keyword.trim()) {
      promptAction.showToast({ message: "请输入症状关键词", duration: 1500 })
      return
    }
    this.showInitial = false
    this.loading = true
    try {
      const data: any = await ApiService.querySymptom(this.keyword)
      this.result = data
    } catch (e: any) {
      promptAction.showToast({ message: e.message, duration: 1500 })
    } finally {
      this.loading = false
    }
  }
}

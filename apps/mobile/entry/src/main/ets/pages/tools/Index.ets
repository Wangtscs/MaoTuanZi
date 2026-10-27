/**
 * 工具 Tab
 */
import router from "@ohos.router";
import { Colors, FontSizes, Spacing, Radius, Shadow, shadowOf } from "../../common/Constants";
import { PawIcon, WarningIcon, HeartPulseIcon, ChartIcon, BellIcon, PetIcon, BrushIcon, FolderIcon } from "../../components/icons/Icons";

interface ToolCardItem {
  key: string
  name: string
  desc: string
  fromColor: string
  toColor: string
  iconColor: string  // 色块上的前景色
  icon: Resource
  onTap: () => void
}

@Component
export struct ToolsTab {
  private tools: ToolCardItem[] = [
    { key: 'pet',     name: '宠物档案', desc: '管理毛孩子信息', fromColor: '#FFE4E1', toColor: '#FFB6C1', iconColor: '#FFFFFF', icon: $r("app.media.icon_pet"),         onTap: () => router.pushUrl({ url: "pages/tools/PetList" }) },
    { key: 'symptom', name: '症状查询', desc: '不舒服先来查查', fromColor: '#FFD3B6', toColor: '#FFAA80', iconColor: '#FFFFFF', icon: $r("app.media.icon_heart_pulse"), onTap: () => router.pushUrl({ url: "pages/tools/Symptom" }) },
    { key: 'feed',    name: '喂养方案', desc: 'AI 定制专属方案', fromColor: '#C6E2FF', toColor: '#B5EAD7', iconColor: '#FFFFFF', icon: $r("app.media.icon_chart"),       onTap: () => router.pushUrl({ url: "pages/tools/FeedingPlan" }) },
    { key: 'alarm',   name: '健康提醒', desc: '驱虫疫苗不忘记', fromColor: '#E0C3FC', toColor: '#C8B6FF', iconColor: '#FFFFFF', icon: $r("app.media.icon_bell"),        onTap: () => router.pushUrl({ url: "pages/tools/Reminders" }) }
  ]

  build() {
    Scroll() {
      Column() {
        // 顶部引导
        Row() {
          Column() {
            Row() {
              PawIcon({ size: 20, color: Colors.PRIMARY })
                .margin({ right: 6 })
              Text("你的毛孩子日常工具箱")
                .fontSize(FontSizes.H2)
                .fontWeight(FontWeight.Medium)
                .fontColor(Colors.TEXT_PRIMARY)
            }
            Text("症状查询 / 健康提醒 / 喂养方案 / 宠物档案")
              .fontSize(FontSizes.BODY_S)
              .fontColor(Colors.TEXT_SECONDARY)
              .margin({ top: 4 })
          }
          .alignItems(HorizontalAlign.Start)
        }
        .width("100%")
        .padding(Spacing.L)

        // 工具宫格 - 2 列
        Grid() {
          ForEach(this.tools, (t: ToolCardItem) => {
            GridItem() {
              this.toolCard(t)
            }
          })
        }
        .columnsTemplate("1fr 1fr")
        .columnsGap(Spacing.M)
        .rowsGap(Spacing.M)
        .padding({ left: Spacing.L, right: Spacing.L })

        // 免责声明
        Column() {
          Row() {
            WarningIcon({ size: 16, color: "#FAAD14" })
              .margin({ right: 6 })
            Text("重要提示")
              .fontSize(FontSizes.BODY_S)
              .fontWeight(FontWeight.Medium)
              .fontColor("#874D00")
          }
          .width("100%")
          .margin({ bottom: 6 })
          Text("所有健康相关功能仅供参考，不构成兽医诊断。若宠物出现持续不适、急性症状或疑似中毒，请立即前往正规宠物医院就诊。")
            .fontSize(FontSizes.CAPTION)
            .fontColor("#874D00")
            .lineHeight(18)
        }
        .padding(Spacing.M)
        .backgroundColor("#FFF7E6")
        .border({ width: { left: 4 }, color: "#FAAD14" })
        .borderRadius(Radius.SM)
        .margin({ left: Spacing.L, right: Spacing.L, top: Spacing.L, bottom: Spacing.L })
      }
      .width("100%")
    }
    .scrollBar(BarState.Off)
  }

  @Builder toolCard(t: ToolCardItem) {
    Column() {
      // 56×56 渐变色块 + 白色图标
      Column() {
        Image(t.icon)
          .width(28).height(28)
          .fillColor(t.iconColor)
      }
      .width(56).height(56)
      .borderRadius(16)
      .linearGradient({
        angle: 135,
        colors: [[t.fromColor], [t.toColor]]
      })
      .justifyContent(FlexAlign.Center)
      .alignItems(HorizontalAlign.Center)
      .shadow(shadowOf(Shadow.SM))

      Text(t.name)
        .fontSize(FontSizes.BODY)
        .fontColor(Colors.TEXT_PRIMARY)
        .fontWeight(FontWeight.Medium)
        .margin({ top: Spacing.M })
      Text(t.desc)
        .fontSize(FontSizes.CAPTION)
        .fontColor(Colors.TEXT_TERTIARY)
        .margin({ top: 2 })
    }
    .width("100%")
    .padding({ top: Spacing.L, bottom: Spacing.L })
    .backgroundColor(Colors.BG_CARD)
    .borderRadius(Radius.LG)
    .justifyContent(FlexAlign.Center)
    .alignItems(HorizontalAlign.Center)
    .shadow(shadowOf(Shadow.SM))
    .onClick(() => t.onTap())
  }
}

/**
 * 我的作品
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing, Radius } from "../../common/Constants";
import { TopBar, EmptyState, Tag } from "../../components/CommonComponents";
import { ApiService } from "../../service/ApiService";

// 演示用作品数据（真实宠物图片）
const MOCK_WORKS = [
  { id: 1, styleName: "橘猫", workType: "油画风", imageUrl: "https://images.unsplash.com/photo-1514888451691-3a165f5f62e4?w=400&q=80", previewUrl: "https://images.unsplash.com/photo-1514888451691-3a165f5f62e4?w=1200&q=90" },
  { id: 2, styleName: "柴犬", workType: "迪士尼风", imageUrl: "https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=400&q=80", previewUrl: "https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=1200&q=90" },
  { id: 3, styleName: "英短", workType: "水彩风", imageUrl: "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=400&q=80", previewUrl: "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=1200&q=90" },
  { id: 4, styleName: "金毛", workType: "动漫风", imageUrl: "https://images.unsplash.com/photo-1518717758536-85ae29035b6d?w=400&q=80", previewUrl: "https://images.unsplash.com/photo-1518717758536-85ae29035b6d?w=1200&q=90" },
  { id: 5, styleName: "布偶", workType: "油画风", imageUrl: "https://images.unsplash.com/photo-1574158622682-e40e69881006?w=400&q=80", previewUrl: "https://images.unsplash.com/photo-1574158622682-e40e69881006?w=1200&q=90" },
  { id: 6, styleName: "哈士奇", workType: "赛博风", imageUrl: "https://images.unsplash.com/photo-1605568427561-40dd23c2acea?w=400&q=80", previewUrl: "https://images.unsplash.com/photo-1605568427561-40dd23c2acea?w=1200&q=90" }
]

@Entry
@Component
struct WorksPage {
  @State works: any[] = []
  @State loading: boolean = false

  aboutToAppear() {
    this.load()
  }

  async load() {
    this.loading = true
    try {
      const data: any = await ApiService.listWorks(undefined, 1, 50)
      const list = data?.list || []
      // API无数据时降级展示示例作品
      this.works = list.length > 0 ? list : MOCK_WORKS
    } catch (e: any) {
      // 失败时也展示示例，确保页面有内容
      this.works = MOCK_WORKS
      promptAction.showToast({ message: "加载作品失败，展示示例", duration: 1500 })
    } finally {
      this.loading = false
    }
  }

  async onDelete(work: any) {
    promptAction.showDialog({
      title: "删除作品",
      message: "确定要删除吗？",
      buttons: [
        { text: "取消", color: "#999999" },
        { text: "删除", color: "#FF4D4F" }
      ]
    }).then(async (idx) => {
      if (idx.index === 1) {
        try {
          await ApiService.deleteWork(work.id)
          promptAction.showToast({ message: "已删除", duration: 1200 })
          this.load()
        } catch (e: any) {
          promptAction.showToast({ message: e.message, duration: 1500 })
        }
      }
    })
  }

  build() {
    Column() {
      TopBar({ title: "我的作品", showBack: true, onBack: () => router.back() })

      if (this.works.length === 0 && !this.loading) {
        EmptyState({
          iconName: "paint",
          title: "还没有作品",
          desc: "去创作你的第一张萌宠画像吧"
        })
      } else if (this.loading) {
        // 加载骨架
        Grid() {
          ForEach([0, 1, 2, 3, 4, 5], (i: number) => {
            GridItem() {
              Column()
                .width("100%")
                .aspectRatio(1)
                .backgroundColor(Colors.BG_INPUT)
                .borderRadius(Radius.SM)
            }
          })
        }
        .columnsTemplate("1fr 1fr 1fr")
        .columnsGap(Spacing.S)
        .rowsGap(Spacing.S)
        .padding(Spacing.S)
        .layoutWeight(1)
      } else {
        Grid() {
          ForEach(this.works, (w: any) => {
            GridItem() {
              Stack() {
                // 真实图片
                Image(w.imageUrl)
                  .width("100%")
                  .aspectRatio(1)
                  .objectFit(ImageFit.Cover)
                  .borderRadius(Radius.SM)
                  .onClick(() => {
                    if (w.previewUrl) {
                      router.pushUrl({ url: "pages/common/ImagePreview", params: { url: w.previewUrl } })
                    }
                  })

                // 类型标签 + 删除按钮
                Row() {
                  Text(w.workType)
                    .fontSize(FontSizes.TINY)
                    .fontColor("#FFFFFF")
                    .backgroundColor("rgba(0,0,0,0.45)")
                    .borderRadius(4)
                    .padding({ left: 4, right: 4, top: 2, bottom: 2 })
                  Blank()
                  Image($r("app.media.icon_delete"))
                    .width(18)
                    .height(18)
                    .fillColor("#FFFFFF")
                    .onClick(() => this.onDelete(w))
                }
                .width("100%")
                .padding(Spacing.S)
                .position({ x: 0, y: 0 })
              }
              .width("100%")
              .onClick(() => router.pushUrl({ url: "pages/ai/Preview", params: { workId: w.id } }))
            }
          })
        }
        .columnsTemplate("1fr 1fr 1fr")
        .columnsGap(Spacing.S)
        .rowsGap(Spacing.S)
        .padding(Spacing.S)
        .layoutWeight(1)
      }
    }
    .width("100%")
    .height("100%")
    .backgroundColor(Colors.BG_PAGE)
  }
}

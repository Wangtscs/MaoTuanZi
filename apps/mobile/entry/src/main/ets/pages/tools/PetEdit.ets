/**
 * 宠物档案编辑/创建
 */
import router from "@ohos.router";
import promptAction from "@ohos.promptAction";
import { Colors, FontSizes, Spacing, Radius } from "../../common/Constants";
import { TopBar, PrimaryButton } from "../../components/CommonComponents";
import { ApiService } from "../../service/ApiService";

@Entry
@Component
struct PetEditPage {
  @State form: any = {
    nickname: "",
    species: 1,
    breedId: null,
    breedName: "",
    gender: 0,
    birthday: "",
    weightKg: "",
    isNeutered: 0,
    stapleFood: "",
    remark: ""
  }
  @State breeds: any[] = []
  private id: number = 0

  aboutToAppear() {
    const params = router.getParams() as Record<string, Object>
    this.id = (params?.id as number) || 0
    this.loadBreeds()
    if (this.id > 0) this.loadPet()
  }

  async loadBreeds() {
    try {
      const data: any = await ApiService.listBreeds(this.form.species)
      this.breeds = data || []
    } catch (e: any) {
      promptAction.showToast({ message: "加载品种失败", duration: 1500 })
    }
  }

  async loadPet() {
    try {
      const data: any = await ApiService.getPet(this.id)
      Object.assign(this.form, data)
    } catch (e: any) {
      promptAction.showToast({ message: "加载宠物信息失败", duration: 1500 })
    }
  }

  build() {
    Column() {
      TopBar({
        title: this.id > 0 ? "编辑档案" : "创建档案",
        showBack: true,
        onBack: () => router.back()
      })

      Scroll() {
        Column() {
          this.section("基础信息") {
            TextInput({ placeholder: "昵称（必填）", text: this.form.nickname })
              .height(44)
              .borderRadius(Radius.SM)
              .backgroundColor(Colors.BG_INPUT)
              .padding({ left: Spacing.M, right: Spacing.M })
              .onChange((v: string) => { this.form.nickname = v })

            Row() {
              Text("品类").fontSize(FontSizes.BODY_S).fontColor(Colors.TEXT_SECONDARY).layoutWeight(1)
              Row() {
                this.choiceChip("🐱 猫", 1)
                this.choiceChip("🐶 犬", 2)
              }
            }.width("100%").margin({ top: Spacing.S })

            if (this.breeds.length > 0) {
              Scroll() {
                Row() {
                  ForEach(this.breeds, (breed: any) => {
                    Row() {
                      Text(breed.name)
                        .fontSize(FontSizes.BODY_S)
                        .fontColor(this.form.breedId === breed.id ? Colors.TEXT_WHITE : Colors.TEXT_PRIMARY)
                        .padding({ left: Spacing.M, right: Spacing.M, top: 4, bottom: 4 })
                        .backgroundColor(this.form.breedId === breed.id ? Colors.PRIMARY : Colors.BG_INPUT)
                        .borderRadius(16)
                        .margin({ right: 6 })
                    }
                    .onClick(() => { this.form.breedId = breed.id; this.form.breedName = breed.name })
                  })
                }
              }
              .scrollBar(BarState.Off)
              .width("100%")
              .margin({ top: Spacing.S })
            }

            Row() {
              Text("性别").fontSize(FontSizes.BODY_S).fontColor(Colors.TEXT_SECONDARY).layoutWeight(1)
              Row() {
                this.genderChip("公", 1)
                this.genderChip("母", 2)
                this.genderChip("未知", 0)
              }
            }.width("100%").margin({ top: Spacing.S })
          }

          this.section("健康信息") {
            Row() {
              Text("生日").fontSize(FontSizes.BODY_S).fontColor(Colors.TEXT_SECONDARY).layoutWeight(1)
              TextInput({ placeholder: "YYYY-MM-DD", text: this.form.birthday })
                .height(36)
                .width(160)
                .borderRadius(Radius.SM)
                .backgroundColor(Colors.BG_INPUT)
                .padding({ left: Spacing.M, right: Spacing.M })
                .onChange((v: string) => { this.form.birthday = v })
            }.width("100%")

            Row() {
              Text("体重 (kg)").fontSize(FontSizes.BODY_S).fontColor(Colors.TEXT_SECONDARY).layoutWeight(1)
              TextInput({ placeholder: "0.0", text: this.form.weightKg })
                .height(36)
                .width(120)
                .borderRadius(Radius.SM)
                .backgroundColor(Colors.BG_INPUT)
                .padding({ left: Spacing.M, right: Spacing.M })
                .onChange((v: string) => { this.form.weightKg = v })
            }.width("100%").margin({ top: Spacing.S })

            Row() {
              Text("是否绝育").fontSize(FontSizes.BODY_S).fontColor(Colors.TEXT_SECONDARY).layoutWeight(1)
              Row() {
                this.boolChip("是", 1)
                this.boolChip("否", 0)
              }
            }.width("100%").margin({ top: Spacing.S })

            TextInput({ placeholder: "主食品牌（可选）", text: this.form.stapleFood })
              .height(44)
              .borderRadius(Radius.SM)
              .backgroundColor(Colors.BG_INPUT)
              .padding({ left: Spacing.M, right: Spacing.M })
              .margin({ top: Spacing.S })
              .onChange((v: string) => { this.form.stapleFood = v })
          }

          this.section("备注") {
            TextInput({ placeholder: "想说点什么...", text: this.form.remark })
              .height(80)
              .borderRadius(Radius.SM)
              .backgroundColor(Colors.BG_INPUT)
              .padding({ left: Spacing.M, right: Spacing.M, top: Spacing.S, bottom: Spacing.S })
              .onChange((v: string) => { this.form.remark = v })
          }

          if (this.id > 0) {
            Button("删除档案")
              .width("calc(100% - 32vp)")
              .height(40)
              .fontSize(FontSizes.BODY_S)
              .fontColor("#FF4D4F")
              .backgroundColor("transparent")
              .margin({ top: Spacing.M })
              .onClick(() => this.onDelete())
          }

          PrimaryButton({ text: this.id > 0 ? "保存修改" : "创建档案", block: true })
            .margin({ top: Spacing.L, bottom: Spacing.XL })
            .onClick(() => this.onSubmit())
        }
        .width("100%")
        .padding({ left: Spacing.L, right: Spacing.L })
      }
      .layoutWeight(1)
      .scrollBar(BarState.Off)
    }
    .width("100%")
    .height("100%")
    .backgroundColor(Colors.BG_PAGE)
  }

  @Builder section(title: string) {
    Column() {
      Text(title)
        .fontSize(FontSizes.H2)
        .fontWeight(FontWeight.Medium)
        .fontColor(Colors.TEXT_PRIMARY)
        .width("100%")
        .margin({ top: Spacing.M, bottom: Spacing.S })
    }
  }

  @Builder choiceChip(label: string, value: number) {
    Row() {
      Text(label)
        .fontSize(FontSizes.BODY_S)
        .fontColor(this.form.species === value ? Colors.TEXT_WHITE : Colors.TEXT_PRIMARY)
        .padding({ left: Spacing.M, right: Spacing.M, top: 4, bottom: 4 })
        .backgroundColor(this.form.species === value ? Colors.PRIMARY : Colors.BG_INPUT)
        .borderRadius(16)
        .margin({ right: 6 })
    }
    .onClick(() => {
      this.form.species = value
      this.form.breedId = null
      this.form.breedName = ""
      this.loadBreeds()
    })
  }

  @Builder genderChip(label: string, value: number) {
    Row() {
      Text(label)
        .fontSize(FontSizes.BODY_S)
        .fontColor(this.form.gender === value ? Colors.TEXT_WHITE : Colors.TEXT_PRIMARY)
        .padding({ left: Spacing.M, right: Spacing.M, top: 4, bottom: 4 })
        .backgroundColor(this.form.gender === value ? Colors.PRIMARY : Colors.BG_INPUT)
        .borderRadius(16)
        .margin({ right: 6 })
    }
    .onClick(() => { this.form.gender = value })
  }

  @Builder boolChip(label: string, value: number) {
    Row() {
      Text(label)
        .fontSize(FontSizes.BODY_S)
        .fontColor(this.form.isNeutered === value ? Colors.TEXT_WHITE : Colors.TEXT_PRIMARY)
        .padding({ left: Spacing.M, right: Spacing.M, top: 4, bottom: 4 })
        .backgroundColor(this.form.isNeutered === value ? Colors.PRIMARY : Colors.BG_INPUT)
        .borderRadius(16)
        .margin({ right: 6 })
    }
    .onClick(() => { this.form.isNeutered = value })
  }

  async onSubmit() {
    if (!this.form.nickname) {
      promptAction.showToast({ message: "请填写昵称", duration: 1500 })
      return
    }
    try {
      if (this.id > 0) {
        await ApiService.updatePet(this.id, this.form)
        promptAction.showToast({ message: "已保存", duration: 1200 })
      } else {
        await ApiService.createPet(this.form)
        promptAction.showToast({ message: "创建成功～", duration: 1200 })
      }
      setTimeout(() => router.back(), 600)
    } catch (e: any) {
      promptAction.showToast({ message: e.message || "保存失败", duration: 1500 })
    }
  }

  async onDelete() {
    promptAction.showDialog({
      title: "确认删除",
      message: "删除后不可恢复，相关作品将一并删除",
      buttons: [
        { text: "取消", color: "#999999" },
        { text: "确认", color: "#FF4D4F" }
      ]
    }).then(async (idx) => {
      if (idx.index === 1) {
        try {
          await ApiService.deletePet(this.id)
          promptAction.showToast({ message: "已删除", duration: 1200 })
          router.back()
        } catch (e: any) {
          promptAction.showToast({ message: e.message, duration: 1500 })
        }
      }
    })
  }
}
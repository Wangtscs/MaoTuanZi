/**
 * SVG Icon 基础组件
 */
@Component
export struct Icon {
  @Prop name: string = "";
  @Prop size: number = 24;
  @Prop color: string = "#2C2C2C";

  build() {
    Text("").width(this.size).height(this.size)
  }
}
/**
 * 底部 Tab 栏图标
 */
import { SvgIcon } from "./IconLoader";

@Component
export struct HomeIcon {
  @Prop active: boolean = false
  @Prop size: number = 24
  build() {
    SvgIcon({ iconName: this.active ? "home-filled" : "home", size: this.size, color: this.active ? "#FFB6C1" : "#999999" })
  }
}

@Component
export struct AiIcon {
  @Prop active: boolean = false
  @Prop size: number = 24
  build() {
    SvgIcon({ iconName: this.active ? "ai-filled" : "ai", size: this.size, color: this.active ? "#FFB6C1" : "#999999" })
  }
}

@Component
export struct ToolsIcon {
  @Prop active: boolean = false
  @Prop size: number = 24
  build() {
    SvgIcon({ iconName: this.active ? "tools-filled" : "tools", size: this.size, color: this.active ? "#FFB6C1" : "#999999" })
  }
}

@Component
export struct SquareIcon {
  @Prop active: boolean = false
  @Prop size: number = 24
  build() {
    SvgIcon({ iconName: this.active ? "square-filled" : "square", size: this.size, color: this.active ? "#FFB6C1" : "#999999" })
  }
}

@Component
export struct MeIcon {
  @Prop active: boolean = false
  @Prop size: number = 24
  build() {
    SvgIcon({ iconName: this.active ? "me-filled" : "me", size: this.size, color: this.active ? "#FFB6C1" : "#999999" })
  }
}
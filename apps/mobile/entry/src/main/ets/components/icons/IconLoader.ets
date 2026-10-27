/**
 * 通用 SVG 图标组件
 * 通过资源 ID 加载本地 SVG
 */
@Component
export struct SvgIcon {
  @Prop iconName: string = ""
  @Prop size: number = 24
  @Prop color: string = ""

  aboutToAppear() {
    // ArkTS 不能直接 $r 模板字符串，需要映射
  }

  build() {
    Image(this.getResource())
      .width(this.size)
      .height(this.size)
      .fillColor(this.color)
      .interpolation(ImageInterpolation.Medium)
  }

  getResource() {
    // 使用 switch 映射 iconName 到资源
    switch (this.iconName) {
      case "home": return $r("app.media.icon_home");
      case "home-filled": return $r("app.media.icon_home_filled");
      case "ai": return $r("app.media.icon_ai");
      case "ai-filled": return $r("app.media.icon_ai_filled");
      case "tools": return $r("app.media.icon_tools");
      case "tools-filled": return $r("app.media.icon_tools_filled");
      case "square": return $r("app.media.icon_square");
      case "square-filled": return $r("app.media.icon_square_filled");
      case "me": return $r("app.media.icon_me");
      case "me-filled": return $r("app.media.icon_me_filled");
      case "camera": return $r("app.media.icon_camera");
      case "image": return $r("app.media.icon_image");
      case "upload": return $r("app.media.icon_upload");
      case "download": return $r("app.media.icon_download");
      case "share": return $r("app.media.icon_share");
      case "edit": return $r("app.media.icon_edit");
      case "delete": return $r("app.media.icon_delete");
      case "plus": return $r("app.media.icon_plus");
      case "close-x": return $r("app.media.icon_close_x");
      case "check": return $r("app.media.icon_check");
      case "arrow-right": return $r("app.media.icon_arrow_right");
      case "arrow-left": return $r("app.media.icon_arrow_left");
      case "arrow-up": return $r("app.media.icon_arrow_up");
      case "arrow-down": return $r("app.media.icon_arrow_down");
      case "search": return $r("app.media.icon_search");
      case "star": return $r("app.media.icon_star");
      case "star-filled": return $r("app.media.icon_star_filled");
      case "heart": return $r("app.media.icon_heart");
      case "heart-filled": return $r("app.media.icon_heart_filled");
      case "like": return $r("app.media.icon_like");
      case "bell": return $r("app.media.icon_bell");
      case "setting": return $r("app.media.icon_setting");
      case "user": return $r("app.media.icon_user");
      case "pet": return $r("app.media.icon_pet");
      case "calendar": return $r("app.media.icon_calendar");
      case "clock": return $r("app.media.icon_clock");
      case "money": return $r("app.media.icon_money");
      case "crown": return $r("app.media.icon_crown");
      case "gift": return $r("app.media.icon_gift");
      case "coupon": return $r("app.media.icon_coupon");
      case "refresh": return $r("app.media.icon_refresh");
      case "eye": return $r("app.media.icon_eye");
      case "lock": return $r("app.media.icon_lock");
      case "phone": return $r("app.media.icon_phone");
      case "wechat": return $r("app.media.icon_wechat");
      case "info": return $r("app.media.icon_info");
      case "warning": return $r("app.media.icon_warning");
      case "success": return $r("app.media.icon_success");
      case "magic": return $r("app.media.icon_magic");
      case "paint": return $r("app.media.icon_paint");
      case "sparkle": return $r("app.media.icon_sparkle");
      case "paw": return $r("app.media.icon_paw");
      case "bone": return $r("app.media.icon_bone");
      case "heart-pulse": return $r("app.media.icon_heart_pulse");
      case "fire": return $r("app.media.icon_fire");
      case "trophy": return $r("app.media.icon_trophy");
      case "certificate": return $r("app.media.icon_certificate");
      case "brush": return $r("app.media.icon_brush");
      case "comment": return $r("app.media.icon_comment");
      case "more": return $r("app.media.icon_more");
      case "grid": return $r("app.media.icon_grid");
      case "list": return $r("app.media.icon_list");
      case "filter": return $r("app.media.icon_filter");
      case "chevron-right": return $r("app.media.icon_chevron_right");
      case "scan": return $r("app.media.icon_scan");
      case "voice": return $r("app.media.icon_voice");
      case "chart": return $r("app.media.icon_chart");
      case "folder": return $r("app.media.icon_folder");
      case "file": return $r("app.media.icon_file");
      case "image-text": return $r("app.media.icon_image_text");
      case "location": return $r("app.media.icon_location");
      case "book": return $r("app.media.icon_book");
      default: return $r("app.media.icon_home");
    }
  }
}
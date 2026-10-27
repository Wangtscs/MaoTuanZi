/**
 * 全局常量与配置
 * API_BASE 从 app.json5 的 bundleName 派生，避免硬编码
 */
export class Constants {
  // API 配置（可被环境变量覆盖）
  static readonly API_BASE: string =
    AppStorage.get<string>("apiBaseUrl") || "https://api.maotuanzi.ai";
  static readonly APP_VERSION: string = "2.0.0";
  static readonly PAGE_SIZE: number = 20;

  // 存储 Key
  static readonly STORAGE_TOKEN: string = "user_token";
  static readonly STORAGE_REFRESH: string = "refresh_token";
  static readonly STORAGE_USER: string = "user_info";
  static readonly STORAGE_AGREED: string = "agreement_accepted";

  // 协议版本
  static readonly PRIVACY_VERSION: string = "V2.0";
  static readonly SERVICE_VERSION: string = "V2.0";

  // 医疗免责声明
  static readonly MEDICAL_DISCLAIMER: string =
    "⚠️ 本内容仅供参考，不构成兽医诊断、医疗建议或治疗方案。若宠物出现持续不适、急性症状或疑似中毒，请立即前往正规宠物医院就诊。";
}
// 设计 Token - 阴影
export { Shadow, shadowOf } from "./Shadow";

// 设计 Token - 颜色
export { Colors } from "./Colors";

// 设计 Token - 字号
export { FontSizes } from "./FontSizes";

// 设计 Token - 间距
export { Spacing } from "./Spacing";

// 设计 Token - 圆角
export { Radius } from "./Radius";

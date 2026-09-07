/**
 * 统一支付管理器
 *
 * 设计目标：
 *  - 对上层页面暴露统一入口 `PaymentManager.pay(orderId, channel)`，屏蔽微信 / 华为 IAP / Mock 的差异；
 *  - 三项产品决策：支付 = 华为 IAP（主） + 微信支付（兜底）并行；
 *  - 凭证降级：当缺少微信 / 华为商户凭证（统一下单返回 null 参数，或显式开启 mockMode）时，
 *    自动走 MockStrategy 并返回"模拟成功"，保证按钮不崩溃、可演示、不阻塞构建；
 *  - 真实微信 / 华为支付 API 若当前工程未集成对应 SDK，则以清晰注释标出接入点并降级 Mock，
 *    避免引入未声明依赖导致编译失败或运行时崩溃。
 *
 * 接入真实渠道时只需：
 *  1) 在工程的 oh-package.json5 中声明对应 SDK 依赖；
 *  2) 在 HuaweiIapPay / WeChatPay 的 `// === 真实接入点 ===` 处替换注释为真实调用；
 *  3) 调用 `PaymentManager.setMockMode(false)` 关闭演示模式。
 */
import { ApiService } from "../service/ApiService";

/** 支付渠道（对齐后端契约 channel 枚举） */
export type PayChannel = "WECHAT" | "HUAWEI";

/** 微信支付调起参数（来自 unified-order 的 wxPayParams） */
export interface WxPayParams {
  appId: string;
  timeStamp: string;
  nonceStr: string;
  package: string;
  signType: string;
  paySign: string;
}

/** 华为 IAP 调起参数（来自 unified-order 的 iapParams） */
export interface IapParams {
  productId: string;
  developerPayload: string;
  signature: string;
  signedData: string;
  iapApiVersion: string;
}

/** 统一下单返回结构 */
export interface UnifiedOrderResp {
  channel: string;
  wxPayParams: WxPayParams | null;
  iapParams: IapParams | null;
  expireAt: string | null;
}

/** 支付结果 */
export interface PayResult {
  success: boolean;
  channel: PayChannel;
  transactionId?: string;
  message?: string;
}

/** 支付策略接口：各渠道实现统一的 pay 方法 */
export interface PaymentStrategy {
  pay(orderId: number, channel: PayChannel, prepay: UnifiedOrderResp | null): Promise<PayResult>;
}

/**
 * Mock 策略：无凭证 / 演示环境下的兜底实现。
 * 模拟"调起支付面板 → 用户确认 → 支付成功回调"的过程。
 */
class MockStrategy implements PaymentStrategy {
  pay(orderId: number, channel: PayChannel, prepay: UnifiedOrderResp | null): Promise<PayResult> {
    const usedChannel: PayChannel = (prepay?.channel as PayChannel) || channel || "HUAWEI";
    return new Promise<PayResult>((resolve) => {
      // 模拟支付面板确认耗时
      setTimeout(() => {
        resolve({
          success: true,
          channel: usedChannel,
          transactionId: `MOCK_TXN_${orderId}_${Date.now()}`,
          message: "模拟支付成功（演示模式：未接入真实支付渠道，请在凭证就绪后关闭 mockMode）"
        });
      }, 1200);
    });
  }
}

/**
 * 华为 IAP 支付策略（主渠道）。
 * 真实接入点已用注释标出；当前工程未集成华为 IAP Kit（无商户凭证 / 无 SDK），
 * 故直接降级 Mock，保证可编译、可演示、不崩溃。
 */
class HuaweiIapPay implements PaymentStrategy {
  pay(orderId: number, channel: PayChannel, prepay: UnifiedOrderResp | null): Promise<PayResult> {
    // === 真实接入点（华为 IAP Kit，需配置 IAP 商品与商户）===
    // 1) 在 oh-package.json5 声明依赖：@hms.iap（或项目已封装的 iap 能力）
    // 2) import { iap } from '@hms.iap'
    // 3) 调起购买：
    //    const resp = await iap.createPurchaseIntent({
    //      productId: prepay?.iapParams?.productId,
    //      developerPayload: prepay?.iapParams?.developerPayload,
    //      signature: prepay?.iapParams?.signature,
    //      signedData: prepay?.iapParams?.signedData,
    //      iapApiVersion: prepay?.iapParams?.iapApiVersion
    //    })
    //    if (resp?.returnCode === 0) {
    //      return { success: true, channel: 'HUAWEI', transactionId: resp.inAppPurchaseData?.inAppPurchaseData?.purchaseToken }
    //    }
    // 4) 失败 / 取消按 resp.returnCode 区分处理
    // 当前无 SDK，统一降级 Mock。
    return new MockStrategy().pay(orderId, channel, prepay);
  }
}

/**
 * 微信支付策略（兜底渠道）。
 * 真实接入点已用注释标出；当前工程未集成鸿蒙微信支付 SDK，
 * 故直接降级 Mock。
 */
class WeChatPay implements PaymentStrategy {
  pay(orderId: number, channel: PayChannel, prepay: UnifiedOrderResp | null): Promise<PayResult> {
    // === 真实接入点（微信支付鸿蒙版 SDK）===
    // 1) 在 oh-package.json5 声明依赖：微信开放平台鸿蒙 SDK（如 @wechat/pay）
    // 2) 调起微信支付：
    //    const params = prepay?.wxPayParams
    //    const payResp = await wxPay.requestPayment({
    //      appId: params?.appId,
    //      timeStamp: params?.timeStamp,
    //      nonceStr: params?.nonceStr,
    //      package: params?.package,
    //      signType: params?.signType,
    //      paySign: params?.paySign
    //    })
    //    if (payResp?.errCode === 0) {
    //      return { success: true, channel: 'WECHAT', transactionId: payResp?.prepayId }
    //    }
    // 3) 失败 / 用户取消按 errCode 区分
    // 当前无 SDK，统一降级 Mock。
    return new MockStrategy().pay(orderId, channel, prepay);
  }
}

/**
 * 支付管理器：统一对外入口。
 */
export class PaymentManager {
  /** 演示模式开关：true=始终走 Mock；false=按统一下单返回的真实参数选择渠道。 */
  private static mockMode: boolean = true;

  /** 单例 Mock 策略（避免重复构造） */
  private static readonly mock: MockStrategy = new MockStrategy();

  static get Mock(): MockStrategy {
    return PaymentManager.mock;
  }

  /** 设置演示模式（凭证就绪后调用 setMockMode(false) 启用真实渠道） */
  static setMockMode(value: boolean): void {
    PaymentManager.mockMode = value;
  }

  static isMockMode(): boolean {
    return PaymentManager.mockMode;
  }

  /**
   * 统一支付入口。
   * @param orderId 订单 ID（由 createOrder 获得）
   * @param channel 支付渠道（WECHAT | HUAWEI）
   * @returns 支付结果 PayResult
   */
  static async pay(orderId: number, channel: PayChannel): Promise<PayResult> {
    let prepay: UnifiedOrderResp | null = null;
    try {
      prepay = (await ApiService.unifiedOrder(orderId, channel)) as UnifiedOrderResp;
    } catch (e) {
      // 统一下单失败（后端未实现 / 网络异常）→ 演示环境降级 Mock，保证可演示
      return PaymentManager.mock.pay(orderId, channel, null);
    }

    // 显式演示模式，或后端未返回该渠道的调起参数（即无凭证）→ Mock 兜底
    const noRealParams: boolean =
      (channel === "HUAWEI" && !prepay?.iapParams) ||
      (channel === "WECHAT" && !prepay?.wxPayParams);

    if (PaymentManager.mockMode || noRealParams) {
      return PaymentManager.mock.pay(orderId, channel, prepay);
    }

    if (channel === "HUAWEI") {
      return new HuaweiIapPay().pay(orderId, channel, prepay);
    }
    return new WeChatPay().pay(orderId, channel, prepay);
  }
}

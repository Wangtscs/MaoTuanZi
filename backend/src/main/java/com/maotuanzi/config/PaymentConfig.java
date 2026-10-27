package com.maotuanzi.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 支付渠道配置。
 *
 * <p>从 application.yml / 环境变量读取微信与华为的凭证。
 * 任意必填项缺失时 {@link #hasCredentials(String)} 返回 false，调用方据此进入 mock 降级模式，
 * 从而“绝不依赖缺失的商户密钥”完成编译与运行。</p>
 */
@Component
@Getter
public class PaymentConfig {

    // ===== 微信支付 =====
    @Value("${maotuanzi.payment.wechat.mch-id:}")
    private String wechatMchId;

    @Value("${maotuanzi.payment.wechat.app-id:}")
    private String wechatAppId;

    @Value("${maotuanzi.payment.wechat.api-key:}")
    private String wechatApiKey;

    @Value("${maotuanzi.payment.wechat.notify-url:}")
    private String wechatNotifyUrl;

    @Value("${maotuanzi.payment.wechat.cert-path:}")
    private String wechatCertPath;

    /** WeChat Pay platform certificate public key (PEM, BEGIN PUBLIC KEY). */
    @Value("${maotuanzi.payment.wechat.platform-public-key-path:}")
    private String wechatPlatformPublicKeyPath;

    /** Serial number of the platform certificate matching the configured public key. */
    @Value("${maotuanzi.payment.wechat.platform-serial-no:}")
    private String wechatPlatformSerialNo;

    // ===== 华为 IAP =====
    @Value("${maotuanzi.payment.huawei.app-id:}")
    private String huaweiAppId;

    @Value("${maotuanzi.payment.huawei.merchant-id:}")
    private String huaweiMerchantId;

    @Value("${maotuanzi.payment.huawei.key:}")
    private String huaweiKey;

    /**
     * 判断某渠道是否已配置完整凭证。
     *
     * @param channel 渠道名（WECHAT / HUAWEI）
     * @return 凭证齐全返回 true；否则（含渠道名非法）返回 false，即 mock 模式
     */
    public boolean hasCredentials(String channel) {
        if (channel == null) return false;
        if ("WECHAT".equalsIgnoreCase(channel)) {
            return isNotBlank(wechatMchId) && isNotBlank(wechatAppId)
                    && isNotBlank(wechatApiKey) && isNotBlank(wechatNotifyUrl)
                    && isNotBlank(wechatCertPath);
        }
        if ("HUAWEI".equalsIgnoreCase(channel)) {
            return isNotBlank(huaweiAppId) && isNotBlank(huaweiMerchantId) && isNotBlank(huaweiKey);
        }
        return false;
    }

    /**
     * Callback verification has a stricter configuration gate than client-side payment setup.
     * A v3 callback must be authenticated with the platform public key before it can change an order.
     */
    public boolean hasWechatCallbackCredentials() {
        return isNotBlank(wechatApiKey) && isNotBlank(wechatAppId) && isNotBlank(wechatMchId)
                && isNotBlank(wechatPlatformPublicKeyPath) && isNotBlank(wechatPlatformSerialNo);
    }

    private static boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}

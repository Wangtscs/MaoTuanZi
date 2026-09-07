package com.maotuanzi.dto;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一下单结果（对齐前端契约）。
 */
@Data
public class UnifiedOrderResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 渠道名 WECHAT / HUAWEI */
    private String channel;

    /** 微信 JSAPI 支付参数 {appId,timeStamp,nonceStr,package,signType,paySign}，非微信渠道为 null */
    private JSONObject wxPayParams;

    /** 华为 IAP 拉起参数 {productId,developerPayload,signature,signedData,iapApiVersion}，非华为渠道为 null */
    private JSONObject iapParams;

    /** 订单过期时间（epoch millis） */
    private Long expireAt;
}

package com.maotuanzi.service;

import java.util.Map;

public interface WxMessageService {
    /**
     * 发送订阅消息
     * @param openId 用户openid
     * @param templateId 模板消息ID
     * @param data 消息数据（Map<keyword, value>）
     * @return 是否发送成功
     */
    boolean sendSubscribeMessage(String openId, String templateId, Map<String, String> data);
}

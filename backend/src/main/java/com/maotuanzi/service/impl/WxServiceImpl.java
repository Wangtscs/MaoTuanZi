package com.maotuanzi.service.impl;

import com.maotuanzi.service.WxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class WxServiceImpl implements WxService {

    @Value("${maotuanzi.wx.app-id:}")
    private String appId;

    @Value("${maotuanzi.wx.app-secret:}")
    private String appSecret;

    @Value("${maotuanzi.wx.enabled:false}")
    private boolean enabled;

    @Override
    public WxSession jscode2session(String code) {
        if (!enabled) {
            // 沙箱模式：mock 一个 openid
            WxSession s = new WxSession();
            s.openid = "mock_openid_" + Math.abs(code.hashCode());
            s.unionid = null;
            s.sessionKey = "mock_session_key";
            return s;
        }
        try {
            String url = String.format(
                    "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    appId, appSecret, code);
            RestTemplate rt = new RestTemplate();
            // 真实实现：解析返回 JSON
            return rt.getForObject(url, WxSession.class);
        } catch (Exception e) {
            log.error("微信 jscode2session 调用失败", e);
            return null;
        }
    }
}
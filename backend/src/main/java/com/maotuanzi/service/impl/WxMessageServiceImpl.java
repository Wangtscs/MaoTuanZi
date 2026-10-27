package com.maotuanzi.service.impl;

import com.alibaba.fastjson2.JSON;
import com.maotuanzi.service.WxMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
public class WxMessageServiceImpl implements WxMessageService {

    @Value("${maotuanzi.wx.app-id:}") private String appId;
    @Value("${maotuanzi.wx.app-secret:}") private String appSecret;

    @Override
    public boolean sendSubscribeMessage(String openId, String templateId, Map<String, String> data) {
        if (appId == null || appId.isEmpty()) {
            log.warn("微信订阅消息未配置，跳过发送");
            return false;
        }

        try {
            // 获取access_token（简化版，实际应缓存）
            String token = getAccessToken();
            if (token == null) return false;

            // 构建消息体
            TreeMap<String, Object> msg = new TreeMap<>();
            msg.put("touser", openId);
            msg.put("template_id", templateId);
            msg.put("page", "pages/index/index");

            TreeMap<String, Object> m = new TreeMap<>();
            for (Map.Entry<String, String> entry : data.entrySet()) {
                TreeMap<String, String> keyword = new TreeMap<>();
                keyword.put("value", entry.getValue());
                keyword.put("color", "#173177");
                m.put(entry.getKey(), keyword);
            }
            msg.put("data", m);

            // 调用微信订阅消息API
            String url = String.format(
                "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=%s", token);

            String resp = callWxApi(url, msg);
            log.info("订阅消息发送结果: openId={}, templateId={}, resp={}", openId, templateId, resp);
            return resp != null && resp.contains("\"errcode\":0");

        } catch (Exception e) {
            log.error("订阅消息发送失败", e);
            return false;
        }
    }

    private String getAccessToken() {
        try {
            String url = String.format(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                appId, appSecret);
            String resp = callWxApi(url, null);
            // 简化解析，实际用JSON库
            int idx = resp.indexOf("\"access_token\":\"");
            if (idx < 0) return null;
            int end = resp.indexOf("\"", idx + 16);
            return resp.substring(idx + 15, end);
        } catch (Exception e) {
            log.error("获取access_token失败", e);
            return null;
        }
    }

    private String callWxApi(String urlStr, Object body) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            if (body != null) {
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(JSON.toJSONString(body).getBytes());
                }
            }
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                return sb.toString();
            }
        } catch (Exception e) {
            log.error("调用微信API失败 url={}", urlStr, e);
            return null;
        }
    }
}

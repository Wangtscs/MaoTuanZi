package com.maotuanzi.service;

public interface WxService {

    WxSession jscode2session(String code);

    class WxSession {
        public String openid;
        public String unionid;
        public String sessionKey;
    }
}
package com.maotuanzi.service;

import com.maotuanzi.dto.LoginResponse;
import com.maotuanzi.dto.WxLoginRequest;
import com.maotuanzi.entity.User;

public interface UserService {
    LoginResponse wxLogin(WxLoginRequest request);
    LoginResponse refresh(String refreshToken);
    void logout(String token);
    User me();
    User updateMe(User patch);
    void cancel(String reason);
    boolean isVip(Long userId);
}
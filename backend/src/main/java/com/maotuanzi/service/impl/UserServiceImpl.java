package com.maotuanzi.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.common.util.IdUtil;
import com.maotuanzi.dto.LoginResponse;
import com.maotuanzi.dto.WxLoginRequest;
import com.maotuanzi.entity.User;
import com.maotuanzi.mapper.UserMapper;
import com.maotuanzi.security.jwt.JwtUtil;
import com.maotuanzi.service.UserService;
import com.maotuanzi.service.WxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redis;
    private final WxService wxService;
    private final ObjectMapper objectMapper;

    @Value("${maotuanzi.agreement.privacy-version:V2.0}")
    private String privacyVersion;

    @Override
    @Transactional
    public LoginResponse wxLogin(WxLoginRequest request) {
        WxService.WxSession session = wxService.jscode2session(request.getCode());
        if (session == null || session.openid == null) {
            throw BizException.of(ErrorCode.WX_LOGIN_ERROR);
        }

        User user = userMapper.selectByOpenid(session.openid);
        boolean isNew = false;
        if (user == null) {
            user = new User();
            user.setOpenid(session.openid);
            user.setUnionid(session.unionid);
            user.setNickname(request.getNickname() != null ? request.getNickname() : "毛团子用户");
            user.setAvatarUrl(request.getAvatarUrl());
            user.setStatus(0);
            user.setVipLevel(0);
            user.setRegisterSource("wx");
            user.setAgreementVersion(request.getAgreementVersion());
            userMapper.insert(user);
            // 生成唯一邀请码（若已有则保留，新注册用户必然为空）
            user.setInviteCode(generateInviteCode());
            userMapper.updateInviteCode(user.getId(), user.getInviteCode());
            isNew = true;
        } else if (user.getStatus() != null && user.getStatus() == 2) {
            throw BizException.of(ErrorCode.ACCOUNT_CANCELLED);
        } else if (user.getStatus() != null && user.getStatus() == 1) {
            throw BizException.of(ErrorCode.ACCOUNT_BANNED);
        }

        userMapper.updateLastLogin(user.getId(), LocalDateTime.now());

        String token = jwtUtil.generateAccess(user.getId(), user.getOpenid(), user.getVipLevel());
        String refresh = jwtUtil.generateRefresh(user.getId());

        return LoginResponse.of(token, refresh, jwtUtil.getAccessExpire(), user, isNew);
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        try {
            var claims = jwtUtil.parse(refreshToken);
            if (!"refresh".equals(claims.get("type", String.class))) {
                throw BizException.of(ErrorCode.TOKEN_INVALID);
            }
            Long uid = claims.get("uid", Number.class).longValue();
            User u = userMapper.selectById(uid);
            if (u == null) throw BizException.of(ErrorCode.NOT_FOUND);
            String token = jwtUtil.generateAccess(u.getId(), u.getOpenid(), u.getVipLevel());
            return LoginResponse.of(token, refreshToken, jwtUtil.getAccessExpire(), u, false);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw BizException.of(ErrorCode.REFRESH_TOKEN_INVALID);
        }
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isEmpty()) return;
        try {
            var claims = jwtUtil.parse(token);
            long remainingMs = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (remainingMs > 0) {
                redis.opsForValue().set("token:invalid:" + token, "1", Duration.ofMillis(remainingMs));
            }
        } catch (Exception ignore) {}
    }

    @Override
    public User me() {
        Long uid = com.maotuanzi.security.UserContext.getUserId();
        if (uid == null) throw BizException.of(ErrorCode.UNAUTHORIZED);
        User u = userMapper.selectById(uid);
        if (u == null) throw BizException.of(ErrorCode.NOT_FOUND);
        return u;
    }

    @Override
    @Transactional
    public User updateMe(User patch) {
        Long uid = com.maotuanzi.security.UserContext.getUserId();
        if (uid == null) throw BizException.of(ErrorCode.UNAUTHORIZED);
        patch.setId(uid);
        userMapper.updateById(patch);
        return userMapper.selectById(uid);
    }

    @Override
    @Transactional
    public void cancel(String reason) {
        Long uid = com.maotuanzi.security.UserContext.getUserId();
        if (uid == null) throw BizException.of(ErrorCode.UNAUTHORIZED);
        User u = userMapper.selectById(uid);
        if (u == null) throw BizException.of(ErrorCode.NOT_FOUND);
        if (u.getVipExpireAt() != null && u.getVipExpireAt().isAfter(LocalDateTime.now())) {
            throw BizException.of(ErrorCode.STATE_CONFLICT, "会员生效中，请先关闭会员");
        }
        userMapper.updateStatus(uid, 2);
    }

    @Override
    public boolean isVip(Long userId) {
        if (userId == null) return false;
        User u = userMapper.selectById(userId);
        if (u == null) return false;
        if (u.getVipLevel() == null || u.getVipLevel() == 0) return false;
        return u.getVipExpireAt() != null && u.getVipExpireAt().isAfter(LocalDateTime.now());
    }

    /**
     * 生成全局唯一的用户邀请码（MT + 8 位大写字母，碰撞则重试，极端情况追加时间戳）。
     */
    private String generateInviteCode() {
        for (int i = 0; i < 10; i++) {
            String code = "MT" + RandomUtil.randomStringUpper(8);
            if (userMapper.selectByInviteCode(code) == null) {
                return code;
            }
        }
        return "MT" + RandomUtil.randomStringUpper(8) + System.currentTimeMillis() % 1000;
    }
}
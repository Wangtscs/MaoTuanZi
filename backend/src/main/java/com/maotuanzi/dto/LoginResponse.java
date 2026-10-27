package com.maotuanzi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maotuanzi.entity.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("登录响应")
public class LoginResponse {

    @ApiModelProperty("访问 Token")
    private String token;

    @ApiModelProperty("刷新 Token")
    private String refreshToken;

    @ApiModelProperty("Token 有效期（秒）")
    private Long expiresIn;

    @ApiModelProperty("用户信息")
    private UserInfo user;

    @Data
    public static class UserInfo {
        private Long id;
        private String openid;
        private String nickname;
        private String avatarUrl;
        private Integer status;
        private Integer vipLevel;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        private LocalDateTime vipExpireAt;
        private Boolean isNewUser;
    }

    public static LoginResponse of(String token, String refresh, long expiresIn, User u, boolean isNew) {
        LoginResponse r = new LoginResponse();
        r.setToken(token);
        r.setRefreshToken(refresh);
        r.setExpiresIn(expiresIn);
        UserInfo ui = new UserInfo();
        ui.setId(u.getId());
        ui.setOpenid(u.getOpenid());
        ui.setNickname(u.getNickname());
        ui.setAvatarUrl(u.getAvatarUrl());
        ui.setStatus(u.getStatus());
        ui.setVipLevel(u.getVipLevel());
        ui.setVipExpireAt(u.getVipExpireAt());
        ui.setIsNewUser(isNew);
        r.setUser(ui);
        return r;
    }
}
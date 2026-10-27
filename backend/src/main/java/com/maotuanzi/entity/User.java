package com.maotuanzi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户
 */
@Data
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String openid;
    private String unionid;
    private String nickname;
    private String avatarUrl;
    private String phone;
    private Integer gender;
    private Integer status;
    private Integer vipLevel;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime vipExpireAt;
    /** 邀请码（唯一），注册时生成 */
    private String inviteCode;
    /** 邀请人用户 ID */
    private Long inviterId;
    private String registerSource;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime lastLoginAt;
    private String agreementVersion;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime agreementAcceptedAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime updatedAt;

    @JsonIgnore
    private LocalDateTime deletedAt;
}
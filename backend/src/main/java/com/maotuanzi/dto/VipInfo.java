package com.maotuanzi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 当前用户 VIP 信息（对齐前端契约：vipLevel / vipExpireAt / benefits）。
 */
@Data
public class VipInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** VIP 等级：0=无 1=月 2=季 3=年 */
    private Integer vipLevel;

    /** 会员到期时间（ISO8601，未开通为 null） */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime vipExpireAt;

    /** 会员权益点描述 */
    private List<String> benefits;
}

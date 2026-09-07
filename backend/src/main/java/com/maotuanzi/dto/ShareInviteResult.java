package com.maotuanzi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 绑定邀请码结果（对齐前端契约：rewarded）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareInviteResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 是否已发奖 */
    private boolean rewarded;
}

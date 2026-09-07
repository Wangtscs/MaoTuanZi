package com.maotuanzi.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 分享记录返回（对齐前端契约：reward {type,amount} / shareCount）。
 */
@Data
public class ShareRecordResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 本次分享获得的奖励（每日首单分享奖励，其余为 null） */
    private RewardBrief reward;

    /** 帖子最新分享数（无 postId 时为 0） */
    private int shareCount;
}

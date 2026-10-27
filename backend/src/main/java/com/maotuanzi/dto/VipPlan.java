package com.maotuanzi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * VIP 套餐视图对象（对齐前端契约：id / productType / name / priceCents / durationDays / tag）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VipPlan implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 套餐标识，如 vip_month / vip_quarter / vip_year */
    private String id;

    /** 商品类型（2=订阅，与 ProductTypeEnum.SUBSCRIPTION 对齐） */
    private int productType;

    /** 套餐名称 */
    private String name;

    /** 价格（分） */
    private int priceCents;

    /** 有效期（天） */
    private int durationDays;

    /** 营销标签，如 热门 / 超值 / 最划算 */
    private String tag;
}

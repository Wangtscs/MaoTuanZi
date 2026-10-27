package com.maotuanzi.mapper;

import com.maotuanzi.entity.CouponClaim;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CouponClaimMapper {
    int insert(CouponClaim claim);
    int updateUsed(@Param("id") Long id, @Param("orderId") Long orderId);
    CouponClaim selectById(@Param("id") Long id);
    List<CouponClaim> selectByUserId(@Param("userId") Long userId, @Param("status") Integer status);
    Long countByUserAndCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId);
}
package com.maotuanzi.mapper;

import com.maotuanzi.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CouponMapper {
    Coupon selectById(@Param("id") Long id);
    Coupon selectByCode(@Param("code") String code);
    int insert(Coupon coupon);
    int updateById(Coupon coupon);
    int decrRemain(@Param("id") Long id);
    List<Coupon> selectAll(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);
    Long countAll(@Param("keyword") String keyword);
}
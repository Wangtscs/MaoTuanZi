package com.maotuanzi.mapper;

import com.maotuanzi.entity.Refund;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RefundMapper {
    int insert(Refund refund);
    int updateById(Refund refund);
    Refund selectById(@Param("id") Long id);
    Refund selectByRefundNo(@Param("refundNo") String refundNo);
    List<Refund> selectByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    Long countByUserId(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    Long countByOrder(@Param("orderId") Long orderId);
    List<Refund> selectAll(@Param("status") Integer status, @Param("offset") int offset, @Param("limit") int limit);
    Long countAll(@Param("status") Integer status);
}
package com.maotuanzi.mapper;

import com.maotuanzi.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    int insert(Order order);

    int updateById(Order order);

    int updatePaid(@Param("id") Long id, @Param("wxTransactionId") String wxTransactionId,
                   @Param("paidAt") LocalDateTime paidAt, @Param("status") Integer status);

    int updateClosed(@Param("id") Long id, @Param("status") Integer status,
                     @Param("closedAt") LocalDateTime closedAt);

    int updateRefunded(@Param("id") Long id, @Param("refundedAt") LocalDateTime refundedAt,
                       @Param("amount") Integer amount, @Param("status") Integer status);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updateChannelPrepay(@Param("id") Long id, @Param("channel") String channel,
                            @Param("wxPrepayId") String wxPrepayId);

    Order selectById(@Param("id") Long id);

    Order selectByOrderNo(@Param("orderNo") String orderNo);

    Order selectByWxTxn(@Param("wxTransactionId") String wxTransactionId);

    List<Order> selectByUserId(@Param("userId") Long userId, @Param("status") Integer status,
                               @Param("offset") int offset, @Param("limit") int limit);

    Long countByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    List<Order> selectAll(@Param("keyword") String keyword, @Param("status") Integer status,
                          @Param("offset") int offset, @Param("limit") int limit);

    Long countAll(@Param("keyword") String keyword, @Param("status") Integer status);

    List<Order> selectExpired(@Param("now") LocalDateTime now);

    java.math.BigDecimal sumPaidAmount(@Param("userId") Long userId, @Param("since") LocalDateTime since);
}
package com.maotuanzi.service.impl;

import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.enums.OrderStatusEnum;
import com.maotuanzi.common.enums.PaymentChannelEnum;
import com.maotuanzi.common.enums.RefundStatusEnum;
import com.maotuanzi.common.enums.VipLevelEnum;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.common.util.IdUtil;
import com.maotuanzi.dto.OrderCreateRequest;
import com.maotuanzi.dto.RefundResult;
import com.maotuanzi.dto.UnifiedOrderResult;
import com.maotuanzi.entity.Coupon;
import com.maotuanzi.entity.CouponClaim;
import com.maotuanzi.entity.Order;
import com.maotuanzi.entity.Product;
import com.maotuanzi.entity.Refund;
import com.maotuanzi.entity.User;
import com.maotuanzi.entity.VipRecord;
import com.maotuanzi.mapper.CouponClaimMapper;
import com.maotuanzi.mapper.CouponMapper;
import com.maotuanzi.mapper.OrderMapper;
import com.maotuanzi.mapper.ProductMapper;
import com.maotuanzi.mapper.RefundMapper;
import com.maotuanzi.mapper.UserMapper;
import com.maotuanzi.mapper.VipRecordMapper;
import com.maotuanzi.security.UserContext;
import com.maotuanzi.service.OrderService;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final VipRecordMapper vipRecordMapper;
    private final RefundMapper refundMapper;
    private final CouponMapper couponMapper;
    private final CouponClaimMapper couponClaimMapper;
    private final WechatPayChannel wechatPayChannel;
    private final HuaweiIapChannel huaweiIapChannel;

    @Override
    @Transactional
    public Order createOrder(OrderCreateRequest req) {
        Long uid = UserContext.getUserId();
        if (uid == null) throw BizException.of(ErrorCode.UNAUTHORIZED);

        Product product = productMapper.selectById(req.getProductId());
        if (product == null) throw BizException.of(ErrorCode.PRODUCT_NOT_FOUND);
        if (product.getIsActive() == 0) throw BizException.of(ErrorCode.PRODUCT_OFFLINE);

        int amount = product.getPriceCents();
        int discount = 0;
        Long claimId = req.getCouponId();

        // 卡券逻辑：req.couponId 为已领取的 CouponClaim.id，校验归属与未使用后计算折扣
        if (claimId != null) {
            CouponClaim claim = couponClaimMapper.selectById(claimId);
            if (claim == null || !claim.getUserId().equals(uid)) {
                throw BizException.of(ErrorCode.COUPON_NOT_FOUND);
            }
            if (claim.getStatus() == null || claim.getStatus() != 0) {
                throw BizException.of(ErrorCode.COUPON_USED);
            }
            Coupon coupon = couponMapper.selectById(claim.getCouponId());
            if (coupon == null || coupon.getIsActive() == null || coupon.getIsActive() != 1) {
                throw BizException.of(ErrorCode.COUPON_NOT_FOUND);
            }
            discount = computeDiscount(coupon, amount);
            // 原型策略：下单即占用卡券（标记已使用），避免重复使用
            couponClaimMapper.updateUsed(claim.getId(), null);
        }

        if (discount > amount) discount = amount;

        Order order = new Order();
        order.setOrderNo(IdUtil.genOrderNo());
        order.setUserId(uid);
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setProductType(product.getProductType());
        order.setAmountCents(amount);
        order.setPayAmountCents(amount - discount);
        order.setDiscountCents(discount);
        order.setCouponId(claimId);
        order.setStatus(OrderStatusEnum.PENDING.getCode());
        order.setWorkId(req.getWorkId());
        order.setClientIp(req.getClientIp());
        order.setExpireAt(LocalDateTime.now().plusMinutes(30));
        orderMapper.insert(order);

        return order;
    }

    /** 卡券折扣计算：1=满减 2=折扣 3=免费券 */
    private int computeDiscount(Coupon coupon, int amount) {
        if (coupon.getType() == null) return 0;
        if (coupon.getType() == 1) {
            int min = coupon.getMinAmountCents() == null ? 0 : coupon.getMinAmountCents();
            if (amount < min) throw BizException.of(ErrorCode.PARAM_INVALID, "未达到卡券使用门槛");
            return coupon.getDiscountCents() == null ? 0 : coupon.getDiscountCents();
        }
        if (coupon.getType() == 2) {
            if (coupon.getDiscountRate() == null) return 0;
            return amount - (int) Math.round(amount * coupon.getDiscountRate().doubleValue());
        }
        if (coupon.getType() == 3) {
            return amount; // 免费券：全免
        }
        return 0;
    }

    @Override
    public Order detail(Long id) {
        Long uid = UserContext.getUserId();
        Order o = orderMapper.selectById(id);
        if (o == null || !o.getUserId().equals(uid)) {
            throw BizException.of(ErrorCode.ORDER_NOT_FOUND);
        }
        return o;
    }

    @Override
    public Order detailByNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public PageResult<Order> listMine(Integer status, int page, int pageSize) {
        Long uid = UserContext.getUserId();
        PageHelper.startPage(page, pageSize);
        List<Order> list = orderMapper.selectByUserId(uid, status, (page - 1) * pageSize, pageSize);
        return PageResult.of(list);
    }

    @Override
    @Transactional
    public synchronized void markPaid(String orderNo, String wxTransactionId) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) throw BizException.of(ErrorCode.ORDER_NOT_FOUND);
        if (order.getStatus() != OrderStatusEnum.PENDING.getCode()) {
            log.info("订单非待支付状态 order_no={} status={}", orderNo, order.getStatus());
            return;
        }

        // 幂等校验（微信交易号；华为 IAP 交易号同样经此幂等）
        Order exist = orderMapper.selectByWxTxn(wxTransactionId);
        if (exist != null && !exist.getOrderNo().equals(orderNo)) {
            log.warn("交易号重复 wx_txn={}", wxTransactionId);
            return;
        }

        LocalDateTime paidAt = LocalDateTime.now();
        orderMapper.updatePaid(order.getId(), wxTransactionId, paidAt, OrderStatusEnum.PAID.getCode());

        // 发权益
        grantEntitlement(order, paidAt);
    }

    private void grantEntitlement(Order order, LocalDateTime paidAt) {
        Product p = productMapper.selectById(order.getProductId());
        if (p == null) return;

        // 会员
        if (p.getProductType() != null && p.getProductType() == 2) {
            User user = userMapper.selectById(order.getUserId());
            VipLevelEnum level = VipLevelEnum.of(p.getId().equals("vip_month") ? 1 :
                    p.getId().equals("vip_quarter") ? 2 : p.getId().equals("vip_year") ? 3 : 1);
            LocalDateTime newExpire = paidAt.plusDays(level.getDays());
            if (user.getVipExpireAt() != null && user.getVipExpireAt().isAfter(paidAt)) {
                newExpire = user.getVipExpireAt().plusDays(level.getDays());
            }

            VipRecord rec = new VipRecord();
            rec.setUserId(user.getId());
            rec.setOrderId(order.getId());
            rec.setChangeType(1);
            rec.setVipLevelBefore(user.getVipLevel());
            rec.setVipLevelAfter(level.getCode());
            rec.setExpireBefore(user.getVipExpireAt());
            rec.setExpireAfter(newExpire);
            rec.setDurationDays(level.getDays());
            rec.setAmountCents(order.getPayAmountCents());
            rec.setRemark("购买" + level.getName());
            vipRecordMapper.insert(rec);

            userMapper.updateVip(user.getId(), level.getCode(), newExpire);
            log.info("开通会员 user_id={} level={} expire={}", user.getId(), level.getCode(), newExpire);
        }

        // 单次作品解锁
        if (order.getWorkId() != null) {
            log.info("解锁作品 order_id={} work_id={}", order.getId(), order.getWorkId());
            // 业务：在 WorkService 中解锁
        }
    }

    @Override
    public void closeExpired() {
        List<Order> list = orderMapper.selectExpired(LocalDateTime.now());
        for (Order o : list) {
            orderMapper.updateClosed(o.getId(), OrderStatusEnum.CLOSED.getCode(), LocalDateTime.now());
        }
        if (!list.isEmpty()) log.info("关闭过期订单 count={}", list.size());
    }

    @Override
    @Transactional
    public void closeOrder(Long id) {
        Long uid = UserContext.getUserId();
        Order o = orderMapper.selectById(id);
        if (o == null || !o.getUserId().equals(uid)) throw BizException.of(ErrorCode.ORDER_NOT_FOUND);
        if (o.getStatus() != OrderStatusEnum.PENDING.getCode()) throw BizException.of(ErrorCode.ORDER_CLOSED);
        orderMapper.updateClosed(id, OrderStatusEnum.CLOSED.getCode(), LocalDateTime.now());
    }

    @Override
    public UnifiedOrderResult unifiedOrder(Long orderId, String channel) {
        Long uid = UserContext.getUserId();
        if (uid == null) throw BizException.of(ErrorCode.UNAUTHORIZED);
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(uid)) {
            throw BizException.of(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != OrderStatusEnum.PENDING.getCode()) {
            throw BizException.of(ErrorCode.STATE_CONFLICT, "订单状态不可发起支付");
        }
        if (!PaymentChannelEnum.isSupported(channel)) {
            throw BizException.of(ErrorCode.PARAM_INVALID, "不支持的支付渠道");
        }

        UnifiedOrderResult result;
        if ("WECHAT".equalsIgnoreCase(channel)) {
            result = wechatPayChannel.unifiedOrder(order, channel);
        } else {
            result = huaweiIapChannel.unifiedOrder(order, channel);
        }
        // 持久化渠道与微信预支付 ID（渠道实现已回填 order.wxPrepayId）
        orderMapper.updateChannelPrepay(order.getId(), channel, order.getWxPrepayId());
        return result;
    }

    @Override
    @Transactional
    public RefundResult refund(Long orderId, String reason) {
        Long uid = UserContext.getUserId();
        if (uid == null) throw BizException.of(ErrorCode.UNAUTHORIZED);
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(uid)) {
            throw BizException.of(ErrorCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != OrderStatusEnum.PAID.getCode()) {
            throw BizException.of(ErrorCode.STATE_CONFLICT, "仅已支付订单可退款");
        }

        // mock 模式：创建退款单并置订单为「退款中」；真实微信退款调用预留（见下 TODO）
        Refund refund = new Refund();
        refund.setRefundNo(IdUtil.genRefundNo());
        refund.setOrderId(order.getId());
        refund.setUserId(uid);
        refund.setAmountCents(order.getPayAmountCents());
        refund.setReason(reason);
        refund.setStatus(RefundStatusEnum.PROCESSING.getCode());
        refundMapper.insert(refund);

        orderMapper.updateStatus(order.getId(), OrderStatusEnum.REFUNDING.getCode());

        // TODO(商业接入): 真实模式若 wechat 渠道已配置凭证，调用微信退款 API /v3/refund/domestic/refunds
        log.info("订单退款（mock）orderNo={} refundNo={}", order.getOrderNo(), refund.getRefundNo());
        return new RefundResult(refund.getRefundNo(), "REFUNDING");
    }
}

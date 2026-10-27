package com.maotuanzi.service.impl;

import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.enums.OrderStatusEnum;
import com.maotuanzi.common.enums.RefundStatusEnum;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.common.util.IdUtil;
import com.maotuanzi.entity.*;
import com.maotuanzi.mapper.*;
import com.maotuanzi.security.AdminContext;
import com.maotuanzi.service.AdminService;
import com.maotuanzi.security.jwt.JwtUtil;
import com.github.pagehelper.PageHelper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;
    private final UserMapper userMapper;
    private final WorkMapper workMapper;
    private final OrderMapper orderMapper;
    private final RefundMapper refundMapper;
    private final SquareMapper squareMapper;
    private final SensitiveWordMapper sensitiveWordMapper;
    private final SystemConfigMapper configMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redis;
    private final BCryptPasswordEncoder pwdEncoder = new BCryptPasswordEncoder();

    @Value("${maotuanzi.jwt.access-expire:604800}")
    private long adminExpire;

    @Override
    public Map<String, Object> login(String username, String password, String ip) {
        AdminUser user = adminMapper.selectByUsername(username);
        if (user == null) throw BizException.of(ErrorCode.UNAUTHORIZED);
        if (user.getStatus() != null && user.getStatus() == 1) throw BizException.of(ErrorCode.ACCOUNT_BANNED);
        if (!pwdEncoder.matches(password, user.getPasswordHash())) {
            throw BizException.of(ErrorCode.UNAUTHORIZED);
        }
        adminMapper.updateLastLogin(user.getId(), ip);

        Set<String> perms = adminMapper.selectPermsByUserId(user.getId()).stream()
                .map(AdminPermission::getCode)
                .collect(Collectors.toCollection(HashSet::new));

        String token = buildAdminToken(user.getId(), perms);

        Map<String, Object> r = new HashMap<>();
        r.put("token", token);
        r.put("expiresIn", adminExpire);
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("nickname", user.getNickname());
        info.put("avatarUrl", user.getAvatarUrl());
        info.put("permissions", perms);
        info.put("roles", adminMapper.selectRolesByUserId(user.getId()).stream()
                .map(AdminRole::getCode).collect(Collectors.toList()));
        r.put("admin", info);
        return r;
    }

    @Override
    public void logout(String token) {
        if (token == null || token.isEmpty()) return;
        try {
            Claims claims = jwtUtil.parse(token);
            long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
            if (remaining > 0) {
                redis.opsForValue().set("admin:token:invalid:" + token, "1", Duration.ofMillis(remaining));
            }
        } catch (io.jsonwebtoken.ExpiredJwtException alreadyExpired) {
            // Expired tokens are unusable; infrastructure failures must propagate.
        }
    }

    private String buildAdminToken(Long adminId, Set<String> perms) {
        return jwtUtil.generateAdmin(adminId, perms, adminExpire);
    }

    @Override
    public PageResult<User> listUsers(String keyword, Integer status, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        return PageResult.of(userMapper.selectByPage(keyword, status, (page - 1) * pageSize, pageSize));
    }

    @Override
    public User getUser(Long id) {
        User u = userMapper.selectById(id);
        if (u == null) throw BizException.of(ErrorCode.NOT_FOUND);
        return u;
    }

    @Override
    public void banUser(Long id) {
        userMapper.updateStatus(id, 1);
    }

    @Override
    public void unbanUser(Long id) {
        userMapper.updateStatus(id, 0);
    }

    @Override
    public PageResult<Work> listWorks(Integer status, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        return PageResult.of(workMapper.selectByUserId(null, status, (page - 1) * pageSize, pageSize));
    }

    @Override
    public void reviewWork(Long id, Integer status) {
        workMapper.updateStatus(id, status);
    }

    @Override
    public PageResult<Order> listOrders(String keyword, Integer status, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        return PageResult.of(orderMapper.selectAll(keyword, status, (page - 1) * pageSize, pageSize));
    }

    @Override
    public Order getOrder(Long id) {
        Order o = orderMapper.selectById(id);
        if (o == null) throw BizException.of(ErrorCode.ORDER_NOT_FOUND);
        return o;
    }

    @Override
    public PageResult<Refund> listRefunds(Integer status, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        return PageResult.of(refundMapper.selectAll(status, (page - 1) * pageSize, pageSize));
    }

    @Override
    @Transactional
    public void reviewRefund(Long id, boolean approve, String remark) {
        Refund r = refundMapper.selectById(id);
        if (r == null) throw BizException.of(ErrorCode.NOT_FOUND);
        if (r.getStatus() != RefundStatusEnum.PENDING.getCode()) {
            throw BizException.of(ErrorCode.STATE_CONFLICT);
        }
        Long adminId = AdminContext.getAdminId();
        String adminName = adminId == null ? "system" : String.valueOf(adminId);

        if (!approve) {
            r.setStatus(RefundStatusEnum.REJECTED.getCode());
            r.setReviewRemark(remark);
            r.setReviewedBy(adminName);
            r.setReviewedAt(LocalDateTime.now());
            refundMapper.updateById(r);
            return;
        }

        // 审核通过 → 退款中
        r.setStatus(RefundStatusEnum.PROCESSING.getCode());
        r.setReviewRemark(remark);
        r.setReviewedBy(adminName);
        r.setReviewedAt(LocalDateTime.now());
        r.setWxRefundId("mock_refund_" + IdUtil.genIdempotencyKey());
        refundMapper.updateById(r);

        // 真实场景：调用微信退款 API
        // 微信回调后状态置为 SUCCESS
        // 这里 mock 直接置为成功
        r.setStatus(RefundStatusEnum.SUCCESS.getCode());
        r.setRefundedAt(LocalDateTime.now());
        refundMapper.updateById(r);

        // 更新订单
        Order order = orderMapper.selectById(r.getOrderId());
        if (order != null) {
            orderMapper.updateRefunded(order.getId(), LocalDateTime.now(), r.getAmountCents(), OrderStatusEnum.REFUNDED.getCode());
        }
    }

    @Override
    public PageResult<SquarePost> listPosts(Integer status, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        return PageResult.of(squareMapper.selectPosts(null, status, (page - 1) * pageSize, pageSize));
    }

    @Override
    public void offlinePost(Long id) {
        SquarePost pp = new SquarePost(); pp.setId(id); pp.setStatus(2); squareMapper.updatePost(pp);
    }

    @Override
    public List<SensitiveWord> listSensitiveWords() {
        return sensitiveWordMapper.selectAllActive();
    }

    @Override
    public void addSensitiveWord(SensitiveWord word) {
        sensitiveWordMapper.insert(word);
    }

    @Override
    public void deleteSensitiveWord(Integer id) {
        sensitiveWordMapper.deleteById(id);
    }

    @Override
    public List<SystemConfig> listConfigs() {
        return configMapper.selectAll();
    }

    @Override
    public void updateConfig(String key, String value, String adminName) {
        configMapper.updateByKey(key, value, adminName);
    }

    @Override
    public Map<String, Object> dashboard() {
        Map<String, Object> data = new HashMap<>();

        // 用户
        Long userTotal = userMapper.countByStatus(0);
        Long userNew = userMapper.countByPage(null, 0);
        data.put("user_total", userTotal);

        // 订单（最近 30 天）
        BigDecimal gmv = BigDecimal.ZERO;
        // 简化：统计 total paid 订单
        Long paid = orderMapper.countAll(null, 1);
        data.put("paid_order_count", paid);
        data.put("gmv_cents", gmv);

        // 退款待审核
        Long pendingRefund = refundMapper.countAll(RefundStatusEnum.PENDING.getCode());
        data.put("pending_refund", pendingRefund);

        // 作品
        Long works = workMapper.countByUserId(null, null);
        data.put("work_count", works);

        data.put("today", LocalDate.now().toString());
        return data;
    }
}
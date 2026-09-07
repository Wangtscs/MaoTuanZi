package com.maotuanzi.controller.api;

import com.maotuanzi.annotation.RequireAuth;
import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.dto.CouponClaimRequest;
import com.maotuanzi.dto.CouponClaimResult;
import com.maotuanzi.entity.Coupon;
import com.maotuanzi.entity.CouponClaim;
import com.maotuanzi.mapper.CouponClaimMapper;
import com.maotuanzi.mapper.CouponMapper;
import com.maotuanzi.security.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDateTime;

/**
 * 卡券接口（阶段2）。
 */
@Api(tags = "卡券")
@RestController
@RequestMapping("/v1/coupons")
@RequireAuth
@RequiredArgsConstructor
public class CouponController extends BaseController {

    private final CouponMapper couponMapper;
    private final CouponClaimMapper couponClaimMapper;

    @ApiOperation("领取卡券")
    @PostMapping("/claim")
    public ApiResult<CouponClaimResult> claim(@Valid @RequestBody CouponClaimRequest req) {
        Long uid = UserContext.getUserId();
        if (uid == null) throw BizException.of(ErrorCode.UNAUTHORIZED);

        Coupon coupon = resolveCoupon(req.getCouponId());
        if (coupon == null) throw BizException.of(ErrorCode.COUPON_NOT_FOUND);

        LocalDateTime now = LocalDateTime.now();
        if (coupon.getValidFrom() != null && now.isBefore(coupon.getValidFrom())) {
            throw BizException.of(ErrorCode.COUPON_EXPIRED, "卡券未开始");
        }
        if (coupon.getValidTo() != null && now.isAfter(coupon.getValidTo())) {
            throw BizException.of(ErrorCode.COUPON_EXPIRED);
        }

        Long claimed = couponClaimMapper.countByUserAndCoupon(uid, coupon.getId());
        int limit = coupon.getPerUserLimit() == null ? 1 : coupon.getPerUserLimit();
        if (claimed != null && claimed >= limit) {
            throw BizException.of(ErrorCode.STATE_CONFLICT, "已达领取上限");
        }

        CouponClaim claim = new CouponClaim();
        claim.setCouponId(coupon.getId());
        claim.setUserId(uid);
        claim.setStatus(0);
        claim.setExpireAt(coupon.getValidTo());
        couponClaimMapper.insert(claim);

        // 扣减库存（best-effort）
        couponMapper.decrRemain(coupon.getId());

        return ApiResult.ok(new CouponClaimResult(String.valueOf(claim.getId()), "CLAIMED"));
    }

    /** 卡券 code 优先，其次按数字 id 解析 */
    private Coupon resolveCoupon(String couponId) {
        if (couponId == null || couponId.isBlank()) return null;
        Coupon byCode = couponMapper.selectByCode(couponId.trim());
        if (byCode != null) return byCode;
        try {
            Long id = Long.parseLong(couponId.trim());
            return couponMapper.selectById(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

package com.maotuanzi.controller.api;

import com.maotuanzi.annotation.RequireAuth;
import com.maotuanzi.common.enums.ProductTypeEnum;
import com.maotuanzi.common.enums.VipLevelEnum;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.dto.VipInfo;
import com.maotuanzi.dto.VipPlan;
import com.maotuanzi.entity.Product;
import com.maotuanzi.entity.User;
import com.maotuanzi.service.ProductService;
import com.maotuanzi.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * VIP 合约对齐接口（阶段0）。
 */
@Slf4j
@RestController
@RequestMapping("/v1/vip")
@RequireAuth
@RequiredArgsConstructor
public class VipController extends BaseController {

    private final ProductService productService;
    private final UserService userService;

    @ApiOperation("我的 VIP 信息")
    @GetMapping("/me")
    public ApiResult<VipInfo> me() {
        User u = userService.me();
        VipInfo info = new VipInfo();
        info.setVipLevel(u.getVipLevel() == null ? 0 : u.getVipLevel());
        info.setVipExpireAt(u.getVipExpireAt());
        info.setBenefits(benefitsFor(u.getVipLevel()));
        return ApiResult.ok(info);
    }

    @ApiOperation("VIP 套餐列表")
    @GetMapping("/plans")
    public ApiResult<List<VipPlan>> plans() {
        List<Product> subs = productService.listActive(ProductTypeEnum.SUBSCRIPTION.getCode());
        List<VipPlan> plans;
        if (subs != null && !subs.isEmpty()) {
            plans = subs.stream().map(this::toPlan).collect(Collectors.toList());
        } else {
            plans = defaultPlans();
        }
        return ApiResult.ok(plans);
    }

    private VipPlan toPlan(Product p) {
        VipPlan plan = new VipPlan();
        plan.setId(p.getId());
        plan.setProductType(p.getProductType() == null ? ProductTypeEnum.SUBSCRIPTION.getCode() : p.getProductType());
        plan.setName(p.getName());
        plan.setPriceCents(p.getPriceCents() == null ? 0 : p.getPriceCents());
        plan.setDurationDays(p.getDurationDays() == null ? 30 : p.getDurationDays());
        plan.setTag(tagOf(p.getId()));
        return plan;
    }

    private List<VipPlan> defaultPlans() {
        List<VipPlan> list = new ArrayList<>();
        list.add(buildPlan("vip_month", VipLevelEnum.MONTH.getName(), 3000, 30));
        list.add(buildPlan("vip_quarter", VipLevelEnum.QUARTER.getName(), 8000, 90));
        list.add(buildPlan("vip_year", VipLevelEnum.YEAR.getName(), 28000, 365));
        return list;
    }

    private VipPlan buildPlan(String id, String name, int price, int days) {
        VipPlan plan = new VipPlan();
        plan.setId(id);
        plan.setProductType(ProductTypeEnum.SUBSCRIPTION.getCode());
        plan.setName(name);
        plan.setPriceCents(price);
        plan.setDurationDays(days);
        plan.setTag(tagOf(id));
        return plan;
    }

    private String tagOf(String id) {
        if ("vip_month".equals(id)) return "热门";
        if ("vip_quarter".equals(id)) return "超值";
        if ("vip_year".equals(id)) return "最划算";
        return null;
    }

    private List<String> benefitsFor(Integer level) {
        VipLevelEnum v = VipLevelEnum.of(level);
        if (v == VipLevelEnum.NONE) {
            return Arrays.asList("每日 3 次 AI 生成", "标准画质下载");
        }
        return Arrays.asList("AI 高清生成", "作品无限下载", "3D 数字手办解锁", "专属客服");
    }
}

package com.maotuanzi.controller.ops;

import com.maotuanzi.annotation.RequireAdmin;
import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.entity.AdminUser;
import com.maotuanzi.entity.AdminRole;
import com.maotuanzi.entity.AdminPermission;
import com.maotuanzi.mapper.AdminMapper;
import com.maotuanzi.security.AdminContext;
import com.maotuanzi.security.AdminPermissions;
import com.maotuanzi.service.AdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/** Operations authentication; preserves the business-admin response and adds a stable ops profile. */
@Api(tags = "运维平台-认证")
@RestController
@RequestMapping("/ops/auth")
@RequiredArgsConstructor
public class OpsAuthController extends BaseController {
    private final AdminService adminService;
    private final AdminMapper adminMapper;

    @ApiOperation("运维登录")
    @PostMapping("/login")
    public ApiResult<Map<String,Object>> login(@RequestBody Map<String,String> body, HttpServletRequest req) {
        String username=body==null?null:body.get("username");
        String password=body==null?null:body.get("password");
        if(username==null||username.trim().isEmpty()||username.length()>128||password==null||password.isEmpty()||password.length()>256)
            throw BizException.of(ErrorCode.PARAM_INVALID);
        Map<String,Object> original=adminService.login(username.trim(),password,clientIp(req));
        Object nested=original.get("admin");
        Map<?,?> profile=nested instanceof Map?(Map<?,?>)nested:original;
        Object rawPermissions=profile.containsKey("permissions")?profile.get("permissions"):original.get("perms");
        Set<String> permissions=AdminPermissions.normalize(rawPermissions);
        if(!hasOpsPerm(permissions))throw new BizException(ErrorCode.FORBIDDEN,"该账号没有运维平台权限");
        Map<String,Object> result=new LinkedHashMap<>(original);
        result.put("adminId",profile.get("id"));
        result.put("username",profile.get("username"));
        result.put("nickname",profile.get("nickname"));
        result.put("avatarUrl",profile.get("avatarUrl"));
        result.put("roles",profile.get("roles"));
        result.put("perms",permissions);
        result.put("opsAccess",true);
        return ApiResult.ok(result);
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    @RequireAdmin
    public ApiResult<Void> logout(HttpServletRequest req) {
        String header=req.getHeader("X-Admin-Token");
        if(header==null||!header.startsWith("Bearer "))header=req.getHeader("Authorization");
        if(header!=null&&header.startsWith("Bearer "))adminService.logout(header.substring(7));
        return ApiResult.ok();
    }

    @ApiOperation("当前运维账号信息")
    @GetMapping("/me")
    @RequireAdmin
    public ApiResult<Map<String,Object>> me() {
        Long id=AdminContext.getAdminId();
        AdminUser user=id==null?null:adminMapper.selectById(id);
        if(user==null)throw BizException.of(ErrorCode.UNAUTHORIZED);
        if(Integer.valueOf(1).equals(user.getStatus()))throw BizException.of(ErrorCode.FORBIDDEN);
        Set<String> permissions=adminMapper.selectPermsByUserId(id).stream().map(AdminPermission::getCode).collect(Collectors.toCollection(LinkedHashSet::new));
        if(!hasOpsPerm(permissions))throw BizException.of(ErrorCode.FORBIDDEN);
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("adminId",id);result.put("username",user.getUsername());
        result.put("nickname",user.getNickname());result.put("avatarUrl",user.getAvatarUrl());
        result.put("perms",permissions);result.put("opsAccess",true);
        result.put("roles",adminMapper.selectRolesByUserId(id).stream().map(AdminRole::getCode).collect(Collectors.toList()));
        return ApiResult.ok(result);
    }

    private boolean hasOpsPerm(Object raw) {
        Set<String> permissions=AdminPermissions.normalize(raw);
        return permissions.contains("*")||permissions.stream().anyMatch(p->p.startsWith("ops."));
    }
    private String clientIp(HttpServletRequest req) {return req.getRemoteAddr();}
}

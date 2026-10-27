package com.maotuanzi.controller.api;

import com.maotuanzi.annotation.RequireAuth;
import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.dto.PetCreateRequest;
import com.maotuanzi.entity.Pet;
import com.maotuanzi.service.PetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "宠物档案")
@RestController
@RequestMapping("/v1/pets")
@RequireAuth
@RequiredArgsConstructor
public class PetController extends BaseController {

    private final PetService petService;

    @ApiOperation("列表")
    @GetMapping
    public ApiResult<List<Pet>> list() {
        return ApiResult.ok(petService.list());
    }

    @ApiOperation("详情")
    @GetMapping("/{id}")
    public ApiResult<Pet> detail(@PathVariable Long id) {
        return ApiResult.ok(petService.detail(id));
    }

    @ApiOperation("创建")
    @PostMapping
    public ApiResult<Pet> create(@Valid @RequestBody PetCreateRequest req) {
        return ApiResult.ok(petService.create(req));
    }

    @ApiOperation("更新")
    @PatchMapping("/{id}")
    public ApiResult<Pet> update(@PathVariable Long id, @Valid @RequestBody PetCreateRequest req) {
        return ApiResult.ok(petService.update(id, req));
    }

    @ApiOperation("删除")
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        petService.delete(id);
        return ApiResult.ok();
    }
}
package com.maotuanzi.controller.api;

import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.entity.Breed;
import com.maotuanzi.service.BreedService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "品种字典")
@RestController
@RequestMapping("/v1/breeds")
@RequiredArgsConstructor
public class BreedController extends BaseController {

    private final BreedService breedService;

    @ApiOperation("按品类列表")
    @GetMapping
    public ApiResult<List<Breed>> list(@RequestParam(required = false) Integer species) {
        return ApiResult.ok(breedService.list(species));
    }

    @ApiOperation("详情")
    @GetMapping("/{id}")
    public ApiResult<Breed> detail(@PathVariable Integer id) {
        return ApiResult.ok(breedService.detail(id));
    }
}
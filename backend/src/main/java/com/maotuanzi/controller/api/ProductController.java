package com.maotuanzi.controller.api;

import com.maotuanzi.common.result.ApiResult;
import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.controller.BaseController;
import com.maotuanzi.entity.Product;
import com.maotuanzi.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "商品")
@RestController
@RequestMapping("/v1/products")
@RequiredArgsConstructor
public class ProductController extends BaseController {

    private final ProductService productService;

    @ApiOperation("上架商品列表")
    @GetMapping
    public ApiResult<List<Product>> list(@RequestParam(required = false) Integer type) {
        return ApiResult.ok(productService.listActive(type));
    }

    @ApiOperation("详情")
    @GetMapping("/{id}")
    public ApiResult<Product> detail(@PathVariable String id) {
        return ApiResult.ok(productService.detail(id));
    }

    @ApiOperation("管理分页")
    @GetMapping("/page")
    public ApiResult<PageResult<Product>> page(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) Integer type,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.ok(productService.page(keyword, type, page, pageSize));
    }
}
package com.maotuanzi.service.impl;

import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.entity.Product;
import com.maotuanzi.mapper.ProductMapper;
import com.maotuanzi.service.ProductService;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    @Cacheable(value = "products", key = "#type")
    public List<Product> listActive(Integer type) {
        return productMapper.selectActive(type);
    }

    @Override
    public Product detail(String id) {
        Product p = productMapper.selectById(id);
        if (p == null) throw BizException.of(ErrorCode.PRODUCT_NOT_FOUND);
        return p;
    }

    @Override
    public PageResult<Product> page(String keyword, Integer type, int page, int pageSize) {
        PageHelper.startPage(page, pageSize);
        List<Product> list = productMapper.selectAll(keyword, type, (page - 1) * pageSize, pageSize);
        return PageResult.of(list);
    }
}
package com.maotuanzi.service;

import com.maotuanzi.common.result.PageResult;
import com.maotuanzi.entity.Product;

import java.util.List;

public interface ProductService {
    List<Product> listActive(Integer type);
    Product detail(String id);
    PageResult<Product> page(String keyword, Integer type, int page, int pageSize);
}
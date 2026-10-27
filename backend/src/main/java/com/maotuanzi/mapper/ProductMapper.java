package com.maotuanzi.mapper;

import com.maotuanzi.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    Product selectById(@Param("id") String id);

    List<Product> selectActive(@Param("type") Integer type);

    List<Product> selectAll(@Param("keyword") String keyword, @Param("type") Integer type,
                            @Param("offset") int offset, @Param("limit") int limit);

    Long countAll(@Param("keyword") String keyword, @Param("type") Integer type);

    int updateById(Product product);

    int insert(Product product);
}
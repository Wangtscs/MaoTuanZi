package com.maotuanzi.mapper;

import com.maotuanzi.entity.AiStyle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiStyleMapper {

    AiStyle selectById(@Param("id") Integer id);

    List<AiStyle> selectByCategory(@Param("category") String category);

    List<AiStyle> selectAll(@Param("category") String category, @Param("keyword") String keyword,
                           @Param("offset") int offset, @Param("limit") int limit);

    Long countAll(@Param("category") String category, @Param("keyword") String keyword);

    int insert(AiStyle style);

    int updateById(AiStyle style);
}
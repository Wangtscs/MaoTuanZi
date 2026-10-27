package com.maotuanzi.mapper;

import com.maotuanzi.entity.AiTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AiTemplateMapper {

    AiTemplate selectById(@Param("id") Integer id);

    List<AiTemplate> selectByCategory(@Param("category") String category);

    int insert(AiTemplate template);

    int updateById(AiTemplate template);
}
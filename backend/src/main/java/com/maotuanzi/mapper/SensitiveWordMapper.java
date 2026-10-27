package com.maotuanzi.mapper;

import com.maotuanzi.entity.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SensitiveWordMapper {
    List<SensitiveWord> selectAllActive();
    int insert(SensitiveWord w);
    int updateById(SensitiveWord w);
    int deleteById(@Param("id") Integer id);
}
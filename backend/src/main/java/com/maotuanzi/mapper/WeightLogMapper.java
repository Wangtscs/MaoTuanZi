package com.maotuanzi.mapper;

import com.maotuanzi.entity.WeightLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WeightLogMapper {
    int insert(WeightLog log);
    int updateById(WeightLog log);
    int deleteById(@Param("id") Long id);
    WeightLog selectById(@Param("id") Long id);
    List<WeightLog> selectByPet(@Param("userId") Long userId, @Param("petId") Long petId, @Param("limit") int limit);
}
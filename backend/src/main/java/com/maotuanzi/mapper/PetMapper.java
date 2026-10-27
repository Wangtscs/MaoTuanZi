package com.maotuanzi.mapper;

import com.maotuanzi.entity.Pet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PetMapper {

    int insert(Pet pet);

    int updateById(Pet pet);

    int softDelete(@Param("id") Long id, @Param("now") java.time.LocalDateTime now);

    Pet selectById(@Param("id") Long id);

    List<Pet> selectByUserId(@Param("userId") Long userId);

    Long countByUserId(@Param("userId") Long userId);
}
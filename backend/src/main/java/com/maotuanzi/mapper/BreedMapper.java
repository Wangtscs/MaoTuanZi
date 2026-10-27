package com.maotuanzi.mapper;

import com.maotuanzi.entity.Breed;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BreedMapper {

    Breed selectById(@Param("id") Integer id);

    List<Breed> selectBySpecies(@Param("species") Integer species);

    List<Breed> selectAll(@Param("keyword") String keyword, @Param("species") Integer species,
                          @Param("offset") int offset, @Param("limit") int limit);

    Long countAll(@Param("keyword") String keyword, @Param("species") Integer species);

    int insert(Breed breed);

    int updateById(Breed breed);

    int deleteById(@Param("id") Integer id);
}
package com.maotuanzi.mapper;

import com.maotuanzi.entity.JournalEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JournalMapper {
    int insert(JournalEntry e);
    int updateById(JournalEntry e);
    int softDelete(@Param("id") Long id, @Param("now") java.time.LocalDateTime now);
    JournalEntry selectById(@Param("id") Long id);
    List<JournalEntry> selectByPet(@Param("userId") Long userId, @Param("petId") Long petId,
                                   @Param("offset") int offset, @Param("limit") int limit);
    Long countByPet(@Param("userId") Long userId, @Param("petId") Long petId);
}
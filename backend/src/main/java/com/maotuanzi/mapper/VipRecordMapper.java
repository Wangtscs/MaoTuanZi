package com.maotuanzi.mapper;

import com.maotuanzi.entity.VipRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VipRecordMapper {
    int insert(VipRecord record);
    List<VipRecord> selectByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    Long countByUserId(@Param("userId") Long userId);
}
package com.maotuanzi.mapper;

import com.maotuanzi.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuditLogMapper {
    int insert(AuditLog log);
    java.util.List<AuditLog> selectRecent(@Param("contentType") String contentType, @Param("limit") int limit);
}
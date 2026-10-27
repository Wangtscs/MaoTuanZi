package com.maotuanzi.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IdempotencyMapper {
    int insert(@Param("key") String key, @Param("userId") Long userId, @Param("endpoint") String endpoint,
               @Param("response") String response, @Param("expiresAt") java.time.LocalDateTime expiresAt);
    String selectResponse(@Param("key") String key);
    int deleteExpired(@Param("now") java.time.LocalDateTime now);
}
package com.maotuanzi.mapper;

import com.maotuanzi.entity.AiTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AiTaskMapper {

    int insert(AiTask task);

    int updateById(AiTask task);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status,
                     @Param("errorCode") Integer errorCode, @Param("errorMessage") String errorMessage,
                     @Param("finishedAt") LocalDateTime finishedAt);

    AiTask selectById(@Param("id") Long id);

    List<AiTask> selectRunning(@Param("now") LocalDateTime now);

    List<AiTask> selectByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);

    Long countByUserId(@Param("userId") Long userId);

    int countRunningByUser(@Param("userId") Long userId);
}
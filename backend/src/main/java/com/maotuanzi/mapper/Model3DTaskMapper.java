package com.maotuanzi.mapper;

import com.maotuanzi.entity.Model3DTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 3D 模型生成任务 Mapper。
 */
@Mapper
public interface Model3DTaskMapper {

    int insert(Model3DTask task);

    int updateResult(@Param("taskId") String taskId,
                     @Param("status") String status,
                     @Param("modelUrl") String modelUrl,
                     @Param("ossKey") String ossKey,
                     @Param("errorMsg") String errorMsg);

    Model3DTask selectByTaskId(@Param("taskId") String taskId);
}

package com.maotuanzi.mapper;

import com.maotuanzi.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SystemConfigMapper {
    SystemConfig selectByKey(@Param("configKey") String configKey);
    List<SystemConfig> selectAll();
    int updateByKey(@Param("configKey") String configKey, @Param("configValue") String configValue, @Param("updatedBy") String updatedBy);
}
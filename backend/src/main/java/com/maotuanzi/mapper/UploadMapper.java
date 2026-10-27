package com.maotuanzi.mapper;

import com.maotuanzi.entity.Upload;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UploadMapper {
    int insert(Upload upload);
    Upload selectByOssKey(@Param("ossKey") String ossKey);
}
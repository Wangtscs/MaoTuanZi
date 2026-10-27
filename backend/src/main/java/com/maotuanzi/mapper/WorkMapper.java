package com.maotuanzi.mapper;

import com.maotuanzi.entity.Work;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkMapper {

    int insert(Work work);

    int updateById(Work work);

    int softDelete(@Param("id") Long id, @Param("now") java.time.LocalDateTime now);

    Work selectById(@Param("id") Long id);

    List<Work> selectByUserId(@Param("userId") Long userId, @Param("status") Integer status,
                              @Param("offset") int offset, @Param("limit") int limit);

    Long countByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    List<Work> selectPublicFeed(@Param("topic") String topic, @Param("offset") int offset, @Param("limit") int limit);

    Long countPublicFeed(@Param("topic") String topic);

    int incrLike(@Param("id") Long id, @Param("delta") int delta);

    int updateResultUrl(@Param("id") Long id, @Param("url") String url);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
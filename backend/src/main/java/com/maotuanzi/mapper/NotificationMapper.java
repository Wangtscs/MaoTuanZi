package com.maotuanzi.mapper;

import com.maotuanzi.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {
    int insert(Notification n);
    int markRead(@Param("id") Long id, @Param("userId") Long userId);
    int markAllRead(@Param("userId") Long userId);
    Long countUnread(@Param("userId") Long userId);
    List<Notification> selectByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
}
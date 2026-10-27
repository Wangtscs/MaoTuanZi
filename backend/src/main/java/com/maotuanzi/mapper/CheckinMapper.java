package com.maotuanzi.mapper;

import com.maotuanzi.entity.Checkin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CheckinMapper {
    int insert(Checkin c);
    Checkin selectToday(@Param("userId") Long userId, @Param("topic") String topic, @Param("date") LocalDate date);
    int countByUserTopic(@Param("userId") Long userId, @Param("topic") String topic);
    List<Checkin> selectByTopic(@Param("topic") String topic, @Param("offset") int offset, @Param("limit") int limit);
}
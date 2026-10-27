package com.maotuanzi.mapper;

import com.maotuanzi.entity.HealthReminder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface HealthReminderMapper {
    int insert(HealthReminder r);
    int updateById(HealthReminder r);
    int softDelete(@Param("id") Long id, @Param("now") LocalDateTime now);
    HealthReminder selectById(@Param("id") Long id);
    List<HealthReminder> selectByUserId(@Param("userId") Long userId, @Param("petId") Long petId);
    Long countByUserId(@Param("userId") Long userId);
    List<HealthReminder> selectDue(@Param("now") LocalDateTime now);
}
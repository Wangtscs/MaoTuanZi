package com.maotuanzi.mapper;

import com.maotuanzi.entity.Reward;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface RewardMapper {

    int insert(Reward reward);

    int updateClaimed(Reward reward);

    Reward selectById(@Param("id") Long id);

    List<Reward> selectByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 查询某用户当天是否已发放过分享奖励（用于幂等 / 每日上限）。
     */
    Reward selectPendingShareToday(@Param("userId") Long userId, @Param("day") LocalDate day);
}

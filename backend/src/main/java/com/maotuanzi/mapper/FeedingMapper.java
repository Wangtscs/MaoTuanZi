package com.maotuanzi.mapper;

import com.maotuanzi.entity.Feeding;
import com.maotuanzi.entity.FeedingPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FeedingMapper {
    int insertRecord(Feeding f);
    int deleteRecord(@Param("id") Long id);
    List<Feeding> selectRecords(@Param("userId") Long userId, @Param("petId") Long petId, @Param("limit") int limit);
    int insertPlan(FeedingPlan p);
    int updatePlan(FeedingPlan p);
    FeedingPlan selectPlanByPet(@Param("userId") Long userId, @Param("petId") Long petId);
}
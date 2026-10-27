package com.maotuanzi.service;

import com.maotuanzi.entity.OpsJob;

import java.util.List;
import java.util.Map;

/**
 * 运维任务调度服务
 */
public interface OpsJobService {

    List<OpsJob> listJobs(String keyword, Integer enabled);

    OpsJob getJob(Long id);

    Long createJob(OpsJob job);

    void updateJob(OpsJob job);

    void deleteJob(Long id);

    void toggleJob(Long id, Integer enabled);

    /** 手动触发一次（同步执行并返回结果） */
    Map<String, Object> trigger(Long id, String operator);

    Map<String, Object> listRecords(Long jobId, String status, int page, int pageSize);

    Map<String, Object> stat(int days);

    /** 内置处理器清单 */
    List<Map<String, Object>> handlers();

    /** 调度器驱动：扫描到点任务并执行 */
    void dispatch();
}

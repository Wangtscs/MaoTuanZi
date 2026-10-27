package com.maotuanzi.service;

import com.maotuanzi.dto.AiTaskCreateRequest;
import com.maotuanzi.entity.AiTask;
import com.maotuanzi.entity.Work;

public interface AiService {
    AiTask createTask(AiTaskCreateRequest req);
    AiTask queryTask(Long taskId);
    void cancelTask(Long taskId);
    void processAsync(Long taskId);
    Work getWork(Long workId);
    Object queryResult(Long workId);
}
package com.maotuanzi.task;

import com.maotuanzi.mapper.IdempotencyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CleanupTask {

    private final IdempotencyMapper idempotencyMapper;

    /** 每天凌晨清理过期幂等键 */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanIdempotencyKeys() {
        try {
            int n = idempotencyMapper.deleteExpired(LocalDateTime.now());
            log.info("清理过期幂等键 count={}", n);
        } catch (Exception e) {
            log.error("清理失败", e);
        }
    }
}
package com.maotuanzi.task;

import com.maotuanzi.entity.HealthReminder;
import com.maotuanzi.mapper.HealthReminderMapper;
import com.maotuanzi.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderTask {

    private final HealthReminderMapper reminderMapper;
    private final NotificationService notificationService;

    /** 每 5 分钟扫描到期提醒 */
    @Scheduled(fixedDelay = 300_000L)
    @Transactional
    public void sendDueReminders() {
        try {
            List<HealthReminder> due = reminderMapper.selectDue(LocalDateTime.now());
            for (HealthReminder r : due) {
                notificationService.send(
                        r.getUserId(),
                        "health_reminder",
                        r.getTitle() + " 提醒",
                        "您的毛孩子今天该做【" + r.getTitle() + "】了～",
                        "{\"reminder_id\":" + r.getId() + "}"
                );
                r.setLastRunAt(LocalDateTime.now());
                if (r.getCycleDays() != null && r.getCycleDays() > 0) {
                    r.setNextRunAt(r.getNextRunAt().plusDays(r.getCycleDays()));
                } else {
                    r.setIsActive(0);
                }
                reminderMapper.updateById(r);
            }
            if (!due.isEmpty()) log.info("已发送健康提醒 count={}", due.size());
        } catch (Exception e) {
            log.error("健康提醒任务失败", e);
        }
    }
}
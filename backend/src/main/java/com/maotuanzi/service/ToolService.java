package com.maotuanzi.service;

import com.maotuanzi.dto.FeedingRequest;
import com.maotuanzi.dto.HealthReminderRequest;
import com.maotuanzi.dto.JournalRequest;
import com.maotuanzi.dto.WeightLogRequest;
import com.maotuanzi.entity.*;

import java.util.List;

public interface ToolService {
    // 健康提醒
    List<HealthReminder> listReminders(Long petId);
    HealthReminder createReminder(HealthReminderRequest req);
    HealthReminder updateReminder(Long id, HealthReminderRequest req);
    void deleteReminder(Long id);

    // 体重
    List<WeightLog> listWeights(Long petId);
    WeightLog addWeight(WeightLogRequest req);
    void deleteWeight(Long id);

    // 时光手册
    List<JournalEntry> listJournal(Long petId, int page, int pageSize);
    JournalEntry addJournal(JournalRequest req);
    JournalEntry updateJournal(Long id, JournalRequest req);
    void deleteJournal(Long id);

    // 喂养
    List<Feeding> listFeedings(Long petId, int limit);
    Feeding addFeeding(FeedingRequest req);
    void deleteFeeding(Long id);

    // 喂养方案（基础版 + 付费解锁）
    Object generateFeedingPlan(Long petId);
    Object queryFeedingPlan(Long petId);
}
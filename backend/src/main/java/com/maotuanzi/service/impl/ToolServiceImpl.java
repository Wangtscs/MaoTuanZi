package com.maotuanzi.service.impl;

import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.exception.BizException;
import com.maotuanzi.dto.FeedingRequest;
import com.maotuanzi.dto.HealthReminderRequest;
import com.maotuanzi.dto.JournalRequest;
import com.maotuanzi.dto.WeightLogRequest;
import com.maotuanzi.entity.*;
import com.maotuanzi.mapper.*;
import com.maotuanzi.security.UserContext;
import com.maotuanzi.service.PetService;
import com.maotuanzi.service.ToolService;
import com.maotuanzi.service.UserService;
import com.maotuanzi.service.WxMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class ToolServiceImpl implements ToolService {

    private final HealthReminderMapper reminderMapper;
    private final WeightLogMapper weightMapper;
    private final JournalMapper journalMapper;
    private final FeedingMapper feedingMapper;
    private final SystemConfigMapper configMapper;
    private final ObjectMapper objectMapper;
    private final PetService petService;
    private final UserService userService;
    private final WxMessageService wxMessageService;

    @Override
    public List<HealthReminder> listReminders(Long petId) {
        Long uid = UserContext.getUserId();
        return reminderMapper.selectByUserId(uid, petId);
    }

    @Override
    @Transactional
    public HealthReminder createReminder(HealthReminderRequest req) {
        Long uid = UserContext.getUserId();
        Pet p = petService.detail(req.getPetId());
        if (!p.getUserId().equals(uid)) throw BizException.of(ErrorCode.PET_NOT_FOUND);

        Long count = reminderMapper.countByUserId(uid);
        if (!userService.isVip(uid) && count >= 3) {
            throw BizException.of(ErrorCode.REMINDER_LIMIT_EXCEEDED);
        }
        if (req.getNextRunAt().isBefore(LocalDateTime.now())) {
            throw BizException.of(ErrorCode.REMINDER_TIME_INVALID);
        }

        HealthReminder r = new HealthReminder();
        r.setUserId(uid);
        r.setPetId(req.getPetId());
        r.setType(req.getType());
        r.setTitle(req.getTitle());
        r.setCycleDays(req.getCycleDays());
        r.setNextRunAt(req.getNextRunAt());
        r.setNote(req.getNote());
        r.setIsActive(1);
        reminderMapper.insert(r);

        // 发送微信订阅消息提醒（定时任务触发时调用）
        // 示例集成代码（实际应由定时任务触发）：
        // User u = userService.getById(uid);
        // if (u != null && u.getOpenId() != null) {
        //     Map<String, String> data = new TreeMap<>();
        //     data.put("thing1", r.getTitle());
        //     data.put("date2", r.getNextRunAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        //     wxMessageService.sendSubscribeMessage(u.getOpenId(), "HEALTH_REMINDER_TEMPLATE_ID", data);
        // }

        return r;
    }

    @Override
    @Transactional
    public HealthReminder updateReminder(Long id, HealthReminderRequest req) {
        Long uid = UserContext.getUserId();
        HealthReminder exist = reminderMapper.selectById(id);
        if (exist == null || !exist.getUserId().equals(uid)) throw BizException.of(ErrorCode.REMINDER_NOT_FOUND);

        exist.setTitle(req.getTitle());
        exist.setType(req.getType());
        exist.setCycleDays(req.getCycleDays());
        exist.setNextRunAt(req.getNextRunAt());
        exist.setNote(req.getNote());
        reminderMapper.updateById(exist);
        return reminderMapper.selectById(id);
    }

    @Override
    @Transactional
    public void deleteReminder(Long id) {
        Long uid = UserContext.getUserId();
        HealthReminder exist = reminderMapper.selectById(id);
        if (exist == null || !exist.getUserId().equals(uid)) throw BizException.of(ErrorCode.REMINDER_NOT_FOUND);
        reminderMapper.softDelete(id, LocalDateTime.now());
    }

    @Override
    public List<WeightLog> listWeights(Long petId) {
        Long uid = UserContext.getUserId();
        return weightMapper.selectByPet(uid, petId, 90);
    }

    @Override
    @Transactional
    public WeightLog addWeight(WeightLogRequest req) {
        Long uid = UserContext.getUserId();
        petService.detail(req.getPetId());
        WeightLog w = new WeightLog();
        w.setUserId(uid);
        w.setPetId(req.getPetId());
        w.setWeightKg(req.getWeightKg());
        w.setRecordedAt(req.getRecordedAt());
        w.setNote(req.getNote());
        weightMapper.insert(w);
        return w;
    }

    @Override
    @Transactional
    public void deleteWeight(Long id) {
        weightMapper.deleteById(id);
    }

    @Override
    public List<JournalEntry> listJournal(Long petId, int page, int pageSize) {
        Long uid = UserContext.getUserId();
        PageHelper.startPage(page, pageSize);
        return journalMapper.selectByPet(uid, petId, (page - 1) * pageSize, pageSize);
    }

    @Override
    @Transactional
    public JournalEntry addJournal(JournalRequest req) {
        Long uid = UserContext.getUserId();
        Pet p = petService.detail(req.getPetId());
        JournalEntry j = new JournalEntry();
        j.setUserId(uid);
        j.setPetId(req.getPetId());
        j.setTitle(req.getTitle());
        j.setContent(req.getContent());
        j.setImages(req.getImages());
        j.setMood(req.getMood());
        j.setEntryDate(req.getEntryDate() != null ? req.getEntryDate() : LocalDate.now());
        journalMapper.insert(j);
        return j;
    }

    @Override
    @Transactional
    public JournalEntry updateJournal(Long id, JournalRequest req) {
        Long uid = UserContext.getUserId();
        JournalEntry exist = journalMapper.selectById(id);
        if (exist == null || !exist.getUserId().equals(uid)) throw BizException.of(ErrorCode.NOT_FOUND);
        exist.setTitle(req.getTitle());
        exist.setContent(req.getContent());
        exist.setImages(req.getImages());
        exist.setMood(req.getMood());
        exist.setEntryDate(req.getEntryDate());
        journalMapper.updateById(exist);
        return exist;
    }

    @Override
    @Transactional
    public void deleteJournal(Long id) {
        Long uid = UserContext.getUserId();
        JournalEntry exist = journalMapper.selectById(id);
        if (exist == null || !exist.getUserId().equals(uid)) throw BizException.of(ErrorCode.NOT_FOUND);
        journalMapper.softDelete(id, LocalDateTime.now());
    }

    @Override
    public List<Feeding> listFeedings(Long petId, int limit) {
        Long uid = UserContext.getUserId();
        return feedingMapper.selectRecords(uid, petId, limit);
    }

    @Override
    @Transactional
    public Feeding addFeeding(FeedingRequest req) {
        Long uid = UserContext.getUserId();
        petService.detail(req.getPetId());
        Feeding f = new Feeding();
        f.setUserId(uid);
        f.setPetId(req.getPetId());
        f.setFedAt(req.getFedAt() != null ? req.getFedAt() : LocalDateTime.now());
        f.setFoodType(req.getFoodType());
        f.setAmountG(req.getAmountG());
        f.setNote(req.getNote());
        feedingMapper.insertRecord(f);
        return f;
    }

    @Override
    @Transactional
    public void deleteFeeding(Long id) {
        feedingMapper.deleteRecord(id);
    }

    @Override
    public Object generateFeedingPlan(Long petId) {
        Long uid = UserContext.getUserId();
        Pet p = petService.detail(petId);
        Map<String, Object> plan = new HashMap<>();
        plan.put("summary", "根据" + p.getNickname() + "的档案生成的专属喂养方案");
        plan.put("daily_food", Map.of("amount_g", 65, "times_per_day", 2));
        plan.put("nutrition", Map.of("protein_pct", 32, "fat_pct", 14));
        plan.put("schedule", java.util.Arrays.asList(
                Map.of("time", "08:00", "action", "喂食"),
                Map.of("time", "20:00", "action", "喂食")));
        plan.put("warnings", java.util.Arrays.asList("严格控制零食量", "避免突然换粮"));
        plan.put("disclaimer", "本方案仅供参考，具体喂养请结合宠物实际情况并咨询兽医。");

        try {
            FeedingPlan fp = feedingMapper.selectPlanByPet(uid, petId);
            if (fp == null) {
                fp = new FeedingPlan();
                fp.setUserId(uid);
                fp.setPetId(petId);
                fp.setAdjustCount(0);
            }
            fp.setPlanContent(objectMapper.writeValueAsString(plan));
            if (fp.getId() == null) feedingMapper.insertPlan(fp);
            else feedingMapper.updatePlan(fp);
        } catch (Exception e) {
            throw BizException.of(ErrorCode.SYSTEM_ERROR);
        }
        return plan;
    }

    @Override
    public Object queryFeedingPlan(Long petId) {
        Long uid = UserContext.getUserId();
        FeedingPlan fp = feedingMapper.selectPlanByPet(uid, petId);
        if (fp == null) return null;
        try {
            return objectMapper.readValue(fp.getPlanContent(), Map.class);
        } catch (Exception e) {
            return null;
        }
    }
}
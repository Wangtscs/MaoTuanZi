package com.maotuanzi.service.impl;

import com.maotuanzi.entity.SensitiveWord;
import com.maotuanzi.mapper.SensitiveWordMapper;
import com.maotuanzi.service.AuditService;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final SensitiveWordMapper mapper;
    private volatile java.util.Set<String> wordCache = new ConcurrentHashMap<String, Boolean>().keySet();

    @PostConstruct
    public void init() {
        reload();
    }

    public void reload() {
        List<SensitiveWord> list = mapper.selectAllActive();
        wordCache = list.stream()
                .filter(w -> w.getSeverity() != null && w.getSeverity() == 2)
                .map(SensitiveWord::getWord)
                .collect(Collectors.toUnmodifiableSet());
        log.info("敏感词加载完成 count={}", wordCache.size());
    }

    @Override
    public boolean checkText(String content) {
        if (content == null || content.isEmpty()) return true;
        for (String word : wordCache) {
            if (content.contains(word)) return false;
        }
        return true;
    }
}
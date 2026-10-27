package com.maotuanzi.ops;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maotuanzi.entity.SystemConfig;
import com.maotuanzi.mapper.OpsMapper;
import com.maotuanzi.security.AdminContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/** Config revisions reuse private system_configs rows; secret contents never enter history. */
@Component
@RequiredArgsConstructor
public class OpsConfigHistory {
    public static final String PREFIX = "ops.internal.";
    public static final String MASK = "********";
    private static final String HISTORY = PREFIX + "h.";
    private final OpsMapper mapper;
    private final ObjectMapper json;

    public static boolean sensitive(String key) {
        return key != null && key.toLowerCase(Locale.ROOT).matches(".*(password|passwd|secret|token|credential|api[._-]?key|private[._-]?key|webhook|authorization|[._-]url|[._-]dsn).*" );
    }

    private void validate(String key) {
        if (key == null || !key.startsWith("ops.") || key.startsWith(PREFIX) || key.length() > 128)
            throw new IllegalArgumentException("配置键必须为非内部 ops.* 配置");
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(String key, String value, String description, String operator) {
        validate(key);
        SystemConfig before = mapper.selectOpsConfigForUpdate(key);
        if (MASK.equals(value)) {
            if (!sensitive(key) || before == null) throw new IllegalArgumentException("掩码仅可用于保留已有敏感配置");
            value = before.getConfigValue();
        }
        if (before != null && Objects.equals(value, before.getConfigValue())
                && (description == null || Objects.equals(description, before.getDescription()))) return;
        revision(key, before, operator, "SAVE");
        if (mapper.upsertOpsConfig(key, value, description, operator) < 1)
            throw new IllegalStateException("配置保存失败");
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String key) {
        validate(key);
        SystemConfig before = mapper.selectOpsConfigForUpdate(key);
        if (before == null) throw new IllegalArgumentException("配置不存在");
        revision(key, before, operator(), "DELETE");
        if (mapper.deleteOpsConfig(key) != 1) throw new IllegalStateException("配置删除失败");
    }

    private void revision(String key, SystemConfig before, String operator, String action) {
        Map<String, Object> event = new LinkedHashMap<>();
        String id = UUID.randomUUID().toString();
        event.put("revision", id); event.put("key", key); event.put("action", action);
        event.put("operator", operator); event.put("timestamp", Instant.now().toString());
        event.put("existed", before != null); event.put("canRollback", !sensitive(key));
        event.put("previousValue", sensitive(key) ? MASK : before == null ? null : before.getConfigValue());
        event.put("previousDescription", before == null ? null : before.getDescription());
        try {
            if (mapper.upsertOpsConfig(HISTORY + id, json.writeValueAsString(event), "运维配置修订", operator) < 1)
                throw new IllegalStateException("配置历史保存失败");
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) { throw new IllegalStateException("配置历史序列化失败", e); }
    }

    public List<Map<String, Object>> history(String key) {
        if (key != null && !key.isBlank()) validate(key);
        List<Map<String, Object>> result = new ArrayList<>();
        List<SystemConfig> all = mapper.selectOpsConfigs();
        if (all == null) return result;
        for (SystemConfig c : all) {
            if (!c.getConfigKey().startsWith(HISTORY)) continue;
            try {
                Map<String, Object> event = json.readValue(c.getConfigValue(), new TypeReference<Map<String, Object>>() {});
                if (key == null || key.isBlank() || key.equals(event.get("key"))) result.add(event);
            } catch (Exception e) { throw new IllegalStateException("配置历史损坏，无法可靠读取", e); }
        }
        result.sort(Comparator.comparing(x -> String.valueOf(x.get("timestamp")), Comparator.reverseOrder()));
        return result.stream().limit(200).collect(java.util.stream.Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void rollback(String key, String revision) {
        validate(key);
        if (revision == null || !revision.matches("[a-fA-F0-9-]{36}")) throw new IllegalArgumentException("无效修订号");
        if (sensitive(key)) throw new IllegalArgumentException("敏感配置不保留明文历史，请重新输入原值");
        SystemConfig stored = mapper.selectOpsConfigForUpdate(HISTORY + revision);
        if (stored == null) throw new IllegalArgumentException("修订不存在");
        try {
            Map<String, Object> event = json.readValue(stored.getConfigValue(), new TypeReference<Map<String, Object>>() {});
            if (!key.equals(event.get("key")) || !Boolean.TRUE.equals(event.get("canRollback")))
                throw new IllegalArgumentException("修订不属于目标配置或不可回滚");
            SystemConfig before = mapper.selectOpsConfigForUpdate(key);
            revision(key, before, operator(), "ROLLBACK");
            if (Boolean.TRUE.equals(event.get("existed"))) {
                if (mapper.restoreOpsConfig(key, (String) event.get("previousValue"), (String) event.get("previousDescription"), operator()) < 1)
                    throw new IllegalStateException("配置回滚失败");
            } else if (before != null && mapper.deleteOpsConfig(key) != 1) throw new IllegalStateException("配置回滚失败");
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) { throw new IllegalStateException("配置历史损坏", e); }
    }

    private String operator() { return "admin#" + AdminContext.getAdminId(); }
}

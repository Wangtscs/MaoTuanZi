package com.maotuanzi.ops;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 系统 / JVM / 中间件资源指标采集
 */
@Slf4j
@Component
public class OpsSystemMetrics {

    private static final long MB = 1024L * 1024L;

    private final ObjectProvider<DataSource> dataSourceProvider;
    private final ObjectProvider<StringRedisTemplate> redisProvider;

    public OpsSystemMetrics(ObjectProvider<DataSource> dataSourceProvider,
                            ObjectProvider<StringRedisTemplate> redisProvider) {
        this.dataSourceProvider = dataSourceProvider;
        this.redisProvider = redisProvider;
    }

    // ==================== JVM ====================

    public Map<String, Object> jvm() {
        Map<String, Object> m = new LinkedHashMap<>();
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = mem.getHeapMemoryUsage();
        MemoryUsage nonHeap = mem.getNonHeapMemoryUsage();
        RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
        ThreadMXBean th = ManagementFactory.getThreadMXBean();
        ClassLoadingMXBean cl = ManagementFactory.getClassLoadingMXBean();

        long heapMax = heap.getMax() > 0 ? heap.getMax() : heap.getCommitted();
        m.put("heapUsedMb", heap.getUsed() / MB);
        m.put("heapCommittedMb", heap.getCommitted() / MB);
        m.put("heapMaxMb", heapMax / MB);
        m.put("heapUsedPct", heapMax <= 0 ? 0 : round2(heap.getUsed() * 100.0 / heapMax));
        m.put("nonHeapUsedMb", nonHeap.getUsed() / MB);
        m.put("threadCount", th.getThreadCount());
        m.put("threadPeak", th.getPeakThreadCount());
        m.put("threadDaemon", th.getDaemonThreadCount());
        m.put("threadTotalStarted", th.getTotalStartedThreadCount());
        m.put("deadlockCount", deadlockCount(th));
        m.put("loadedClasses", cl.getLoadedClassCount());
        m.put("unloadedClasses", cl.getUnloadedClassCount());
        m.put("uptimeMs", rt.getUptime());
        m.put("startTime", rt.getStartTime());
        m.put("jvmName", rt.getVmName());
        m.put("jvmVersion", rt.getVmVersion());
        m.put("javaVersion", System.getProperty("java.version"));
        m.put("pid", pid());

        long gcCount = 0;
        long gcTime = 0;
        List<Map<String, Object>> gcs = new ArrayList<>();
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            long c = Math.max(gc.getCollectionCount(), 0);
            long t = Math.max(gc.getCollectionTime(), 0);
            gcCount += c;
            gcTime += t;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", gc.getName());
            item.put("count", c);
            item.put("timeMs", t);
            gcs.add(item);
        }
        m.put("gcCount", gcCount);
        m.put("gcTimeMs", gcTime);
        m.put("gcDetail", gcs);

        List<Map<String, Object>> pools = new ArrayList<>();
        ManagementFactory.getMemoryPoolMXBeans().forEach(p -> {
            MemoryUsage u = p.getUsage();
            if (u == null) return;
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", p.getName());
            item.put("type", p.getType() == null ? null : p.getType().name());
            item.put("usedMb", u.getUsed() / MB);
            long max = u.getMax() > 0 ? u.getMax() : u.getCommitted();
            item.put("maxMb", max / MB);
            item.put("usedPct", max <= 0 ? 0 : round2(u.getUsed() * 100.0 / max));
            pools.add(item);
        });
        m.put("memoryPools", pools);
        return m;
    }

    private int deadlockCount(ThreadMXBean th) {
        try {
            long[] ids = th.findDeadlockedThreads();
            return ids == null ? 0 : ids.length;
        } catch (Throwable t) {
            return 0;
        }
    }

    private String pid() {
        try {
            String name = ManagementFactory.getRuntimeMXBean().getName();
            int idx = name.indexOf('@');
            return idx > 0 ? name.substring(0, idx) : name;
        } catch (Throwable t) {
            return "-";
        }
    }

    // ==================== 操作系统 ====================

    public Map<String, Object> os() {
        Map<String, Object> m = new LinkedHashMap<>();
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        m.put("name", os.getName());
        m.put("arch", os.getArch());
        m.put("version", os.getVersion());
        m.put("availableProcessors", os.getAvailableProcessors());
        double loadAvg = os.getSystemLoadAverage();
        m.put("loadAverage", loadAvg < 0 ? null : round2(loadAvg));

        double processCpu = -1;
        double systemCpu = -1;
        long totalMem = -1;
        long freeMem = -1;
        try {
            com.sun.management.OperatingSystemMXBean sun = (com.sun.management.OperatingSystemMXBean) os;
            processCpu = sun.getProcessCpuLoad();
            systemCpu = sun.getSystemCpuLoad();
            totalMem = sun.getTotalPhysicalMemorySize();
            freeMem = sun.getFreePhysicalMemorySize();
        } catch (Throwable ignored) {
            // 非 HotSpot 环境降级
        }
        m.put("processCpuLoad", processCpu < 0 ? null : round4(processCpu));
        m.put("systemCpuLoad", systemCpu < 0 ? null : round4(systemCpu));
        m.put("memTotalMb", totalMem < 0 ? null : totalMem / MB);
        m.put("memFreeMb", freeMem < 0 ? null : freeMem / MB);
        m.put("memUsedPct", (totalMem <= 0 || freeMem < 0) ? null
                : round2((totalMem - freeMem) * 100.0 / totalMem));
        try {
            m.put("hostname", InetAddress.getLocalHost().getHostName());
            m.put("ip", InetAddress.getLocalHost().getHostAddress());
        } catch (Throwable t) {
            m.put("hostname", "unknown");
            m.put("ip", "127.0.0.1");
        }
        return m;
    }

    // ==================== 磁盘 ====================

    public Map<String, Object> disk() {
        Map<String, Object> m = new LinkedHashMap<>();
        List<Map<String, Object>> partitions = new ArrayList<>();
        long total = 0;
        long free = 0;
        File[] roots = File.listRoots();
        if (roots != null) {
            for (File f : roots) {
                long t = f.getTotalSpace();
                if (t <= 0) continue;
                long u = f.getUsableSpace();
                total += t;
                free += u;
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("path", f.getAbsolutePath());
                item.put("totalGb", round2(t / 1024.0 / 1024 / 1024));
                item.put("freeGb", round2(u / 1024.0 / 1024 / 1024));
                item.put("usedPct", round2((t - u) * 100.0 / t));
                item.put("freePct", round2(u * 100.0 / t));
                partitions.add(item);
            }
        }
        m.put("partitions", partitions);
        m.put("totalGb", round2(total / 1024.0 / 1024 / 1024));
        m.put("freeGb", round2(free / 1024.0 / 1024 / 1024));
        m.put("freePct", total <= 0 ? 100 : round2(free * 100.0 / total));
        m.put("usedPct", total <= 0 ? 0 : round2((total - free) * 100.0 / total));
        return m;
    }

    // ==================== 数据库连接池 ====================

    public Map<String, Object> dbPool() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("available", false);
        try {
            DataSource ds = dataSourceProvider.getIfAvailable();
            if (ds == null) return m;
            m.put("type", ds.getClass().getSimpleName());
            if (ds instanceof HikariDataSource) {
                HikariDataSource hikari = (HikariDataSource) ds;
                m.put("jdbcUrl", mask(hikari.getJdbcUrl()));
                m.put("poolName", hikari.getPoolName());
                m.put("maximumPoolSize", hikari.getMaximumPoolSize());
                m.put("minimumIdle", hikari.getMinimumIdle());
                m.put("connectionTimeoutMs", hikari.getConnectionTimeout());
                m.put("idleTimeoutMs", hikari.getIdleTimeout());
                m.put("maxLifetimeMs", hikari.getMaxLifetime());
                HikariPoolMXBean bean = hikari.getHikariPoolMXBean();
                if (bean != null) {
                    int active = bean.getActiveConnections();
                    int idle = bean.getIdleConnections();
                    int totalConn = bean.getTotalConnections();
                    m.put("activeConnections", active);
                    m.put("idleConnections", idle);
                    m.put("totalConnections", totalConn);
                    m.put("threadsAwaitingConnection", bean.getThreadsAwaitingConnection());
                    m.put("usedPct", hikari.getMaximumPoolSize() <= 0 ? 0
                            : round2(active * 100.0 / hikari.getMaximumPoolSize()));
                }
                m.put("available", true);
            }
        } catch (Throwable t) {
            m.put("error", t.getMessage());
        }
        return m;
    }

    /** 数据库连通性与版本 */
    public Map<String, Object> dbPing() {
        Map<String, Object> m = new LinkedHashMap<>();
        long start = System.currentTimeMillis();
        try {
            DataSource ds = dataSourceProvider.getIfAvailable();
            if (ds == null) {
                m.put("ok", false);
                m.put("error", "DataSource 未初始化");
                return m;
            }
            try (java.sql.Connection conn = ds.getConnection()) {
                java.sql.DatabaseMetaData meta = conn.getMetaData();
                m.put("productName", meta.getDatabaseProductName());
                m.put("productVersion", meta.getDatabaseProductVersion());
                m.put("driverName", meta.getDriverName());
                m.put("driverVersion", meta.getDriverVersion());
                m.put("catalog", conn.getCatalog());
                try (java.sql.Statement st = conn.createStatement();
                     java.sql.ResultSet rs = st.executeQuery("SELECT 1")) {
                    m.put("ok", rs.next());
                }
            }
        } catch (Throwable t) {
            m.put("ok", false);
            m.put("error", t.getMessage());
        }
        m.put("latencyMs", System.currentTimeMillis() - start);
        return m;
    }

    // ==================== Redis ====================

    public Map<String, Object> redis() {
        Map<String, Object> m = new LinkedHashMap<>();
        long start = System.currentTimeMillis();
        StringRedisTemplate redis = redisProvider.getIfAvailable();
        if (redis == null) {
            m.put("ok", false);
            m.put("error", "RedisTemplate 未初始化");
            m.put("latencyMs", 0);
            return m;
        }
        RedisConnection conn = null;
        try {
            conn = redis.getConnectionFactory() == null ? null : redis.getConnectionFactory().getConnection();
            if (conn == null) {
                m.put("ok", false);
                m.put("error", "无法获取 Redis 连接");
                m.put("latencyMs", System.currentTimeMillis() - start);
                return m;
            }
            String pong = conn.ping();
            m.put("ok", pong != null);
            m.put("pong", pong);
            try {
                Properties info = conn.info();
                if (info != null) {
                    m.put("version", info.getProperty("redis_version"));
                    m.put("mode", info.getProperty("redis_mode"));
                    m.put("uptimeSeconds", parseLong(info.getProperty("uptime_in_seconds")));
                    m.put("connectedClients", parseLong(info.getProperty("connected_clients")));
                    m.put("usedMemoryMb", round2(parseLong(info.getProperty("used_memory")) / 1024.0 / 1024));
                    m.put("usedMemoryPeakMb", round2(parseLong(info.getProperty("used_memory_peak")) / 1024.0 / 1024));
                    m.put("memFragmentationRatio", info.getProperty("mem_fragmentation_ratio"));
                    long hits = parseLong(info.getProperty("keyspace_hits"));
                    long misses = parseLong(info.getProperty("keyspace_misses"));
                    m.put("keyspaceHits", hits);
                    m.put("keyspaceMisses", misses);
                    m.put("hitRate", (hits + misses) == 0 ? 0 : round2(hits * 100.0 / (hits + misses)));
                    m.put("totalCommandsProcessed", parseLong(info.getProperty("total_commands_processed")));
                    m.put("instantaneousOpsPerSec", parseLong(info.getProperty("instantaneous_ops_per_sec")));
                    m.put("evictedKeys", parseLong(info.getProperty("evicted_keys")));
                    m.put("expiredKeys", parseLong(info.getProperty("expired_keys")));
                }
                Long dbSize = conn.dbSize();
                m.put("dbSize", dbSize);
            } catch (Throwable ignored) {
                // INFO 无权限时降级
            }
        } catch (Throwable t) {
            m.put("ok", false);
            m.put("error", t.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Throwable ignored) {
                    // ignore
                }
            }
        }
        m.put("latencyMs", System.currentTimeMillis() - start);
        return m;
    }

    /** Redis 键空间抽样（按业务前缀统计） */
    public List<Map<String, Object>> redisKeyGroups(String[] prefixes) {
        List<Map<String, Object>> list = new ArrayList<>();
        StringRedisTemplate redis = redisProvider.getIfAvailable();
        if (redis == null) return list;
        for (String p : prefixes) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("prefix", p);
            try {
                java.util.Set<String> keys = redis.keys(p + "*");
                item.put("count", keys == null ? 0 : keys.size());
            } catch (Throwable t) {
                item.put("count", 0);
                item.put("error", t.getMessage());
            }
            list.add(item);
        }
        return list;
    }

    // ==================== 工具 ====================

    private static long parseLong(String s) {
        try {
            return s == null ? 0 : Long.parseLong(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static String mask(String url) {
        if (url == null) return null;
        return url.replaceAll("(password=)[^&]*", "$1***");
    }

    static double round2(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return 0;
        return Math.round(v * 100.0) / 100.0;
    }

    static double round4(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return 0;
        return Math.round(v * 10000.0) / 10000.0;
    }
}

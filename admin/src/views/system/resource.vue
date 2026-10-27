<template>
<div class="page-pad">
<div class="bar"><div><div class="section-title">系统资源</div><p class="intro">JVM、主机与连接池实际采集值；— 表示未取得样本。</p></div><el-button :loading="loading" @click="refresh">刷新</el-button></div><PageState :loading="loading" :error="error" :stale="stale" :updated-at="updatedAt" @retry="refresh" />
<div class="cards"><StatCard label="JVM 堆使用率" :value="display(d?.jvm?.heapUsedPct)" unit="%" /><StatCard label="进程 CPU" :value="display(d?.os?.processCpuLoad == null ? null : d.os.processCpuLoad*100)" unit="%" /><StatCard label="物理内存使用率" :value="display(d?.os?.memUsedPct)" unit="%" /><StatCard label="磁盘使用率" :value="display(d?.disk?.usedPct)" unit="%" /></div>
<div class="split"><el-card class="panel" v-for="section in sections" :key="section.key"><div class="section-title">{{section.name}}</div><el-descriptions :column="1" border><el-descriptions-item v-for="field in section.fields" :key="field.key" :label="field.label">{{display(d?.[section.key]?.[field.key],field.unit||'')}}</el-descriptions-item></el-descriptions></el-card></div>
<el-card class="panel"><div class="section-title">磁盘分区</div><OpsTable :rows="d?.disk?.partitions??[]" :columns="[{key:'path',label:'路径'},{key:'totalGb',label:'总容量 GB'},{key:'freeGb',label:'可用 GB'},{key:'usedPct',label:'使用率 %'}]" /></el-card>

</div>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'
import * as ops from '@/api/ops'
import PageState from '@/components/PageState.vue'
import OpsTable from '@/components/OpsTable.vue'
import StatCard from '@/components/StatCard.vue'
import OpsStatus from '@/components/OpsStatus.vue'
import { usePolling } from '@/composables/usePolling'
import { display, bytes } from '@/utils/format'
import { exportCsv } from '@/utils/export'
const d=ref<any>(null)
const sections=[{key:'jvm',name:'JVM 内存与线程',fields:[{key:'heapUsedMb',label:'堆已用',unit:' MB'},{key:'heapMaxMb',label:'堆上限',unit:' MB'},{key:'nonHeapUsedMb',label:'非堆已用',unit:' MB'},{key:'threadCount',label:'线程数'},{key:'deadlockCount',label:'死锁数'},{key:'gcCount',label:'GC 次数'},{key:'gcTimeMs',label:'GC 耗时',unit:' ms'},{key:'javaVersion',label:'Java 版本'}]},{key:'dbPool',name:'数据库连接池',fields:[{key:'type',label:'类型'},{key:'usedPct',label:'使用率',unit:'%'},{key:'activeConnections',label:'活跃连接'},{key:'idleConnections',label:'空闲连接'},{key:'threadsAwaitingConnection',label:'等待线程'},{key:'maximumPoolSize',label:'连接上限'}]},{key:'os',name:'主机',fields:[{key:'name',label:'系统'},{key:'arch',label:'架构'},{key:'availableProcessors',label:'CPU 核数'},{key:'hostname',label:'主机名'},{key:'memTotalMb',label:'内存总量',unit:' MB'}]},{key:'app',name:'运行实例',fields:[{key:'appName',label:'应用'},{key:'profile',label:'环境'},{key:'version',label:'版本'},{key:'instanceId',label:'实例 ID'},{key:'startedAt',label:'启动时间'}]}]
const {loading,error,stale,updatedAt,refresh}=usePolling(async()=>{d.value=await ops.systemResource()},{interval:15000})

</script>
<style scoped>.bar{display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:18px}.filters{display:flex;gap:10px;flex-wrap:wrap;align-items:center;margin-bottom:16px}.filters .el-input,.filters .el-select{width:200px}.cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(175px,1fr));gap:14px;margin:14px 0}.split{display:grid;grid-template-columns:repeat(auto-fit,minmax(min(100%,350px),1fr));gap:16px}.panel{margin-bottom:16px}.section-title{margin-bottom:12px}.intro{color:var(--ops-muted);margin:6px 0 0;font-size:13px}pre{white-space:pre-wrap;word-break:break-word}.chart{height:320px}.pager{margin-top:18px;overflow:auto}</style>
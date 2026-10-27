<template>
<div class="page-pad">
<div class="bar"><div><div class="section-title">数据库</div><p class="intro">数据库探测、连接池与 information_schema 容量统计。</p></div><el-button :loading="loading" @click="refresh">刷新</el-button></div><PageState :loading="loading" :error="error" :stale="stale" :updated-at="updatedAt" @retry="refresh" />
<el-alert v-if="d?.ping?.error" :title="d.ping.error" type="error" :closable="false" />
<div class="cards"><StatCard label="连通性" :value="d?.ping?.ok===true?'正常':d?.ping?.ok===false?'异常':'未知'" /><StatCard label="版本" :value="display(d?.ping?.productVersion)" /><StatCard label="探测延迟" :value="display(d?.ping?.latencyMs)" unit="ms" /><StatCard label="连接池使用率" :value="display(d?.pool?.usedPct)" unit="%" /></div>
<el-card class="panel"><div class="bar"><div><div class="section-title">{{display(d?.schema)}} · {{tables.length}} 张表</div><span class="muted">{{display(d?.pool?.jdbcUrl)}}</span></div><el-button @click="exportCsv('数据库容量',tables)">导出容量</el-button></div><OpsTable :rows="tables" :columns="columns" /></el-card>

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
const d=ref<any>(null),tables=ref<any[]>([])
const columns=[{key:'tableName',label:'表名',width:240},{key:'tableRows',label:'估算行数',sortable:true},{key:'dataLength',label:'数据大小',format:bytes},{key:'indexLength',label:'索引大小',format:bytes},{key:'totalMb',label:'总大小 MB',sortable:true},{key:'freeMb',label:'可回收 MB'},{key:'engine',label:'引擎'},{key:'collation',label:'字符排序'}]
const {loading,error,stale,updatedAt,refresh}=usePolling(async()=>{const [status,list]=await Promise.all([ops.dbStatus(),ops.dbTables()]);d.value=status;tables.value=list},{interval:30000})

</script>
<style scoped>.bar{display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:18px}.filters{display:flex;gap:10px;flex-wrap:wrap;align-items:center;margin-bottom:16px}.filters .el-input,.filters .el-select{width:200px}.cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(175px,1fr));gap:14px;margin:14px 0}.split{display:grid;grid-template-columns:repeat(auto-fit,minmax(min(100%,350px),1fr));gap:16px}.panel{margin-bottom:16px}.section-title{margin-bottom:12px}.intro{color:var(--ops-muted);margin:6px 0 0;font-size:13px}pre{white-space:pre-wrap;word-break:break-word}.chart{height:320px}.pager{margin-top:18px;overflow:auto}</style>
<template>
<div class="page-pad">
<div class="bar"><div><div class="section-title">Redis</div><p class="intro">连接与缓存命中情况；Key 分组计数以当前实例扫描结果为准。</p></div><el-button :loading="loading" @click="refresh">刷新</el-button></div><PageState :loading="loading" :error="error" :stale="stale" :updated-at="updatedAt" @retry="refresh" />
<el-alert v-if="d?.info?.error" :title="d.info.error" type="error" :closable="false" />
<div class="cards"><StatCard label="连通性" :value="d?.info?.ok===true?'正常':d?.info?.ok===false?'异常':'未知'" /><StatCard v-for="m in metrics" :key="m.key" :label="m.label" :value="display(d?.info?.[m.key])" :unit="m.unit" /></div>
<el-card class="panel"><div class="bar"><div class="section-title">Key 分组</div><el-button @click="exportCsv('Redis分组',d?.keyGroups??[])">导出分组</el-button></div><OpsTable :rows="d?.keyGroups??[]" :columns="[{key:'prefix',label:'前缀',width:260},{key:'count',label:'Key 数量',sortable:true},{key:'error',label:'采集异常',width:300}]" /></el-card>

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
const metrics=[{key:'version',label:'版本',unit:''},{key:'usedMemoryMb',label:'已用内存',unit:'MB'},{key:'connectedClients',label:'客户端连接',unit:''},{key:'hitRate',label:'缓存命中率',unit:'%'},{key:'instantaneousOpsPerSec',label:'每秒操作数',unit:''},{key:'dbSize',label:'Key 总数',unit:''},{key:'usedMemoryPeakMb',label:'内存峰值',unit:'MB'}]
const {loading,error,stale,updatedAt,refresh}=usePolling(async()=>{d.value=await ops.redisStatus()},{interval:30000})

</script>
<style scoped>.bar{display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:18px}.filters{display:flex;gap:10px;flex-wrap:wrap;align-items:center;margin-bottom:16px}.filters .el-input,.filters .el-select{width:200px}.cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(175px,1fr));gap:14px;margin:14px 0}.split{display:grid;grid-template-columns:repeat(auto-fit,minmax(min(100%,350px),1fr));gap:16px}.panel{margin-bottom:16px}.section-title{margin-bottom:12px}.intro{color:var(--ops-muted);margin:6px 0 0;font-size:13px}pre{white-space:pre-wrap;word-break:break-word}.chart{height:320px}.pager{margin-top:18px;overflow:auto}</style>
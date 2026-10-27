<template>
<div class="page-pad">
<div class="bar"><div><div class="section-title">运行实例</div><p class="intro">在线判定采用服务端心跳状态，当前实例单独标识。</p></div><el-button :loading="loading" @click="refresh">刷新</el-button></div><PageState :loading="loading" :error="error" :stale="stale" :updated-at="updatedAt" @retry="refresh" />
<div class="filters"><el-input v-model="keyword" placeholder="搜索实例 / 主机 / 环境" clearable aria-label="搜索实例" /><el-select v-model="status" clearable placeholder="全部状态" aria-label="实例状态"><el-option value="UP" label="在线"/><el-option value="DOWN" label="离线"/></el-select><el-button @click="exportCsv('运行实例',rows)">导出当前列表</el-button></div>
<el-card class="panel"><OpsTable :rows="rows" :columns="columns" /></el-card>

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
const list=ref<any[]>([]),keyword=ref(''),status=ref('')
const rows=computed(()=>list.value.filter(r=>(!status.value||r.status===status.value)&&(!keyword.value||[r.instanceId,r.host,r.profile,r.ip].join(' ').toLowerCase().includes(keyword.value.toLowerCase()))))
const columns=[{key:'instanceId',label:'实例',width:240},{key:'self',label:'当前实例',format:(v:any)=>v?'当前':'—'},{key:'appName',label:'应用'},{key:'profile',label:'环境'},{key:'host',label:'主机'},{key:'ip',label:'IP'},{key:'port',label:'端口'},{key:'version',label:'版本'},{key:'status',label:'状态',status:true},{key:'lastHeartbeatAt',label:'最近心跳',width:180},{key:'heapUsedPct',label:'堆使用率 %'},{key:'cpuLoad',label:'CPU 负载'},{key:'qps',label:'QPS'}]
const {loading,error,stale,updatedAt,refresh}=usePolling(async()=>{list.value=await ops.instances()},{interval:15000})

</script>
<style scoped>.bar{display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:18px}.filters{display:flex;gap:10px;flex-wrap:wrap;align-items:center;margin-bottom:16px}.filters .el-input,.filters .el-select{width:200px}.cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(175px,1fr));gap:14px;margin:14px 0}.split{display:grid;grid-template-columns:repeat(auto-fit,minmax(min(100%,350px),1fr));gap:16px}.panel{margin-bottom:16px}.section-title{margin-bottom:12px}.intro{color:var(--ops-muted);margin:6px 0 0;font-size:13px}pre{white-space:pre-wrap;word-break:break-word}.chart{height:320px}.pager{margin-top:18px;overflow:auto}</style>
<template>
<div class="page-pad">
<div class="bar"><div><div class="section-title">线程快照</div><p class="intro">按状态和线程名 / 堆栈定位阻塞；详情可复制完整堆栈。</p></div><el-button :loading="loading" @click="refresh">刷新</el-button></div><PageState :loading="loading" :error="error" :stale="stale" :updated-at="updatedAt" @retry="refresh" />
<div class="filters"><el-input v-model="keyword" clearable placeholder="线程名或堆栈" aria-label="线程名或堆栈" /><el-select v-model="state" clearable placeholder="所有状态" aria-label="线程状态"><el-option v-for="s in ['RUNNABLE','BLOCKED','WAITING','TIMED_WAITING','NEW','TERMINATED']" :key="s" :value="s" :label="s" /></el-select><el-button @click="exportCsv('线程快照',rows)">导出当前快照</el-button></div>
<el-alert v-if="deadlocks.length" type="error" :closable="false" :title="'发现死锁线程：'+deadlocks.join(', ')" />
<el-card class="panel"><div class="muted">采集 {{display(total)}} 个线程，当前匹配 {{rows.length}} 个（单次最多 500）</div><OpsTable :rows="rows" :columns="columns" /></el-card>

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
const all=ref<any[]>([]),total=ref<number|null>(null),deadlocks=ref<number[]>([]),keyword=ref(''),state=ref('')
const rows=computed(()=>all.value.filter(t=>(!state.value||t.state===state.value)&&(!keyword.value||(t.name+' '+t.stack).toLowerCase().includes(keyword.value.toLowerCase()))))
const columns=[{key:'id',label:'线程 ID'},{key:'name',label:'线程名',width:250},{key:'state',label:'状态',status:true},{key:'blockedCount',label:'阻塞次数'},{key:'blockedTimeMs',label:'阻塞时间 ms'},{key:'waitedCount',label:'等待次数'},{key:'lockOwnerName',label:'锁持有线程'},{key:'stack',label:'堆栈',width:300}]
const {loading,error,stale,updatedAt,refresh}=usePolling(async()=>{const result=await ops.threadDump();all.value=result.list??[];total.value=result.total;deadlocks.value=result.deadlocks??[]},{interval:0})

</script>
<style scoped>.bar{display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:18px}.filters{display:flex;gap:10px;flex-wrap:wrap;align-items:center;margin-bottom:16px}.filters .el-input,.filters .el-select{width:200px}.cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(175px,1fr));gap:14px;margin:14px 0}.split{display:grid;grid-template-columns:repeat(auto-fit,minmax(min(100%,350px),1fr));gap:16px}.panel{margin-bottom:16px}.section-title{margin-bottom:12px}.intro{color:var(--ops-muted);margin:6px 0 0;font-size:13px}pre{white-space:pre-wrap;word-break:break-word}.chart{height:320px}.pager{margin-top:18px;overflow:auto}</style>
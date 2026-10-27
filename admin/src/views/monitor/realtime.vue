<template>
<div class="page-pad">
<div class="bar"><div><div class="section-title">实时监控</div><p class="intro">当前实例指标与最近 60 秒 HTTP 请求，自动刷新 5 秒。</p></div><el-button :loading="loading" @click="refresh">刷新</el-button></div><PageState :loading="loading" :error="error" :stale="stale" :updated-at="updatedAt" @retry="refresh" />
<el-switch v-model="auto" active-text="自动刷新" />
<div class="cards"><StatCard v-for="m in catalog" :key="m.key" :label="m.name" :value="display(values[m.key])" :unit="values[m.key] == null ? '' : m.unit" /></div>
<el-card class="panel"><div class="section-title">HTTP 请求 / 秒</div><div ref="chartEl" class="chart" /></el-card>
<el-card class="panel"><div class="bar"><div class="section-title">接口排行</div><el-button @click="exportCsv('接口排行',endpoints)">导出当前排行</el-button></div><OpsTable :rows="endpoints" :columns="endpointColumns" /></el-card>

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
import { useChart } from '@/utils/echarts'
const auto=ref(true), catalog=ref<any[]>([]), values=ref<any>({}), endpoints=ref<any[]>([])
const chartEl=ref<HTMLElement|null>(null), chartOption=ref<any>({})
useChart(chartEl,chartOption)
const endpointColumns=[{key:'endpoint',label:'接口',width:300},{key:'count',label:'请求数',sortable:true},{key:'errorRate',label:'错误率 %'},{key:'avgMs',label:'平均耗时 ms'},{key:'maxMs',label:'最长耗时 ms'}]
const { loading,error,stale,updatedAt,refresh }=usePolling(async()=>{
 const [c,r,t,e]=await Promise.all([ops.metricCatalog(),ops.realtime(),ops.httpTimeline(60),ops.httpEndpoints()])
 catalog.value=c;values.value=r.values??{};endpoints.value=e.list??[]
 const timeline=t.timeline??[]
 chartOption.value={tooltip:{trigger:'axis'},legend:{data:['请求数','错误数']},xAxis:{type:'category',data:timeline.map((p:any)=>new Date(p.time).toLocaleTimeString())},yAxis:{type:'value',minInterval:1},series:[{name:'请求数',type:'line',data:timeline.map((p:any)=>p.count)},{name:'错误数',type:'line',data:timeline.map((p:any)=>p.errors)}]}
},{interval:5000,enabled:auto})

</script>
<style scoped>.bar{display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:18px}.filters{display:flex;gap:10px;flex-wrap:wrap;align-items:center;margin-bottom:16px}.filters .el-input,.filters .el-select{width:200px}.cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(175px,1fr));gap:14px;margin:14px 0}.split{display:grid;grid-template-columns:repeat(auto-fit,minmax(min(100%,350px),1fr));gap:16px}.panel{margin-bottom:16px}.section-title{margin-bottom:12px}.intro{color:var(--ops-muted);margin:6px 0 0;font-size:13px}pre{white-space:pre-wrap;word-break:break-word}.chart{height:320px}.pager{margin-top:18px;overflow:auto}</style>
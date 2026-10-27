<template>
<div class="page-pad">
<div class="bar"><div><div class="section-title">指标趋势</div><p class="intro">历史采样来自指标快照任务；没有样本时不会显示为零。</p></div><el-button :loading="loading" @click="refresh">刷新</el-button></div><PageState :loading="loading" :error="error" :stale="stale" :updated-at="updatedAt" @retry="refresh" />
<el-card class="panel"><div class="filters"><el-select v-model="selected" filterable aria-label="指标"><el-option v-for="m in catalog" :key="m.key" :label="m.name+' ('+m.unit+')'" :value="m.key" /></el-select><el-select v-model="minutes" aria-label="趋势时间范围"><el-option v-for="m in [60,360,1440,10080]" :key="m" :label="m<1440 ? '近 '+m/60+' 小时' : '近 '+m/1440+' 天'" :value="m" /></el-select><el-button type="primary" :loading="loading" @click="refresh">查询</el-button><el-button :disabled="pending || user.isReadOnly || !user.hasPerm('ops.metrics')" @click="onReset">重置 HTTP 缓存</el-button><el-button :disabled="!points.length" @click="exportCsv('指标样本',points)">导出样本</el-button></div><el-empty v-if="updatedAt && !points.length" description="此范围暂无历史采样，请检查指标快照任务" /><div v-show="points.length" ref="chartEl" class="chart" /></el-card>

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
import { useUserStore } from '@/stores/user'
import { useMutation, confirmAction } from '@/composables/useMutation'
const user=useUserStore(), {pending,mutate}=useMutation()
const catalog=ref<any[]>([]),selected=ref(''),minutes=ref(60),points=ref<any[]>([])
const chartEl=ref<HTMLElement|null>(null),chartOption=ref<any>({})
useChart(chartEl,chartOption)
const {loading,error,stale,updatedAt,refresh}=usePolling(async()=>{
 if(!catalog.value.length){catalog.value=await ops.metricCatalog();selected.value ||= catalog.value.find((c:any)=>c.snapshot)?.key || catalog.value[0]?.key || ''}
 if(!selected.value)return
 const result=await ops.metricSeries(selected.value,minutes.value)
 points.value=result.series?.find((s:any)=>s.key===selected.value)?.points??[]
 chartOption.value={tooltip:{trigger:'axis'},xAxis:{type:'category',data:points.value.map(p=>String(p.sampleTime).replace('T',' '))},yAxis:{type:'value',scale:true},series:[{type:'line',connectNulls:false,data:points.value.map(p=>p.metricValue??null)}]}
},{interval:0})
async function onReset(){if(user.isReadOnly||!user.hasPerm('ops.metrics')||!await confirmAction('重置当前实例的 HTTP 内存统计？历史快照保留。'))return;await mutate(()=>ops.resetMetrics(),'HTTP 指标缓存已重置');await refresh()}

</script>
<style scoped>.bar{display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:18px}.filters{display:flex;gap:10px;flex-wrap:wrap;align-items:center;margin-bottom:16px}.filters .el-input,.filters .el-select{width:200px}.cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(175px,1fr));gap:14px;margin:14px 0}.split{display:grid;grid-template-columns:repeat(auto-fit,minmax(min(100%,350px),1fr));gap:16px}.panel{margin-bottom:16px}.section-title{margin-bottom:12px}.intro{color:var(--ops-muted);margin:6px 0 0;font-size:13px}pre{white-space:pre-wrap;word-break:break-word}.chart{height:320px}.pager{margin-top:18px;overflow:auto}</style>
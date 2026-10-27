<template>
<div class="page-pad">
<div class="bar"><div><div class="section-title">存储概览</div><p class="intro">上传量、文件类型与容量趋势；统计范围可切换。</p></div><el-button :loading="loading" @click="refresh">刷新</el-button></div><PageState :loading="loading" :error="error" :stale="stale" :updated-at="updatedAt" @retry="refresh" />
<div class="filters"><el-select v-model="days" aria-label="存储统计范围" @change="refresh"><el-option v-for="n in [7,14,30,90]" :key="n" :label="'近 '+n+' 天'" :value="n" /></el-select><el-button @click="exportCsv('上传趋势',d?.daily??[])">导出趋势</el-button></div>
<el-alert v-if="d?.error" :title="d.error" type="error" :closable="false" />
<div class="cards"><StatCard label="文件总数" :value="display(d?.fileTotal)" /><StatCard label="总容量" :value="display(d?.sizeTotalMb)" unit="MB" /><StatCard :label="'近 '+days+' 天上传数'" :value="display(d?.fileRecent)" /><StatCard label="本期新增容量" :value="display(d?.sizeRecentMb)" unit="MB" /></div>
<div class="split"><el-card class="panel"><div class="section-title">业务类型分布</div><OpsTable :rows="d?.byType??[]" :columns="[{key:'bizType',label:'业务类型'},{key:'cnt',label:'文件数'},{key:'sizeBytes',label:'大小',format:bytes}]" /></el-card><el-card class="panel"><div class="section-title">上传用户排行</div><OpsTable :rows="d?.topUploaders??[]" :columns="[{key:'userId',label:'用户 ID'},{key:'cnt',label:'上传数'},{key:'sizeBytes',label:'大小',format:bytes}]" /></el-card></div>
<el-card class="panel"><div class="section-title">近 {{days}} 天上传趋势</div><el-empty v-if="updatedAt&&!d?.daily?.length" description="本期暂无上传记录"/><div v-show="d?.daily?.length" ref="chartEl" class="chart" /></el-card>

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
const d=ref<any>(null),days=ref(14),chartEl=ref<HTMLElement|null>(null),chartOption=ref<any>({})
useChart(chartEl,chartOption)
const {loading,error,stale,updatedAt,refresh}=usePolling(async()=>{const r=await ops.storageOverview(days.value);d.value=r;if(r.error)throw new Error(r.error);const daily=r.daily??[];chartOption.value={tooltip:{trigger:'axis'},xAxis:{type:'category',data:daily.map((p:any)=>p.day)},yAxis:{type:'value',minInterval:1},series:[{name:'文件数',type:'bar',data:daily.map((p:any)=>p.cnt)}]}},{interval:0})

</script>
<style scoped>.bar{display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:18px}.filters{display:flex;gap:10px;flex-wrap:wrap;align-items:center;margin-bottom:16px}.filters .el-input,.filters .el-select{width:200px}.cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(175px,1fr));gap:14px;margin:14px 0}.split{display:grid;grid-template-columns:repeat(auto-fit,minmax(min(100%,350px),1fr));gap:16px}.panel{margin-bottom:16px}.section-title{margin-bottom:12px}.intro{color:var(--ops-muted);margin:6px 0 0;font-size:13px}pre{white-space:pre-wrap;word-break:break-word}.chart{height:320px}.pager{margin-top:18px;overflow:auto}</style>
<template>
<div class="page-pad">
<div class="bar"><div><div class="section-title">内容安全审计</div><p class="intro">保留查询条件，支持分页、详情与当前页导出。</p></div><el-button :disabled="!rows.length" @click="exportCsv('内容安全审计',rows)">导出当前页</el-button></div><PageState :loading="loading" :error="error" :stale="stale" :updated-at="updatedAt" @retry="refresh" />
<div class="filters"><el-select v-model="filters.checkResult" clearable placeholder="审核结果" aria-label="审核结果"><el-option label="通过" value="pass"/><el-option label="拦截" value="block"/><el-option label="复核" value="report"/></el-select><el-input v-model="filters.contentType" clearable placeholder="内容类型" aria-label="内容类型"/><el-input v-model="filters.keyword" placeholder="原因 / 内容 ID / 操作人" aria-label="审计关键词" clearable/><el-date-picker v-model="range" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss" start-placeholder="开始时间" end-placeholder="结束时间" aria-label="查询时间范围" style="max-width:100%"/><el-button type="primary" :loading="loading" @click="search">查询</el-button><el-button @click="reset">重置条件</el-button></div>
<el-card class="panel"><OpsTable :rows="rows" :columns="columns" :action-width="90"></OpsTable><el-pagination class="pager" v-model:current-page="filters.page" v-model:page-size="filters.pageSize" :page-sizes="[20,50,100]" :total="total" layout="total, sizes, prev, pager, next" @current-change="refresh" @size-change="search" /></el-card>
</div>
</template>
<script setup lang="ts">
import { computed, ref } from 'vue'
import * as ops from '@/api/ops'
import PageState from '@/components/PageState.vue'
import OpsTable from '@/components/OpsTable.vue'
import OpsStatus from '@/components/OpsStatus.vue'
import StatCard from '@/components/StatCard.vue'
import { usePolling } from '@/composables/usePolling'
import { useFilters } from '@/composables/useFilters'
import { useMutation, confirmAction } from '@/composables/useMutation'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { display } from '@/utils/format'
import { exportCsv } from '@/utils/export'
const user=useUserStore(),{pending,mutate}=useMutation()
const canWrite=computed(()=>!user.isReadOnly&&user.hasPerm('ops.alarm'))
const rows=ref<any[]>([]),total=ref(0),range=ref<string[]>([])
const defaults={checkResult:'',contentType:'',keyword:'',from:'',to:'',page:1,pageSize:20}
const {filters,persist}=useFilters(defaults)
if('from' in filters && filters.from && filters.to)range.value=[String(filters.from),String(filters.to)]
const columns=[{key:'contentType',label:'内容类型'},{key:'contentId',label:'内容 ID'},{key:'userId',label:'用户 ID'},{key:'checkType',label:'审核方式'},{key:'checkResult',label:'审核结果',status:true},{key:'reason',label:'原因',width:250},{key:'operator',label:'审核人'},{key:'createdAt',label:'审核时间',width:180}]
const {loading,error,stale,updatedAt,refresh}=usePolling(async()=>{
 await persist()
 const query:any={...filters}
 if(query.jobId){if(!/^\d+$/.test(query.jobId))throw new Error('任务 ID 必须为正整数');query.jobId=Number(query.jobId)}else delete query.jobId
 const r=await ops.auditContentLogs(query)
 rows.value=r.list??[];total.value=r.pagination?.total??0
},{interval:0})
async function search(){filters.page=1;Object.assign(filters,'from' in filters ? {from:range.value?.[0]??'',to:range.value?.[1]??''}:{});await refresh()}
async function reset(){Object.assign(filters,defaults);range.value=[];await refresh()}

</script>
<style scoped>.bar{display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:18px}.filters{display:flex;gap:10px;flex-wrap:wrap;align-items:center;margin-bottom:16px}.filters .el-input,.filters .el-select{width:190px}.panel{margin-bottom:16px}.intro{color:var(--ops-muted);font-size:13px;margin:6px 0 0}.pager{margin-top:18px;overflow:auto}.cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:14px;margin:14px 0}.detail{white-space:pre-wrap;overflow:auto;max-height:60vh}.hint{color:var(--ops-muted);font-size:12px}</style>
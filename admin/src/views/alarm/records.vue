<template>
<div class="page-pad">
<div class="bar"><div><div class="section-title">告警记录</div><p class="intro">保留查询条件，支持分页、详情与当前页导出。</p></div><el-button :disabled="!rows.length" @click="exportCsv('告警记录',rows)">导出当前页</el-button></div><PageState :loading="loading" :error="error" :stale="stale" :updated-at="updatedAt" @retry="refresh" />
<div class="filters"><el-select v-model="filters.status" placeholder="告警状态" clearable aria-label="告警状态"><el-option label="告警中" value="FIRING"/><el-option label="已恢复" value="RESOLVED"/></el-select><el-select v-model="filters.level" clearable placeholder="告警等级" aria-label="告警等级"><el-option v-for="l in ['P0','P1','P2']" :key="l" :label="l" :value="l"/></el-select><el-input v-model="filters.keyword" clearable placeholder="规则名 / 告警内容" aria-label="告警关键词"/><el-date-picker v-model="range" type="datetimerange" value-format="YYYY-MM-DD HH:mm:ss" start-placeholder="开始时间" end-placeholder="结束时间" aria-label="查询时间范围" style="max-width:100%"/><el-button type="primary" :loading="loading" @click="search">查询</el-button><el-button @click="reset">重置条件</el-button></div>
<el-card class="panel"><OpsTable :rows="rows" :columns="columns" :action-width="230"><template #actions="{row}"><el-button v-if="row.status==='FIRING'&&!row.ackBy" link type="warning" :disabled="pending||!canWrite" @click="onAck(row)">认领</el-button><el-button v-if="row.status==='FIRING'" link type="success" :disabled="pending||!canWrite" @click="onResolve(row)">恢复</el-button></template></OpsTable><el-pagination class="pager" v-model:current-page="filters.page" v-model:page-size="filters.pageSize" :page-sizes="[20,50,100]" :total="total" layout="total, sizes, prev, pager, next" @current-change="refresh" @size-change="search" /></el-card>
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
const defaults={status:'',level:'',keyword:'',from:'',to:'',page:1,pageSize:20}
const {filters,persist}=useFilters(defaults)
if('from' in filters && filters.from && filters.to)range.value=[String(filters.from),String(filters.to)]
const columns=[{key:'ruleName',label:'规则',width:180},{key:'level',label:'等级',status:true},{key:'metricKey',label:'指标',width:200},{key:'metricValue',label:'触发值'},{key:'threshold',label:'阈值'},{key:'status',label:'状态',status:true},{key:'ackBy',label:'认领人'},{key:'firedAt',label:'触发时间',width:180},{key:'resolvedAt',label:'恢复时间',width:180}]
const {loading,error,stale,updatedAt,refresh}=usePolling(async()=>{
 await persist()
 const query:any={...filters}
 if(query.jobId){if(!/^\d+$/.test(query.jobId))throw new Error('任务 ID 必须为正整数');query.jobId=Number(query.jobId)}else delete query.jobId
 const r=await ops.alarmRecords(query)
 rows.value=r.list??[];total.value=r.pagination?.total??0
},{interval:0})
async function search(){filters.page=1;Object.assign(filters,'from' in filters ? {from:range.value?.[0]??'',to:range.value?.[1]??''}:{});await refresh()}
async function reset(){Object.assign(filters,defaults);range.value=[];await refresh()}
async function onAck(row:any){
 if(!canWrite.value||pending.value)return
 let remark:string
 try{const r=await ElMessageBox.prompt('填写处理备注（可选）','认领告警',{inputType:'textarea',inputValidator:v=>(v?.length??0)<=500||'备注不能超过 500 字'});remark=r.value??''}
 catch(cause){if(cause==='cancel'||cause==='close')return;ElMessage.error('无法打开确认窗口');return}
 if(await mutate(()=>ops.ackAlarm(row.id,remark),'已认领告警'))await refresh()
}
async function onResolve(row:any){if(!canWrite.value||!await confirmAction('确认手动恢复该告警？若指标仍异常，下轮巡检可能再次触发。'))return;if(await mutate(()=>ops.resolveAlarm(row.id),'告警已恢复'))await refresh()}

</script>
<style scoped>.bar{display:flex;justify-content:space-between;align-items:center;gap:12px;flex-wrap:wrap;margin-bottom:18px}.filters{display:flex;gap:10px;flex-wrap:wrap;align-items:center;margin-bottom:16px}.filters .el-input,.filters .el-select{width:190px}.panel{margin-bottom:16px}.intro{color:var(--ops-muted);font-size:13px;margin:6px 0 0}.pager{margin-top:18px;overflow:auto}.cards{display:grid;grid-template-columns:repeat(auto-fit,minmax(150px,1fr));gap:14px;margin:14px 0}.detail{white-space:pre-wrap;overflow:auto;max-height:60vh}.hint{color:var(--ops-muted);font-size:12px}</style>
<template><div class="page-pad">
<div class="bar"><div><div class="section-title">任务调度</div><p class="intro">查看计划、真实执行结果与失败记录。</p></div><el-button type="primary" :disabled="!canWrite" @click="onAdd">新增任务</el-button></div>
<PageState :loading="loading" :error="error" :stale="stale" :updated-at="updatedAt" @retry="refresh"/>
<div class="filters"><el-input v-model="keyword" clearable placeholder="任务名称或编码" aria-label="搜索任务" @keyup.enter="refresh"/><el-select v-model="enabled" clearable placeholder="全部状态" aria-label="任务启用状态"><el-option label="启用" :value="1"/><el-option label="停用" :value="0"/></el-select><el-button :loading="loading" @click="refresh">查询</el-button><el-button @click="exportCsv('任务调度',jobs)">导出任务</el-button></div>
<el-card class="panel"><OpsTable :rows="jobs" :columns="columns" :action-width="310"><template #actions="{row}"><el-button link :disabled="pending||!canWrite" @click="toggle(row)">{{row.enabled===1?'停用':'启用'}}</el-button><el-button link type="primary" :disabled="pending||!canWrite" @click="onEdit(row)">编辑</el-button><el-button link type="success" :disabled="pending||!canWrite||row.lastStatus==='RUNNING'" @click="onTrigger(row)">执行</el-button><el-button link type="primary" @click="router.push({path:'/job/records',query:{jobId:String(row.id)}})">记录</el-button><el-button link type="danger" :disabled="pending||!canWrite||row.lastStatus==='RUNNING'" @click="onDelete(row)">删除</el-button></template></OpsTable></el-card>
<el-alert v-if="triggerResult" :type="triggerResult.status==='SUCCESS'?'success':triggerResult.status==='FAILED'?'error':'warning'" :title="triggerResult.jobCode+' · '+triggerResult.status" :description="triggerResult.message" show-icon :closable="false"/>
<el-dialog v-model="show" :title="form.id?'编辑任务':'新增任务'" width="min(580px, 94vw)" :close-on-click-modal="false"><el-form ref="formEl" :model="form" :rules="validation" label-position="top" :disabled="pending||!canWrite">
<el-form-item label="任务编码" prop="jobCode"><el-input v-model="form.jobCode" :disabled="!!form.id" maxlength="64" placeholder="唯一编码，如 dailyDbCheck"/></el-form-item><el-form-item label="任务名称" prop="jobName"><el-input v-model="form.jobName" maxlength="100"/></el-form-item><el-form-item label="处理器" prop="handler"><el-select v-model="form.handler" filterable style="width:100%"><el-option v-for="h in handlers" :key="h.handler" :value="h.handler" :label="h.name+' ('+h.handler+')'"/></el-select><div class="hint">{{handlers.find(h=>h.handler===form.handler)?.description}}</div></el-form-item>
<el-form-item label="Cron 表达式" prop="cron"><el-input v-model="form.cron" placeholder="秒 分 时 日 月 周"/><div class="hint">6 段，秒在前。例如：0 0 * * * ? 每小时执行。</div></el-form-item><el-form-item label="处理器参数 JSON" prop="params"><el-input v-model="form.params" type="textarea" :rows="4" placeholder="{}"/></el-form-item><el-form-item label="启用"><el-switch v-model="form.enabled" :active-value="1" :inactive-value="0"/></el-form-item></el-form><template #footer><el-button :disabled="pending" @click="show=false">取消</el-button><el-button type="primary" :loading="pending" :disabled="!canWrite" @click="submit">保存任务</el-button></template></el-dialog>
</div></template>
<script setup lang="ts">
import { computed,ref } from 'vue'
import { useRouter } from 'vue-router'
import * as ops from '@/api/ops'
import { type FormInstance } from 'element-plus'
import PageState from '@/components/PageState.vue'
import OpsTable from '@/components/OpsTable.vue'
import {usePolling} from '@/composables/usePolling'
import {useMutation,confirmAction} from '@/composables/useMutation'
import {useUserStore} from '@/stores/user'
import {exportCsv} from '@/utils/export'
const user=useUserStore(),router=useRouter(),canWrite=computed(()=>!user.isReadOnly&&user.hasPerm('ops.job')),{pending,mutate}=useMutation()
const jobs=ref<any[]>([]),handlers=ref<any[]>([]),keyword=ref(''),enabled=ref<number|undefined>(),show=ref(false),formEl=ref<FormInstance>(),triggerResult=ref<any>(null)
function blank(){return {id:null,jobCode:'',jobName:'',handler:'',cron:'0 0 * * * ?',params:'{}',jobType:'manual',enabled:1}}
const form=ref<any>(blank())
const validation={jobCode:[{required:true,pattern:/^[a-zA-Z0-9_.-]+$/,message:'编码使用字母、数字、下划线、点或短横线',trigger:'blur'}],jobName:[{required:true,message:'请输入任务名称',trigger:'blur'}],handler:[{required:true,message:'请选择处理器',trigger:'change'}],cron:[{required:true,validator:(_r:any,value:string,done:any)=>done(value.trim().split(/\s+/).length===6?undefined:new Error('Cron 必须为 6 段')),trigger:'blur'}],params:[{validator:(_r:any,value:string,done:any)=>{try{const parsed=JSON.parse(value||'{}');if(!parsed||Array.isArray(parsed)||typeof parsed!=='object')throw new Error();done()}catch{done(new Error('参数必须为 JSON 对象'))}},trigger:'blur'}]}
const columns=[{key:'jobName',label:'任务名称',width:170},{key:'jobCode',label:'任务编码',width:170},{key:'handler',label:'处理器',width:170},{key:'cron',label:'Cron',width:160},{key:'lastStatus',label:'上次状态',status:true},{key:'enabled',label:'启用',format:(v:any)=>v===1?'启用':'停用'},{key:'lastRunAt',label:'上次执行',width:180},{key:'nextRunAt',label:'下次执行',width:180},{key:'lastDurationMs',label:'最近耗时 ms'},{key:'lastMessage',label:'最近结果',width:250}]
const {loading,error,stale,updatedAt,refresh}=usePolling(async()=>{const [j,h]=await Promise.all([ops.jobList({keyword:keyword.value,enabled:enabled.value}),ops.jobHandlers()]);jobs.value=j;handlers.value=h},{interval:15000})
function onAdd(){form.value=blank();show.value=true;formEl.value?.clearValidate()}
function onEdit(row:any){form.value={...row,params:row.params||'{}'};show.value=true;formEl.value?.clearValidate()}
async function toggle(row:any){if(!canWrite.value)return;if(await mutate(()=>ops.toggleJob(row.id,row.enabled===1?0:1),'任务状态已更新'))await refresh()}
async function onDelete(row:any){if(!canWrite.value||!await confirmAction('删除任务「'+row.jobName+'」？'))return;if(await mutate(()=>ops.deleteJob(row.id),'任务已删除'))await refresh()}
async function onTrigger(row:any){if(!canWrite.value||!await confirmAction('立即执行「'+row.jobName+'」？处理器可能修改业务数据。'))return;await mutate(async()=>{const r:any=await ops.triggerJob(row.id);triggerResult.value=r;if(r.status==='FAILED')throw new Error(r.message||'任务执行失败')},'');await refresh()}
async function submit(){if(!canWrite.value)return;try{if(!await formEl.value?.validate())return}catch{return}const saved=await mutate(()=>form.value.id?ops.updateJob(form.value.id,{...form.value}):ops.createJob({...form.value}),'任务已保存');if(saved){show.value=false;await refresh()}}
</script><style scoped>.bar,.filters{display:flex;gap:12px;flex-wrap:wrap;align-items:center}.bar{justify-content:space-between;margin-bottom:18px}.filters{margin:16px 0}.filters .el-input,.filters .el-select{width:220px}.intro,.hint{color:var(--ops-muted);font-size:13px}.panel{margin-bottom:18px}</style>

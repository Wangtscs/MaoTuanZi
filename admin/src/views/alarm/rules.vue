<template><div class="page-pad">
<div class="bar"><div><div class="section-title">告警规则</div><p class="intro">阈值、连续检查与静默周期，统一管理告警响应。</p></div><div class="actions"><el-button :loading="pending" :disabled="!canWrite" @click="onEvaluate">立即评估</el-button><el-button type="primary" :disabled="!canWrite" @click="onAdd">新增规则</el-button></div></div>
<PageState :loading="loading" :error="error" :stale="stale" :updated-at="updatedAt" @retry="refresh"/>
<div class="filters"><el-input v-model="keyword" clearable placeholder="搜索规则 / 指标" aria-label="搜索规则" @keyup.enter="refresh"/><el-select v-model="enabled" clearable placeholder="全部启用状态" aria-label="规则启用状态"><el-option label="启用" :value="1"/><el-option label="停用" :value="0"/></el-select><el-button :loading="loading" @click="refresh">查询</el-button><el-button @click="exportCsv('告警规则',rules)">导出规则</el-button></div>
<el-card class="panel"><OpsTable :rows="rules" :columns="columns" :action-width="245"><template #actions="{row}"><el-button link :disabled="pending||!canWrite" @click="toggle(row)">{{row.enabled===1?'停用':'启用'}}</el-button><el-button link type="primary" :disabled="pending||!canWrite" @click="onEdit(row)">编辑</el-button><el-button link type="danger" :disabled="pending||!canWrite" @click="onDelete(row)">删除</el-button></template></OpsTable></el-card>
<el-card class="panel"><div class="bar"><div><div class="section-title">通知投递回执</div><span class="hint">查看服务端真实回执；未配置渠道时显示 UNCONFIGURED。</span></div><el-button @click="deliveryPoll.refresh">刷新回执</el-button></div><PageState :loading="deliveryPoll.loading.value" :error="deliveryPoll.error.value" :stale="deliveryPoll.stale.value" :updated-at="deliveryPoll.updatedAt.value" @retry="deliveryPoll.refresh"/><OpsTable :rows="deliveries" :columns="deliveryColumns" :action-width="150"><template #actions="{row}"><el-button v-if="row.retryable" link type="primary" :disabled="pending||!canWrite" @click="retry(row)">重试</el-button></template></OpsTable></el-card>
<el-dialog v-model="show" :title="form.id?'编辑告警规则':'新增告警规则'" width="min(600px, 94vw)" :close-on-click-modal="false">
<el-form ref="formEl" :model="form" :rules="validation" label-position="top" :disabled="pending||!canWrite">
<el-form-item label="规则名称" prop="name"><el-input v-model="form.name" maxlength="100" show-word-limit/></el-form-item>
<el-form-item label="指标" prop="metricKey"><el-select v-model="form.metricKey" filterable style="width:100%"><el-option v-for="m in catalog" :key="m.key" :label="m.name+' ('+m.unit+')'" :value="m.key"/></el-select></el-form-item>
<div class="form-grid"><el-form-item label="比较符" prop="operator"><el-select v-model="form.operator"><el-option v-for="(label,op) in operators" :key="op" :value="op" :label="label"/></el-select></el-form-item><el-form-item label="阈值" prop="threshold"><el-input-number v-model="form.threshold" :precision="2"/></el-form-item><el-form-item label="连续检查次数" prop="durationChecks"><el-input-number v-model="form.durationChecks" :min="1" :max="1000" :precision="0"/></el-form-item><el-form-item label="静默分钟" prop="silenceMinutes"><el-input-number v-model="form.silenceMinutes" :min="0" :max="10080" :precision="0"/></el-form-item></div>
<el-form-item label="等级" prop="level"><el-radio-group v-model="form.level"><el-radio-button value="P0">P0 严重</el-radio-button><el-radio-button value="P1">P1 警告</el-radio-button><el-radio-button value="P2">P2 提示</el-radio-button></el-radio-group></el-form-item>
<el-form-item label="通知渠道（逗号分隔）"><el-input v-model="form.channels" placeholder="未配置渠道时仅记录回执"/></el-form-item><el-form-item label="说明"><el-input v-model="form.description" type="textarea" maxlength="1000"/></el-form-item>
<el-button :loading="previewing" @click="onPreview">试算当前指标</el-button><el-alert v-if="previewText" :title="previewText" :type="previewMatched?'warning':'info'" :closable="false" style="margin-top:12px"/>
</el-form><template #footer><el-button :disabled="pending" @click="show=false">取消</el-button><el-button type="primary" :loading="pending" :disabled="!canWrite" @click="submit">保存规则</el-button></template>
</el-dialog></div></template>
<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import * as ops from '@/api/ops'
import { ElMessage, type FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'
import PageState from '@/components/PageState.vue'
import OpsTable from '@/components/OpsTable.vue'
import { usePolling } from '@/composables/usePolling'
import { useMutation, confirmAction } from '@/composables/useMutation'
import { display } from '@/utils/format'
import { exportCsv } from '@/utils/export'
const user=useUserStore(),canWrite=computed(()=>!user.isReadOnly&&user.hasPerm('ops.alarm')),{pending,mutate}=useMutation()
const rules=ref<any[]>([]),catalog=ref<any[]>([]),keyword=ref(''),enabled=ref<number|undefined>(undefined),show=ref(false),formEl=ref<FormInstance>(),previewing=ref(false),previewText=ref(''),previewMatched=ref(false)
const operators:Record<string,string>={gt:'大于 >',gte:'大于等于 ≥',lt:'小于 <',lte:'小于等于 ≤',eq:'等于 =',ne:'不等于 ≠'}
function blank(){return {id:null,name:'',metricKey:'',operator:'gt',threshold:0,durationChecks:1,silenceMinutes:30,level:'P1',enabled:1,channels:'',description:''}}
const form=ref<any>(blank()),validation={name:[{required:true,message:'请输入规则名称',trigger:'blur'}],metricKey:[{required:true,message:'请选择指标',trigger:'change'}],threshold:[{required:true,type:'number',message:'请输入有效阈值',trigger:'change'}],durationChecks:[{required:true,type:'number',min:1,message:'连续检查次数至少为 1',trigger:'change'}],silenceMinutes:[{required:true,type:'number',min:0,message:'静默分钟不能为负数',trigger:'change'}]}
watch(form,()=>{previewText.value=''}, {deep:true})
const columns=[{key:'name',label:'规则名称',width:180},{key:'metricKey',label:'指标',width:200},{key:'operator',label:'比较符',format:(v:any)=>operators[v]??v},{key:'threshold',label:'阈值'},{key:'durationChecks',label:'连续检查次数'},{key:'level',label:'等级',status:true},{key:'lastValue',label:'最近取值'},{key:'lastCheckAt',label:'最近检查',width:180},{key:'enabled',label:'启用',format:(v:any)=>v===1?'启用':'停用'},{key:'silenceMinutes',label:'静默分钟'}]
const {loading,error,stale,updatedAt,refresh}=usePolling(async()=>{const [r,c]=await Promise.all([ops.alarmRules({keyword:keyword.value,enabled:enabled.value}),ops.alarmMetricCatalog()]);rules.value=r;catalog.value=c},{interval:0})
function onAdd(){form.value=blank();show.value=true;formEl.value?.clearValidate()}
function onEdit(row:any){form.value={...row};show.value=true;formEl.value?.clearValidate()}
async function toggle(row:any){if(!canWrite.value)return;if(await mutate(()=>ops.toggleAlarmRule(row.id,row.enabled===1?0:1),'规则状态已更新'))await refresh()}
async function onDelete(row:any){if(!canWrite.value||!await confirmAction('删除告警规则「'+row.name+'」？'))return;if(await mutate(()=>ops.deleteAlarmRule(row.id),'规则已删除'))await refresh()}
async function valid(){try{return await formEl.value?.validate()}catch{return false}}
async function onPreview(){if(previewing.value||!await valid())return;previewing.value=true;try{const r=await ops.previewAlarmRule({...form.value});previewMatched.value=r.matched;previewText.value=r.currentValue==null?'当前指标未采集，无法判定':(r.matched?'当前指标命中':'当前指标未命中')+' · 当前值 '+display(r.currentValue)+' '+(r.unit??'')}catch(cause){previewText.value=cause instanceof Error?cause.message:'试算失败';previewMatched.value=false}finally{previewing.value=false}}
async function submit(){if(!canWrite.value||!await valid())return;const saved=await mutate(()=>form.value.id?ops.updateAlarmRule(form.value.id,{...form.value}):ops.createAlarmRule({...form.value}),'规则已保存');if(saved){show.value=false;await refresh()}}
async function onEvaluate(){if(!canWrite.value)return;await mutate(async()=>{const r:any=await ops.evaluateAlarms();ElMessage.success('评估完成，本轮触发 '+r.fired+' 条告警')},'');await refresh()}
const deliveries=ref<any[]>([]),deliveryColumns=[{key:'id',label:'回执 ID'},{key:'channel',label:'渠道'},{key:'status',label:'投递状态',status:true},{key:'detail',label:'投递结果',width:300},{key:'attempts',label:'尝试次数'},{key:'updatedAt',label:'时间',width:180}]
const deliveryPoll=usePolling(async()=>{deliveries.value=await ops.alarmDeliveries()},{interval:0})
async function retry(row:any){if(!canWrite.value||!await confirmAction('重新投递该通知？'))return;await mutate(async()=>{const r=await ops.retryDelivery(row.id);if(r.status!=='ACCEPTED')throw new Error(r.detail||'投递状态：'+r.status)},'接收方已接受通知，尚未证明人员送达');await deliveryPoll.refresh()}
</script><style scoped>.bar,.actions,.filters{display:flex;align-items:center;gap:12px;flex-wrap:wrap}.bar{justify-content:space-between;margin-bottom:18px}.filters{margin:16px 0}.filters .el-input,.filters .el-select{width:220px}.intro,.hint{color:var(--ops-muted);font-size:13px}.panel{margin-bottom:18px}.form-grid{display:grid;grid-template-columns:repeat(auto-fit,minmax(210px,1fr));gap:12px}</style>

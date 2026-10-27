<template><div class="page-pad">
<div class="bar"><div><div class="section-title">运维配置</div><p class="intro">配置修改保留审计历史；敏感值默认脱敏，未修改时保留原值。</p></div><el-button type="primary" :disabled="!canWrite" @click="onAdd">新增配置</el-button></div>
<PageState :loading="loading" :error="error" :stale="stale" :updated-at="updatedAt" @retry="refresh"/>
<div class="filters"><el-input v-model="keyword" clearable placeholder="搜索配置键 / 说明" aria-label="搜索配置"/><el-button :loading="loading" @click="refresh">刷新</el-button></div>
<el-card class="panel"><OpsTable :rows="rows" :columns="columns" :action-width="240"><template #actions="{row}"><el-button link type="primary" :disabled="!canWrite||pending" @click="onEdit(row)">编辑</el-button><el-button link @click="openHistory(row)">历史</el-button><el-button link type="danger" :disabled="!canWrite||pending" @click="onDelete(row)">删除</el-button></template></OpsTable></el-card>
<el-dialog v-model="show" :title="form._edit?'编辑配置':'新增配置'" width="min(560px,94vw)" :close-on-click-modal="false"><el-form ref="formEl" :model="form" :rules="validation" label-position="top" :disabled="pending||!canWrite"><el-form-item label="配置键" prop="configKey"><el-input v-model="form.configKey" :disabled="form._edit" placeholder="ops.xxx" maxlength="200"/></el-form-item><el-form-item label="配置值" prop="configValue"><el-input v-model="form.configValue" :type="isSensitive(form.configKey)?'password':'textarea'" :show-password="isSensitive(form.configKey)" :rows="4"/><div class="hint" v-if="form.configValue==='********'">当前为已脱敏原值，直接保存会保留原值。输入新内容将替换原值。</div></el-form-item><el-form-item label="说明"><el-input v-model="form.description" maxlength="1000"/></el-form-item></el-form><template #footer><el-button :disabled="pending" @click="show=false">取消</el-button><el-button type="primary" :loading="pending" :disabled="!canWrite" @click="submit">保存配置</el-button></template></el-dialog>
<el-drawer v-model="historyShow" :title="'配置历史 · '+historyKey" size="min(760px,94vw)"><PageState :loading="historyLoading" :error="historyError" :updated-at="historyAt" @retry="loadHistory"/><OpsTable :rows="history" :columns="historyColumns" :action-width="155"><template #actions="{row}"><el-button v-if="row.canRollback" link type="warning" :disabled="pending||!canWrite" @click="rollback(row)">回滚至此前</el-button></template></OpsTable><el-alert title="回滚会恢复该修订之前的值，并新增一条审计记录。敏感历史不支持回滚。" type="info" :closable="false" style="margin-top:16px"/></el-drawer>
</div></template>
<script setup lang="ts">
import {computed,ref} from 'vue'
import * as ops from '@/api/ops'
import {type FormInstance} from 'element-plus'
import {useUserStore} from '@/stores/user'
import PageState from '@/components/PageState.vue'
import OpsTable from '@/components/OpsTable.vue'
import {usePolling} from '@/composables/usePolling'
import {useMutation,confirmAction} from '@/composables/useMutation'
const user=useUserStore(),canWrite=computed(()=>!user.isReadOnly&&user.hasPerm('ops.config')),{pending,mutate}=useMutation()
const list=ref<any[]>([]),keyword=ref(''),show=ref(false),formEl=ref<FormInstance>()
const rows=computed(()=>list.value.filter(r=>(r.configKey+' '+r.description).toLowerCase().includes(keyword.value.toLowerCase())))
const form=ref<any>({configKey:'',configValue:'',description:'',_edit:false})
const validation={configKey:[{required:true,pattern:/^ops\.[a-zA-Z0-9_.-]+$/,message:'配置键必须以 ops. 开头，仅使用字母、数字、点、短横线和下划线',trigger:'blur'}],configValue:[{required:true,message:'请输入配置值',trigger:'blur'}]}
function isSensitive(key:string){return /password|secret|token|credential|api.?key|webhook/i.test(key)}
const columns=[{key:'configKey',label:'配置键',width:230},{key:'configValue',label:'配置值',width:230,format:(v:any,row:any)=>isSensitive(row.configKey)?'********':String(v??'—')},{key:'description',label:'说明',width:230},{key:'updatedAt',label:'修改时间',width:180},{key:'updatedBy',label:'修改人'}]
const {loading,error,stale,updatedAt,refresh}=usePolling(async()=>{list.value=await ops.opsConfigs()},{interval:0})
function onAdd(){form.value={configKey:'ops.',configValue:'',description:'',_edit:false};show.value=true;formEl.value?.clearValidate()}
function onEdit(row:any){form.value={...row,_edit:true};show.value=true;formEl.value?.clearValidate()}
async function submit(){if(!canWrite.value)return;try{if(!await formEl.value?.validate())return}catch{return}if(!await confirmAction('确认保存配置「'+form.value.configKey+'」？'))return;const saved=await mutate(()=>ops.saveOpsConfig({configKey:form.value.configKey,configValue:form.value.configValue,description:form.value.description}),'配置已保存');if(saved){show.value=false;await refresh()}}
async function onDelete(row:any){if(!canWrite.value||!await confirmAction('删除配置「'+row.configKey+'」？使用该配置的功能可能受影响。'))return;if(await mutate(()=>ops.deleteOpsConfig(row.configKey),'配置已删除'))await refresh()}
const historyShow=ref(false),historyKey=ref(''),history=ref<any[]>([]),historyLoading=ref(false),historyError=ref(''),historyAt=ref<Date|null>(null)
const historyColumns=[{key:'revision',label:'修订号'},{key:'action',label:'动作'},{key:'operator',label:'操作人'},{key:'timestamp',label:'时间',width:180},{key:'previousValue',label:'修改前的值',width:220},{key:'previousDescription',label:'修改前说明',width:220}]
async function openHistory(row:any){historyKey.value=row.configKey;history.value=[];historyAt.value=null;historyShow.value=true;await loadHistory()}
async function loadHistory(){if(historyLoading.value)return;historyLoading.value=true;historyError.value='';const key=historyKey.value;try{const result=await ops.configHistory(key);if(key===historyKey.value){history.value=result;historyAt.value=new Date()}}catch(cause){historyError.value=cause instanceof Error?cause.message:'历史加载失败'}finally{historyLoading.value=false}}
async function rollback(row:any){if(!canWrite.value||!await confirmAction('将「'+historyKey.value+'」恢复到修订 '+row.revision+' 之前？'))return;if(await mutate(()=>ops.rollbackConfig(historyKey.value,row.revision),'配置已回滚')){await loadHistory();await refresh()}}
</script><style scoped>.bar,.filters{display:flex;align-items:center;gap:12px;flex-wrap:wrap}.bar{justify-content:space-between;margin-bottom:18px}.filters{margin:16px 0}.filters .el-input{width:300px}.intro,.hint{color:var(--ops-muted);font-size:13px}</style>

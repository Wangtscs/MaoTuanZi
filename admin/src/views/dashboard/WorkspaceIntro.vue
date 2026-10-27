<template>
 <section class="workspace-intro panel" aria-label="角色工作区">
  <div class="intro-heading"><div><span class="intro-kicker">让每一次运营行动都有依据</span><h2>{{ greeting }}，{{ user.nickname || user.username || '同事' }}</h2><p>{{ personaDescriptions[persona] }}</p></div><div class="intro-actions"><el-select v-model="persona" aria-label="选择工作视角" style="width:150px"><el-option label="值班视角" value="oncall"/><el-option label="经营视角" value="manager"/><el-option label="技术视角" value="engineer"/></el-select><el-button @click="guide=true">使用指南</el-button></div></div>
  <div class="focus-grid"><router-link v-for="card in cards" :key="card.label" :to="card.to" class="focus-card"><span>{{ card.label }} <el-icon><TopRight/></el-icon></span><b>{{ card.value == null ? '—' : card.value }}<small>{{ card.unit }}</small></b><p>{{ card.note }}</p></router-link></div>
  <div class="next-action"><span class="action-caption">建议下一步</span><router-link :to="nextAction.to">{{ nextAction.label }} <span aria-hidden="true">→</span></router-link><small>数据来自当前统计窗口，未采集的指标显示“—”。</small></div>
  <el-dialog v-model="guide" title="从了解现状到完成处置" width="640px"><div class="guide-copy"><p>这个工作区把技术运维与业务经营连接起来。请根据工作职责选择视角。</p><ol><li><b>看现状：</b>概览查看健康、流量和待办；指标旁的单位、时间范围定义其统计口径。</li><li><b>找原因：</b>关联检索可按任务号、订单号、用户编号和 Trace ID 定位已有数据。</li><li><b>做处置：</b>工作待办提供队列入口；告警时间线记录交接、升级和复盘。</li><li><b>验结果：</b>查看最新采集时间、执行记录和真实回执，区分“接收端接受”和“人员送达”。</li></ol><el-alert title="未知不等于正常，未配置不等于可用。样例演示不连接真实业务服务。" type="info" :closable="false"/><div class="guide-links"><router-link to="/workspace/todos" @click="guide=false">打开工作待办</router-link><router-link to="/workspace/search" @click="guide=false">打开关联检索</router-link></div><el-checkbox v-model="hideGuide">下次不自动展示</el-checkbox></div></el-dialog>
 </section>
</template>
<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { hasModulePermission } from '@/utils/authPolicy'
const props=defineProps<{summary:any}>(),user=useUserStore(),guide=ref(false),hideGuide=ref(false)
function saved(key:string,fallback:string){try{return localStorage.getItem(key)||fallback}catch{return fallback}}
const persona=ref(saved('ops.persona','oncall'))
if(!['manager','engineer','oncall'].includes(persona.value))persona.value='oncall'
const personaDescriptions:Record<string,string>={oncall:'优先关注未恢复告警、业务积压与需要交接的事项。',manager:'关注用户增长、支付订单与 AI 交付质量。',engineer:'关注请求性能、资源水位与技术故障。'}
watch(persona,value=>{try{localStorage.setItem('ops.persona',value)}catch{}})
watch(hideGuide,value=>{try{localStorage.setItem('ops.guide.hidden',value?'1':'0')}catch{}})
onMounted(()=>{hideGuide.value=saved('ops.guide.hidden','0')==='1';guide.value=!hideGuide.value})
const greeting=computed(()=>new Date().getHours()<12?'上午好':new Date().getHours()<18?'下午好':'晚上好')
const can=(module:string)=>hasModulePermission(user.perms,module,'read')
const cards=computed(()=>{
 const d=props.summary, todo='/workspace/todos'
 if(persona.value==='manager')return [{label:'已支付订单',value:d?.biz?.paidOrders24h,unit:'笔',note:'最近24小时 · 支付不等同于已交付',to:'/biz/inspect'},{label:'新增用户',value:d?.biz?.newUsers24h,unit:'人',note:'最近24小时 · 注册用户',to:'/biz/inspect'},{label:'AI失败任务',value:d?.biz?.aiTaskFailed24h,unit:'个',note:'查看失败原因与影响范围',to:'/biz/inspect'}]
 if(persona.value==='engineer')return [{label:'请求吞吐',value:d?.core?.qps,unit:'req/s',note:'最近60秒 · HTTP请求',to:can('ops.metrics')?'/monitor/realtime':todo},{label:'响应时间P95',value:d?.core?.p95,unit:'ms',note:'最近5分钟 · 95%请求耗时上界',to:can('ops.metrics')?'/monitor/metrics':todo},{label:'堆内存水位',value:d?.core?.heapUsedPct,unit:'%',note:'当前JVM堆内存使用率',to:can('ops.system')?'/system/resource':todo}]
 return [{label:'未恢复告警',value:d?.alarm?.stat?.firing,unit:'条',note:'当前全量 · 按优先级处理',to:can('ops.alarm')?'/alarm/records?status=FIRING':todo},{label:'AI待处理',value:d?.biz?.aiTaskPending,unit:'个',note:'当前队列 · 查看积压任务',to:todo+'?type=AI_PENDING'},{label:'退款待审核',value:d?.biz?.pendingRefunds,unit:'笔',note:'当前队列 · 关注等待时长',to:todo+'?type=REFUND'}]
})
const nextAction=computed(()=>persona.value==='manager'?{to:'/biz/inspect',label:'查看业务趋势与交付质量'}:persona.value==='engineer'?{to:'/workspace/search',label:'按任务号或 Trace ID 定位问题'}:{to:'/workspace/todos',label:'打开今日待办，优先处理超时事项'})
</script>
<style scoped>
.workspace-intro{padding:24px;margin:0 0 24px;border:1px solid var(--ops-border);background:linear-gradient(120deg,var(--ops-panel),var(--ops-panel-alt))}.intro-heading{display:flex;justify-content:space-between;gap:20px}.intro-kicker{font-size:12px;color:var(--ops-accent);font-weight:600}.intro-heading h2{font-size:25px;letter-spacing:-.6px;margin:9px 0}.intro-heading p,.focus-card p{font-size:12px;color:var(--ops-sub);line-height:1.8;margin:0}.intro-actions{display:flex;gap:10px;align-items:flex-start}.focus-grid{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:14px;margin-top:22px}.focus-card{padding:18px;background:var(--ops-panel);border:1px solid var(--ops-border);border-radius:10px;color:var(--ops-text);transition:border-color .15s}.focus-card:hover{border-color:var(--ops-accent)}.focus-card>span{font-size:13px;display:flex;align-items:center;justify-content:space-between}.focus-card b{display:block;font-size:30px;margin:13px 0;font-variant-numeric:tabular-nums}.focus-card small{font-size:12px;font-weight:400;margin-left:8px;color:var(--ops-sub)}.next-action{display:flex;align-items:center;flex-wrap:wrap;gap:12px;margin-top:18px;font-size:13px}.action-caption{color:var(--ops-sub)}.next-action small{font-size:12px;color:var(--ops-sub);margin-left:auto}.guide-copy{font-size:14px;line-height:1.9}.guide-copy ol{padding-left:22px}.guide-copy li{padding:7px 0}.guide-links{display:flex;gap:24px;margin:20px 0}@media(max-width:760px){.workspace-intro{padding:18px}.intro-heading{flex-direction:column}.intro-heading h2{font-size:22px}.intro-actions{width:100%}.focus-grid{grid-template-columns:1fr;gap:10px}.focus-card{padding:14px}.focus-card b{font-size:26px;margin:9px 0}.next-action{align-items:flex-start}.next-action small{margin-left:0;width:100%}}
</style>

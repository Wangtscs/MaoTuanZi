import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import vm from 'node:vm'
import path from 'node:path'
import { createRequire } from 'node:module'
const require=createRequire(import.meta.url)
const ts=require('typescript')
const {parse,compileScript}=require('@vue/compiler-sfc')
const root=path.resolve(import.meta.dirname,'..')
function execute(source,mocks={},globals={}) {
 const output=ts.transpileModule(source,{compilerOptions:{module:ts.ModuleKind.CommonJS,target:ts.ScriptTarget.ES2020,esModuleInterop:true}}).outputText
 const module={exports:{}}
 vm.runInNewContext(output,{module,exports:module.exports,require(id){if(id in mocks)return mocks[id];throw new Error('Unexpected import '+id)},console,URLSearchParams,Error,...globals})
 return module.exports
}
function load(file,mocks,globals){return execute(fs.readFileSync(path.join(root,file),'utf8'),mocks,globals)}
function vueHarness(){
 const mounts=[],unmounts=[],watches=[]
 return {mounts,unmounts,watches,api:{defineComponent:v=>v,ref:value=>({value}),reactive:v=>v,computed:fn=>({get value(){return fn()}}),unref:v=>v&&typeof v==='object'&&'value'in v?v.value:v,onMounted:fn=>mounts.push(fn),onBeforeUnmount:fn=>unmounts.push(fn),watch:(...args)=>watches.push(args),nextTick:()=>Promise.resolve()}}
}
test('API uses actual monitor, toggle, acknowledgement, config and pagination contracts',async()=>{
 const calls=[];const api=load('src/api/ops.ts',{'./request':{api:async c=>{calls.push(c);return {}}}})
 await api.httpTimeline(120);await api.metricSeries(['http.qps','jvm.heap.used.pct'],360)
 await api.toggleAlarmRule(2,0);await api.toggleJob(3,1);await api.ackAlarm(4,'remark');await api.resolveAlarm(4)
 await api.deleteOpsConfig('ops.a&b');await api.alarmRecords({page:3,pageSize:50,from:'2026-10-01 00:00:00'})
 await api.jobRecords({page:2,pageSize:20});await api.bizInspect(72)
 assert.equal(calls[0].url,'/ops/monitor/timeline?seconds=120')
 assert.match(calls[1].url,/keys=http.qps%2Cjvm.heap.used.pct&minutes=360/)
 for(const i of [2,3,4,5])assert.equal(calls[i].method,'PUT')
 assert.equal(calls[2].data.enabled,0);assert.equal(calls[3].data.enabled,1)
 assert.equal(calls[4].data.remark,'remark')
 assert.equal(calls[6].url,'/ops/configs?configKey=ops.a%26b')
 assert.match(calls[7].url,/page=3&pageSize=50&from=/)
 assert.equal(calls[9].url,'/ops/biz/inspect?hours=72')
})
test('polling is single-flight, retains stale data, backs off and pauses when hidden/unmounted',async()=>{
 const v=vueHarness(),listeners={},timers=new Map();let timerId=0,resolveTask,calls=0,fail=false
 const document={hidden:false,addEventListener:(n,f)=>listeners[n]=f,removeEventListener:n=>delete listeners[n]}
 const polling=load('src/composables/usePolling.ts',{vue:v.api},{document,setTimeout:(f,ms)=>{timers.set(++timerId,{f,ms});return timerId},clearTimeout:id=>timers.delete(id)})
 const p=polling.usePolling(()=>{calls++;return fail?Promise.reject(new Error('offline')):new Promise(r=>resolveTask=r)},{interval:5000})
 const first=p.refresh(),second=p.refresh();assert.equal(first,second);assert.equal(calls,1)
 resolveTask();await first;assert.ok(p.updatedAt.value);assert.equal(p.stale.value,false)
 fail=true;await p.refresh();assert.equal(p.stale.value,true);assert.equal(p.error.value,'offline');assert.equal([...timers.values()][0].ms,10000)
 v.mounts[0]();document.hidden=true;listeners.visibilitychange();assert.equal(timers.size,0)
 v.unmounts[0]();assert.equal(Object.keys(listeners).length,0);await p.refresh();assert.equal(calls,3)
})
test('chart initializes after a late conditional DOM mount, resizes and disposes',async()=>{
 const v=vueHarness();let inits=0,resizes=0,disposes=0,observerDisconnected=false,resizeCallback
 const chart={setOption(){},resize(){resizes++},dispose(){disposes++}}
 const helpers=load('src/utils/echarts.ts',{vue:v.api,echarts:{init(){inits++;return chart}}},{getComputedStyle:()=>({getPropertyValue:()=>''}),ResizeObserver:class{constructor(cb){resizeCallback=cb}observe(){}disconnect(){observerDisconnected=true}},MutationObserver:class{observe(){}disconnect(){}},document:{documentElement:{}}})
 const el={value:null};helpers.useChart(el,{value:{series:[]}});await v.mounts[0]();await Promise.resolve();assert.equal(inits,0)
 el.value={};v.watches[0][1]();await Promise.resolve();await Promise.resolve();assert.equal(inits,1)
 resizeCallback();assert.ok(resizes>=2)
 v.unmounts[0]();assert.equal(disposes,1);assert.equal(observerDisconnected,true)
})
for(const file of ['alarm/rules.vue','job/list.vue','config/index.vue'])test(file+' delete cancellation never calls a mutation',async()=>{
 const v=vueHarness();let requests=0
 const el={ElMessageBox:{confirm:()=>Promise.reject('cancel')},ElMessage:{success(){},error(){}}}
 const mutations=load('src/composables/useMutation.ts',{vue:v.api,'element-plus':el})
 const source=fs.readFileSync(path.join(root,'src/views',file),'utf8')
 const script=compileScript(parse(source).descriptor,{id:'test'}).content
 const mocks={vue:v.api,'element-plus':el,'@/api/ops':new Proxy({},{get:()=>()=>{requests++;return Promise.resolve({})}}),'@/stores/user':{useUserStore:()=>({isReadOnly:false,hasPerm:()=>true})},'vue-router':{useRouter:()=>({})},'@/composables/usePolling':{usePolling:()=>({refresh:async()=>{},loading:{value:false},error:{value:''},stale:{value:false},updatedAt:{value:null}})},'@/composables/useMutation':mutations,'@/utils/format':{display:String},'@/utils/export':{exportCsv(){}}}
 for(const match of script.matchAll(/from ['"]([^'"]+\.vue)['"]/g))mocks[match[1]]={default:{}}
 const component=execute(script,mocks).default
 const exposed=component.setup({}, {expose(){}})
 await exposed.onDelete({id:1,name:'rule',jobName:'job',configKey:'ops.key'})
 assert.equal(requests,0)
})
test('alarm acknowledgement prompt cancellation never submits acknowledgement',async()=>{
 const v=vueHarness();let requests=0
 const el={ElMessageBox:{prompt:()=>Promise.reject('close')},ElMessage:{success(){},error(){}}}
 const mutations=load('src/composables/useMutation.ts',{vue:v.api,'element-plus':el})
 const script=compileScript(parse(fs.readFileSync(path.join(root,'src/views/alarm/records.vue'),'utf8')).descriptor,{id:'test'}).content
 const mocks={vue:v.api,'element-plus':el,'@/api/ops':{ackAlarm(){requests++}},'@/stores/user':{useUserStore:()=>({isReadOnly:false,hasPerm:()=>true})},'@/composables/usePolling':{usePolling:()=>({refresh:async()=>{}})},'@/composables/useFilters':{useFilters:defaults=>({filters:defaults,persist:async()=>{}})},'@/composables/useMutation':mutations,'@/utils/format':{display:String},'@/utils/export':{exportCsv(){}}}
 for(const match of script.matchAll(/from ['"]([^'"]+\.vue)['"]/g))mocks[match[1]]={default:{}}
 await execute(script,mocks).default.setup({},{expose(){}}).onAck({id:1})
 assert.equal(requests,0)
})
test('formatters retain unknown values and protect exported CSV formulas',()=>{
 const format=load('src/utils/format.ts',{}),csv=load('src/utils/export.ts',{})
 assert.equal(format.display(null),'—');assert.equal(format.display(0),'0');assert.equal(format.bytes(0),'0 B')
 assert.equal(csv.csvCell('=HYPERLINK("x")'),'"\'=HYPERLINK(""x"")"')
 assert.equal(csv.csvCell('a,b'),'"a,b"')
})
function viewHarness(file,opsOverrides={}){
 const v=vueHarness(),requests=[],messages=[],tasks=[]
 const el={ElMessageBox:{confirm:async()=>{},prompt:async()=>({value:'remark'})},ElMessage:{success:m=>messages.push(['success',m]),error:m=>messages.push(['error',m])}}
 const mutations=load('src/composables/useMutation.ts',{vue:v.api,'element-plus':el})
 const polling={usePolling:task=>{tasks.push(task);return {refresh:task,loading:{value:false},error:{value:''},stale:{value:false},updatedAt:{value:null}}}}
 const ops=new Proxy(opsOverrides,{get:(obj,key)=>key in obj?obj[key]:async(...args)=>{requests.push([key,args]);return []}})
 const script=compileScript(parse(fs.readFileSync(path.join(root,'src/views',file),'utf8')).descriptor,{id:'test'}).content
 const mocks={vue:v.api,'element-plus':el,'@/api/ops':ops,'@/stores/user':{useUserStore:()=>({isReadOnly:false,hasPerm:()=>true})},'vue-router':{useRouter:()=>({push(){}})},'@/composables/usePolling':polling,'@/composables/useFilters':{useFilters:defaults=>({filters:{...defaults},persist:async()=>{}})},'@/composables/useMutation':mutations,'@/utils/format':{display:v=>v??'—'},'@/utils/export':{exportCsv(){}}}
 for(const match of script.matchAll(/from ['"]([^'"]+\.vue)['"]/g))mocks[match[1]]={default:{}}
 return {state:execute(script,mocks).default.setup({},{expose(){}}),tasks,requests,messages}
}
test('record views read pagination.total and preserve requested pageSize',async()=>{
 for(const [file,method] of [['alarm/records.vue','alarmRecords'],['job/records.vue','jobRecords'],['audit/admin.vue','auditAdminLogs'],['audit/content.vue','auditContentLogs']]){
  let query
  const h=viewHarness(file,{[method]:async p=>{query=p;return {list:[{id:17}],pagination:{page:3,pageSize:50,total:123}}}})
  h.state.filters.page=3;h.state.filters.pageSize=50;await h.tasks[0]()
  assert.equal(h.state.total.value,123,file);assert.equal(h.state.rows.value[0].id,17);assert.equal(query.page,3);assert.equal(query.pageSize,50);assert.equal('size'in query,false)
 }
})
test('alarm preview submits all validated fields and reads currentValue',async()=>{
 let payload
 const h=viewHarness('alarm/rules.vue',{previewAlarmRule:async p=>{payload=p;return {matched:true,currentValue:88,unit:'%'}}})
 h.state.formEl.value={validate:async()=>true};h.state.form.value={name:'Heap rule',metricKey:'jvm.heap.used.pct',operator:'gt',threshold:80,durationChecks:3,silenceMinutes:10,level:'P1',enabled:1}
 await h.state.onPreview();assert.equal(payload.name,'Heap rule');assert.equal(payload.operator,'gt');assert.equal(payload.durationChecks,3);assert.equal(payload.silenceMinutes,10)
 assert.match(h.state.previewText.value,/88/)
})
test('manual trigger FAILED is shown as failure and does not announce success',async()=>{
 const h=viewHarness('job/list.vue',{triggerJob:async()=>({jobCode:'checkDb',status:'FAILED',message:'db offline'}),jobList:async()=>[],jobHandlers:async()=>[]})
 await h.state.onTrigger({id:3,jobName:'DB check'})
 assert.equal(h.state.triggerResult.value.status,'FAILED');assert.equal(h.messages.filter(m=>m[0]==='success').length,0);assert.equal(h.messages[0][0],'error')
})
test('mutations reject duplicate actions while pending and release the lock after failure',async()=>{
 const v=vueHarness();let finish,calls=0
 const m=load('src/composables/useMutation.ts',{vue:v.api,'element-plus':{ElMessage:{success(){},error(){}},ElMessageBox:{}}}).useMutation()
 const first=m.mutate(()=>{calls++;return new Promise(r=>finish=r)})
 assert.equal(await m.mutate(async()=>{calls++}),false);assert.equal(calls,1);finish();await first
 assert.equal(m.pending.value,false);assert.equal(await m.mutate(async()=>{throw new Error('failed')}),false);assert.equal(m.pending.value,false)
})
test('disabled permission polling never starts automatically; logs can opt into an initial snapshot',async()=>{
 for(const runWhenDisabled of [false,true]){
  const v=vueHarness();let calls=0
  const polling=load('src/composables/usePolling.ts',{vue:v.api},{document:{hidden:false,addEventListener(){},removeEventListener(){}},setTimeout(){return 1},clearTimeout(){}})
  polling.usePolling(async()=>{calls++},{enabled:false,runWhenDisabled})
  v.mounts[0]();await Promise.resolve();assert.equal(calls,runWhenDisabled?1:0)
 }
})
test('operational GET failures use local page feedback while authentication and mutations retain notices',async()=>{
 const calls=[];const api=load('src/api/ops.ts',{'./request':{api:async config=>{calls.push(config)}}})
 await api.overview();await api.realtime();await api.alarmDeliveries();await api.me();await api.login('u','p');await api.toggleJob(1,0)
 assert.equal(calls[0].silent,true);assert.equal(calls[1].silent,true);assert.equal(calls[2].silent,true)
 assert.equal(calls[3].silent,false);assert.equal(calls[4].silent,false);assert.equal(calls[5].silent,false)
})
test('alarm-only rule loading uses its own dictionary and never calls protected monitor catalog',async()=>{
 const calls=[];const api=load('src/api/ops.ts',{'./request':{api:async config=>{calls.push(config);return [{key:'http.qps',name:'HTTP QPS',unit:'req/s'}]}}})
 const result=await api.alarmMetricCatalog()
 assert.equal(calls[0].url,'/ops/alarm/metric-catalog');assert.equal(calls[0].method,'GET');assert.equal(calls[0].silent,true);assert.equal(result[0].key,'http.qps')
 let monitorRequests=0,alarmDictionaryRequests=0
 const h=viewHarness('alarm/rules.vue',{alarmRules:async()=>[{id:1,name:'rule'}],alarmMetricCatalog:async()=>{alarmDictionaryRequests++;return result},metricCatalog:async()=>{monitorRequests++;throw new Error('Forbidden')}})
 await h.tasks[0]()
 assert.equal(monitorRequests,0);assert.equal(alarmDictionaryRequests,1);assert.equal(h.state.catalog.value[0].key,'http.qps');assert.equal(h.state.rules.value[0].name,'rule')
})

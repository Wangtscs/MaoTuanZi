/** Read-only environment smoke. No database writes, credentials in output, or delivery calls. */
const base=process.env.OPS_VERIFY_URL,credential=process.env.OPS_VERIFY_TOKEN
if(!base||!credential){console.error('请通过环境变量 OPS_VERIFY_URL 与 OPS_VERIFY_TOKEN 提供待验环境和临时只读凭据；脚本不会输出凭据。');process.exitCode=2}else{
 const origin=new URL(base);if(!['http:','https:'].includes(origin.protocol))throw Error('仅支持HTTP(S)环境')
 const paths=['/ops/auth/me','/ops/overview','/ops/health','/ops/datasource/db','/ops/datasource/db/tables','/ops/datasource/redis','/ops/biz/inspect','/ops/biz/slo','/ops/biz/providers']
 const results=[]
 for(const path of paths){try{const response=await fetch(new URL(path,origin),{headers:{'X-Admin-Token':'Bearer '+credential.replace(/^Bearer\s+/i,'')},signal:AbortSignal.timeout(8000),redirect:'error'});const body=await response.json();const ok=response.ok&&body.code===0&&body.data!=null;results.push({path,status:response.status,result:ok?'readable':'failed-or-unconfigured'});if(!ok)process.exitCode=1}catch{results.push({path,result:'connection-or-response-failed'});process.exitCode=1}}
 console.log(JSON.stringify({kind:'read-only-environment-smoke',time:new Date().toISOString(),results,limitations:'仅证明只读接口可访问，不证明业务写入、回滚、通知送达或财务正确性'},null,2))
}

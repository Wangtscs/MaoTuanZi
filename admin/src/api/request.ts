import axios,{ type AxiosRequestConfig,type AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { normalizeToken } from '@/utils/authPolicy'
import { isDemoMode, demoResponse } from '@/utils/demo'
export interface ApiResult<T=unknown>{code:number;message:string;data:T;traceId?:string}
export type OpsRequestConfig=AxiosRequestConfig&{silent?:boolean}
export class ApiError extends Error {constructor(message:string,public status?:number,public code?:number,public traceId?:string){super(message+(traceId&&/^[\w.-]{1,100}$/.test(traceId)?' · 关联编号 '+traceId:''));this.name='ApiError'}}
const TOKEN_KEY='ops_admin_token'
let lastNotice='',lastNoticeAt=0,expiring=false
export function getToken():string{if(isDemoMode())return 'local-demo-readonly';try{return normalizeToken(localStorage.getItem(TOKEN_KEY))}catch{return ''}}
export function setToken(token:string){localStorage.setItem(TOKEN_KEY,normalizeToken(token));expiring=false}
export function clearToken(){try{localStorage.removeItem(TOKEN_KEY)}finally{window.dispatchEvent(new Event('ops:session-cleared'))}}
function notice(message:string,config?:OpsRequestConfig){if(config?.silent)return;const now=Date.now();if(message!==lastNotice||now-lastNoticeAt>1800){lastNotice=message;lastNoticeAt=now;ElMessage.error(message)}}
function expireSession(config?:OpsRequestConfig){if(String(config?.url||'').includes('/auth/login')||expiring)return;expiring=true;clearToken();if(router.currentRoute.value.path!=='/login'){const redirect=router.currentRoute.value.fullPath;void router.replace({path:'/login',query:{redirect}}).finally(()=>{expiring=false})}else expiring=false}
const request=axios.create({baseURL:'/',timeout:20000})
request.interceptors.request.use(config=>{if(isDemoMode()){config.adapter=async c=>({data:{code:0,message:'本地演示样例',data:demoResponse(String(c.url||''),c.method)},status:200,statusText:'DEMO',headers:{},config:c});return config}const token=getToken();if(token)config.headers.set('X-Admin-Token','Bearer '+token);return config})
request.interceptors.response.use((response:AxiosResponse<ApiResult>)=>{if(response.config.responseType==='blob'||response.config.responseType==='arraybuffer')return response;const body=response.data;if(!body||typeof body.code!=='number'){const e=new ApiError('服务返回格式不正确，请重试',response.status);notice(e.message,response.config);return Promise.reject(e)}if(body.code===0)return response;const e=new ApiError(body.message||(body.code===103001?'没有执行此操作的权限':'请求失败'),response.status,body.code,body.traceId);if([102001,102002,102003,401].includes(body.code))expireSession(response.config);notice(e.message,response.config);return Promise.reject(e)},error=>{if(axios.isCancel(error))return Promise.reject(error);const status=error.response?.status,body=error.response?.data;const message=body?.message||(isDemoMode()&&error.message?error.message:status===403?'没有执行此操作的权限':error.code==='ECONNABORTED'?'请求超时，请稍后重试':'网络连接失败，请检查连接后重试');if(status===401)expireSession(error.config);const e=new ApiError(message,status,body?.code,body?.traceId);notice(e.message,error.config);return Promise.reject(e)})
/** Operational calls preserve the response contract: body.data. */
export async function api<T=any>(config:OpsRequestConfig):Promise<T>{const response=await request(config);if(config.responseType==='blob'||config.responseType==='arraybuffer')return response.data as T;return (response.data as ApiResult<T>).data}
export default request

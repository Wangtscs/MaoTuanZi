import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import * as opsApi from '@/api/ops'
import { getToken,setToken,clearToken } from '@/api/request'
import { hasPermission,isReadOnlyPermissions,normalizeProfile,normalizeToken,type OpsProfile } from '@/utils/authPolicy'
export const useUserStore=defineStore('user',()=>{
 const token=ref(getToken()),username=ref(''),nickname=ref(''),avatarUrl=ref('')
 const perms=ref<string[]>([]),roles=ref<string[]>([]),loaded=ref(false)
 const isReadOnly=computed(()=>isReadOnlyPermissions(perms.value))
 let profileRequest:Promise<OpsProfile>|null=null,generation=0
 function applyProfile(p:OpsProfile){username.value=p.username;nickname.value=p.nickname;avatarUrl.value=p.avatarUrl||'';perms.value=p.perms;roles.value=p.roles;loaded.value=true}
 function reset(){generation++;profileRequest=null;token.value='';username.value='';nickname.value='';avatarUrl.value='';perms.value=[];roles.value=[];loaded.value=false}
 window.addEventListener('ops:session-cleared',reset)
 async function login(user:string,password:string){const data=await opsApi.login(user.trim(),password);const profile=normalizeProfile(data);const nextToken=normalizeToken(data.token);if(!nextToken||!profile.opsAccess)throw new Error('该账号没有运维平台权限');generation++;profileRequest=null;setToken(nextToken);token.value=nextToken;applyProfile(profile);return data}
 async function fetchMe():Promise<OpsProfile>{if(!getToken())throw new Error('请先登录');if(profileRequest)return profileRequest;const epoch=generation;profileRequest=opsApi.me().then(data=>{if(epoch!==generation)throw new Error('登录状态已变化，请重新操作');const profile=normalizeProfile(data);if(!profile.opsAccess)throw new Error('该账号没有运维平台权限');token.value=getToken();applyProfile(profile);return profile}).finally(()=>{if(epoch===generation)profileRequest=null});return profileRequest}
 function hasPerm(p:string){return hasPermission(perms.value,p)}
 async function logout(){try{await opsApi.logout()}finally{clearToken();reset()}}
 return {token,username,nickname,avatarUrl,perms,roles,loaded,isReadOnly,login,fetchMe,hasPerm,logout,reset}
})

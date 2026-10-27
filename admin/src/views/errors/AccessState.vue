<template>
  <main class="access-state">
    <div class="access-card">
      <span class="eyebrow">MAOTUANZI / OPERATIONS</span>
      <div class="access-code">{{ unavailable ? '连接待恢复' : forbidden ? '403' : '404' }}</div>
      <h1>{{ unavailable ? '暂时无法验证服务状态' : forbidden ? '你没有这个页面的访问权限' : '这个页面暂不存在' }}</h1>
      <p>{{ unavailable ? '请检查后端连接后重试。当前不会把未验证的状态显示为正常。' : forbidden ? '请联系管理员确认角色与权限，或返回你可以访问的工作台。' : '入口可能已经调整，请从工作台重新选择。' }}</p>
      <div class="access-actions"><el-button v-if="unavailable" type="primary" @click="retry">重新连接</el-button><el-button @click="home">{{ user.loaded ? '返回工作台' : '返回登录' }}</el-button></div>
    </div>
  </main>
</template>
<script setup lang="ts">
import { computed } from 'vue'
import { useRoute,useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { safeRedirect } from '@/utils/authPolicy'
const route=useRoute(),router=useRouter(),user=useUserStore()
const unavailable=computed(()=>route.name==='Unavailable'),forbidden=computed(()=>route.name==='Forbidden')
function retry(){router.replace(safeRedirect(route.query.from))}
function home(){if(!user.loaded){router.replace('/login');return}const allowed=router.getRoutes().find(r=>r.meta.perm&&user.hasPerm(String(r.meta.perm)));router.replace(allowed?.path||'/login')}
</script>
<style scoped>
.access-state{min-height:100vh;display:grid;place-items:center;padding:32px;background:var(--ops-bg);color:var(--ops-text)}.access-card{width:min(640px,100%);padding:48px;border:1px solid var(--ops-border);border-radius:24px;background:var(--ops-panel)}.eyebrow{font-size:11px;letter-spacing:.16em;color:var(--ops-sub)}.access-code{font-size:clamp(32px,7vw,64px);font-weight:700;margin:24px 0 16px;color:var(--ops-accent)}h1{font-size:24px;line-height:1.4}p{font-size:14px;line-height:1.8;color:var(--ops-sub)}.access-actions{display:flex;gap:12px;flex-wrap:wrap;margin-top:28px}@media(max-width:600px){.access-card{padding:28px}}
</style>

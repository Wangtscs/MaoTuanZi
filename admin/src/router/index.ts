import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router'
import Layout from '@/layout/index.vue'
import { getToken, clearToken } from '@/api/request'
import { safeRedirect, hasModulePermission } from '@/utils/authPolicy'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  { path: '/forbidden', name: 'Forbidden', component: () => import('@/views/errors/AccessState.vue'), meta: { public: true, recovery: true, title: '访问受限' } },
  { path: '/unavailable', name: 'Unavailable', component: () => import('@/views/errors/AccessState.vue'), meta: { public: true, recovery: true, title: '服务连接异常' } },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '运维概览', icon: 'Odometer', perm: 'ops.view' }
      },
      { path: 'workspace/todos', name: 'WorkTodos', component: () => import('@/views/workspace/todos.vue'), meta: { title: '工作待办', perm: 'ops.view' } },
      { path: 'workspace/search', name: 'WorkSearch', component: () => import('@/views/workspace/search.vue'), meta: { title: '关联检索', perm: 'ops.view' } },
      { path: 'workspace/alarm-timeline', name: 'AlarmTimeline', component: () => import('@/views/workspace/alarm-timeline.vue'), meta: { title: '告警处置时间线', perm: 'ops.alarm' } },
      { path: 'workspace/settings', name: 'BusinessSettings', component: () => import('@/views/workspace/settings.vue'), meta: { title: '经营与服务目标', perm: 'ops.config' } },
      {
        path: 'monitor/realtime',
        name: 'MonitorRealtime',
        component: () => import('@/views/monitor/realtime.vue'),
        meta: { title: '实时监控', icon: 'VideoCamera', perm: 'ops.metrics' }
      },
      {
        path: 'monitor/metrics',
        name: 'MonitorMetrics',
        component: () => import('@/views/monitor/metrics.vue'),
        meta: { title: '指标趋势', icon: 'TrendCharts', perm: 'ops.metrics' }
      },
      {
        path: 'system/resource',
        name: 'SystemResource',
        component: () => import('@/views/system/resource.vue'),
        meta: { title: '系统资源', icon: 'Cpu', perm: 'ops.system' }
      },
      {
        path: 'system/threads',
        name: 'SystemThreads',
        component: () => import('@/views/system/threads.vue'),
        meta: { title: '线程快照', icon: 'Collection', perm: 'ops.system' }
      },
      {
        path: 'logs',
        name: 'Logs',
        component: () => import('@/views/logs/index.vue'),
        meta: { title: '日志中心', icon: 'Document', perm: 'ops.log' }
      },
      {
        path: 'alarm/rules',
        name: 'AlarmRules',
        component: () => import('@/views/alarm/rules.vue'),
        meta: { title: '告警规则', icon: 'Bell', perm: 'ops.alarm' }
      },
      {
        path: 'alarm/records',
        name: 'AlarmRecords',
        component: () => import('@/views/alarm/records.vue'),
        meta: { title: '告警记录', icon: 'Warning', perm: 'ops.alarm' }
      },
      {
        path: 'job/list',
        name: 'JobList',
        component: () => import('@/views/job/list.vue'),
        meta: { title: '任务调度', icon: 'Timer', perm: 'ops.job' }
      },
      {
        path: 'job/records',
        name: 'JobRecords',
        component: () => import('@/views/job/records.vue'),
        meta: { title: '任务记录', icon: 'List', perm: 'ops.job' }
      },
      {
        path: 'datasource/db',
        name: 'DatasourceDb',
        component: () => import('@/views/datasource/db.vue'),
        meta: { title: '数据源·数据库', icon: 'Coin', perm: 'ops.datasource' }
      },
      {
        path: 'datasource/redis',
        name: 'DatasourceRedis',
        component: () => import('@/views/datasource/redis.vue'),
        meta: { title: '数据源·Redis', icon: 'Connection', perm: 'ops.datasource' }
      },
      {
        path: 'storage',
        name: 'Storage',
        component: () => import('@/views/storage/index.vue'),
        meta: { title: '存储概览', icon: 'FolderOpened', perm: 'ops.storage' }
      },
      {
        path: 'biz/inspect',
        name: 'BizInspect',
        component: () => import('@/views/biz/inspect.vue'),
        meta: { title: '业务巡检', icon: 'DataAnalysis', perm: 'ops.view' }
      },
      {
        path: 'audit/admin',
        name: 'AuditAdmin',
        component: () => import('@/views/audit/admin.vue'),
        meta: { title: '管理审计', icon: 'Stamp', perm: 'ops.audit' }
      },
      {
        path: 'audit/content',
        name: 'AuditContent',
        component: () => import('@/views/audit/content.vue'),
        meta: { title: '内容审计', icon: 'Postcard', perm: 'ops.audit' }
      },
      {
        path: 'config',
        name: 'Config',
        component: () => import('@/views/config/index.vue'),
        meta: { title: '运维配置', icon: 'Setting', perm: 'ops.config' }
      },
      {
        path: 'instances',
        name: 'Instances',
        component: () => import('@/views/instance/index.vue'),
        meta: { title: '实例列表', icon: 'Monitor', perm: 'ops.view' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/errors/AccessState.vue'), meta: { public: true, recovery: true, title: '页面不存在' } }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export function firstAccessiblePath(): string {
  const user = useUserStore()
  const group = routes.find(route => route.path === '/')
  const allowed = group?.children?.find(route => route.meta?.perm && hasModulePermission(user.perms,String(route.meta.perm),'read'))
  return allowed ? '/' + allowed.path : '/forbidden'
}

router.beforeEach(async (to) => {
  if (to.meta.recovery) return true
  const token = getToken(), user = useUserStore()
  if (to.meta.public) {
    if (token && !user.loaded) { try { await user.fetchMe() } catch { return true } }
    if (getToken() && user.loaded && to.path === '/login') return firstAccessiblePath()
    return true
  }
  if (!token) return { path: '/login', query: { redirect: safeRedirect(to.fullPath) } }
  if (!user.loaded) {
    try { await user.fetchMe() }
    catch (error: any) {
      if (!getToken() || error.status === 401) { clearToken(); return { path: '/login', query: { redirect: safeRedirect(to.fullPath) } } }
      if (error.status === 403) return { path: '/forbidden', query: { from: to.fullPath } }
      return { path: '/unavailable', query: { from: to.fullPath } }
    }
  }
  if (to.meta.perm && !hasModulePermission(user.perms,String(to.meta.perm),'read')) return { path: '/forbidden', query: { from: to.fullPath } }
  return true
})
router.afterEach(to => { document.title = String(to.meta.title || '运维平台') + ' · 毛团子' })

export default router

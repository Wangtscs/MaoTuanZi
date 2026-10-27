/** Browser-local, explicitly labelled demonstration. Never grants server access. */
const KEY = 'ops_demo_session'
export function isDemoMode(): boolean { try { return sessionStorage.getItem(KEY) === '1' } catch { return false } }
export function enableDemo() { sessionStorage.setItem(KEY, '1'); window.location.assign('/#/dashboard'); window.location.reload() }
export function disableDemo() { sessionStorage.removeItem(KEY); window.location.assign('/#/login'); window.location.reload() }
export const demoPermissions = ['ops.view', 'ops.metrics.read', 'ops.system.read', 'ops.log.read', 'ops.alarm.read', 'ops.job.read', 'ops.datasource.read', 'ops.storage.read', 'ops.audit.read', 'ops.config.read']
const stamp = () => new Date().toISOString()
const page = (rows: unknown[], query: URLSearchParams) => { const n = Math.max(1, Number(query.get('page')) || 1), size = Math.min(100, Math.max(1, Number(query.get('pageSize')) || 20)); return { list: rows.slice((n - 1) * size, n * size), pagination: { page: n, pageSize: size, total: rows.length, hasMore: n * size < rows.length } } }
export function demoResponse(rawUrl: string, method = 'GET'): unknown {
  if (method.toUpperCase() !== 'GET') throw new Error('演示模式为只读，未发送任何业务修改请求。请退出演示后操作。')
  const url = new URL(rawUrl, 'https://demo.invalid'), p = url.pathname, q = url.searchParams, now = stamp()
  const alarm = { id: 1001, ruleName: '样例 · AI 队列等待时间', metricKey: 'ai.pending', level: 'P1', status: 'FIRING', message: '样例数据：待处理任务超过提醒阈值', firedAt: now, ackBy: null }
  const instance = { instanceId: 'DEMO-LOCAL', appName: '毛团子 AI', status: 'UP', self: true, profile: 'DEMO', version: '演示', lastHeartbeatAt: now, qps: 12.8, heapUsedPct: 42 }
  const check = ['database', 'redis', 'disk', 'heap'].map(name => ({ name, ok: true, detail: '本地演示样例，非服务探测结果' }))
  const health = { status: 'UP', score: 100, checks: check, infrastructure: { status: 'UP', score: 100, checks: check }, business: { status: 'UNKNOWN', score: null, checks: [] } }
  const app = { appName: '毛团子 AI · 演示样例', profile: 'DEMO · 本地样例', version: '演示', instanceId: 'DEMO-LOCAL', uptimeMs: 7200000 }
  const trend = Array.from({ length: 24 }, (_, i) => ({ time: Date.now() - (23 - i) * 1000, count: 10 + i % 6, errors: i % 11 === 0 ? 1 : 0 }))
  const biz = { hours: 24, aiTask: { total: 248, failed: 6, failRate: 2.42, pending: 18, processing: 4, statusStats: [{ status: 0, cnt: 18 }, { status: 1, cnt: 4 }, { status: 2, cnt: 220 }, { status: 3, cnt: 6 }], typeStats: [{ taskType: '样例画像', cnt: 248, success: 220, failed: 6, avgSeconds: 12.5 }], failReasons: [{ errorCode: 'DEMO_TIMEOUT', errorMessage: '样例：上游响应超时', cnt: 6 }] }, queue: { pendingWorks: 8, pendingRefunds: 2, pendingReports: 1 }, order: { paid: 32 }, newUsers: 46, userDaily: [{ day: now.slice(0, 10), cnt: 46 }], orderDaily: [{ day: now.slice(0, 10), cnt: 32, amountCents: 89600 }], risks: [{ title: '样例退款待审核', detail: '2笔样例退款，请进入工作待办查看', level: 'warning' }], provider: { status: 'unconfigured', detail: '演示不连接真实供应商' } }
  const metric = { key: 'http.qps', name: 'HTTP QPS（样例）', unit: 'req/s', snapshot: true }
  if (p === '/ops/auth/me') return { username: 'demo-observer', nickname: '演示观察员', roles: ['demo'], perms: demoPermissions, opsAccess: true }
  if (p === '/ops/overview') return { app, health, core: { qps: 12.8, p95: 186, errorRate5m: 0.8, heapUsedPct: 42, memUsedPct: 48, diskFreePct: 78 }, alarm: { stat: { firing: 1, new7d: 1 }, firingList: [alarm] }, biz: { aiTasks24h: 248, aiTaskPending: 18, aiTaskFailed24h: 6, newUsers24h: 46, paidOrders24h: 32, pendingWorks: 8, pendingRefunds: 2, pendingReports: 1 }, job: { enabled: 3, failed: 0 }, instances: [instance], serverTime: now }
  if (p === '/ops/health') return health
  if (p === '/ops/instances') return [instance]
  if (p === '/ops/biz/inspect') return biz
  if (p === '/ops/biz/providers') return { status: 'unconfigured', configured: false, detail: '演示模式不连接供应商，成本数据未采集', providers: [] }
  if (p === '/ops/biz/slo') return { status: 'unconfigured', detail: '演示模式：请在真实工作区配置服务目标', targets: [] }
  if (p === '/ops/biz/queues') return page([{ id: 'DEMO-ORDER-01', type: q.get('type') || 'REFUND', title: '样例 · 退款审核', status: 'PENDING', orderId: 'DEMO-ORDER-01', priority: 'P1', createdAt: now, detail: '仅用于界面展示，不含真实用户数据' }], q)
  if (p.endsWith('/metric-catalog') || p === '/ops/monitor/catalog') return [metric]
  if (p === '/ops/monitor/realtime') return { values: { 'http.qps': 12.8 }, serverTime: Date.now() }
  if (p === '/ops/monitor/timeline') return { timeline: trend, seconds: 24 }
  if (p === '/ops/monitor/series') return { series: [{ ...metric, points: trend.map(r => ({ sampleTime: new Date(r.time).toISOString(), metricValue: r.count })) }] }
  if (p === '/ops/monitor/endpoints') return { list: [{ endpoint: 'GET /demo', count: 248, errorRate: 0.8, avgMs: 110, maxMs: 260 }] }
  if (p === '/ops/alarm/records') return page([alarm], q)
  if (p === '/ops/alarm/rules') return [{ id: 201, name: '样例 · AI积压', metricKey: 'ai.pending', operator: 'gt', threshold: 50, durationChecks: 2, silenceMinutes: 30, level: 'P1', enabled: 1 }]
  if (p === '/ops/alarm/stat') return { firing: 1, total: 1 }
  if (p === '/ops/alarm/deliveries' || p === '/ops/configs/history') return []
  if (p === '/ops/alarm/deliveries/page' || p === '/ops/configs/history/page') return page([], q)
  if (p === '/ops/jobs') return [{ id: 301, jobCode: 'demoInspect', jobName: '样例 · 定时巡检', handler: 'demoHandler', cron: '0 0 * * * ?', enabled: 1, lastStatus: 'SUCCESS', lastRunAt: now, lastDurationMs: 120 }]
  if (p === '/ops/jobs/handlers') return []
  if (p === '/ops/jobs/records') return page([{ id: 401, jobId: 301, jobCode: 'demoInspect', status: 'SUCCESS', startTime: now, durationMs: 120, message: '样例：巡检完成' }], q)
  if (p === '/ops/logs/stat') return { total: 1, byLevel: { INFO: 1 } }
  if (p === '/ops/logs') return { list: [{ id: 1, time: now, timestamp: now, level: 'INFO', logger: 'DEMO', message: '本地演示样例，不是线上日志', traceId: 'DEMO-TRACE-01' }], total: 1, lastId: 1 }
  if (p === '/ops/configs') return [{ configKey: 'ops.demo.notice', configValue: '本地只读样例', description: '退出演示后才可修改真实配置', updatedAt: now }]
  if (p === '/ops/system/resource') return { app, jvm: { heapUsedPct: 42, heapUsedMb: 420, heapMaxMb: 1000, threadCount: 24, javaVersion: '17' }, os: { processCpuLoad: 0.12, memUsedPct: 48, name: 'DEMO', availableProcessors: 4 }, disk: { usedPct: 22, partitions: [] }, dbPool: { usedPct: 20, activeConnections: 2, maximumPoolSize: 10 } }
  if (p === '/ops/system/threads') return { list: [{ id: 1, name: 'demo-worker', state: 'WAITING', stack: '本地样例线程' }] }
  if (p === '/ops/datasource/db') return { schema: 'DEMO', pool: { activeConnections: 2, idleConnections: 8, maximumPoolSize: 10 } }
  if (p === '/ops/datasource/db/tables') return [{ tableName: 'demo_orders', tableRows: 32, totalMb: 1.2, freeMb: 0 }]
  if (p === '/ops/datasource/redis') return { available: true, info: { redis_version: 'DEMO', used_memory_human: '12M' }, keys: [] }
  if (p === '/ops/storage/overview') return { oss: { configured: false }, local: { configured: false }, detail: '本地演示不连接文件存储' }
  if (p === '/ops/audit/admin-logs' || p === '/ops/audit/content-logs') return page([], q)
  throw new Error('该功能尚无演示样例；未访问真实后端。请退出演示后查看。')
}

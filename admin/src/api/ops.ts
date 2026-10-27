import { api as requestApi, type OpsRequestConfig } from './request'

// Operational GET views own their loading/stale/error feedback. Authentication and mutations keep global notices.
function api<T = any>(config: OpsRequestConfig): Promise<T> {
  const silent = config.method === 'GET' && !String(config.url).startsWith('/ops/auth/')
  return requestApi<T>({ ...config, silent })
}
import type { Timeline, Realtime, MetricDefinition, MetricSeries, AlarmRule, AlarmRecord, Job, JobRecord, JobHandler, TriggerResult, LogResult, OpsConfig, ConfigRevision, DeliveryReceipt, Page } from './types'

export interface OpsProfile { adminId?: number; username: string; nickname: string; avatarUrl?: string; roles?: string[]; perms: string[]; opsAccess?: boolean; admin?: Partial<OpsProfile> }
export interface LoginResult extends OpsProfile { token: string }

// ============ 鉴权 ============
export function login(username: string, password: string) {
  return api<LoginResult>({
    url: '/ops/auth/login',
    method: 'POST',
    data: { username, password }
  })
}
export function logout() {
  return api({ url: '/ops/auth/logout', method: 'POST' })
}
export function me() {
  return api<OpsProfile>({
    url: '/ops/auth/me',
    method: 'GET'
  })
}

// ============ 概览 / 健康 / 实例 ============
export function overview() {
  return api<any>({ url: '/ops/overview', method: 'GET' })
}
export function health() {
  return api<any>({ url: '/ops/health', method: 'GET' })
}
export function instances() {
  return api<any[]>({ url: '/ops/instances', method: 'GET' })
}

// ============ 监控 ============
export function realtime() {
  return api<Realtime>({ url: '/ops/monitor/realtime', method: 'GET' })
}
export function metricCatalog() {
  return api<MetricDefinition[]>({ url: '/ops/monitor/catalog', method: 'GET' })
}
export function httpTimeline(seconds = 60) {
  return api<Timeline>({ url: `/ops/monitor/timeline?seconds=${seconds}`, method: 'GET' })
}
export function httpEndpoints(orderBy = 'count', limit = 20) {
  return api<any>({ url: `/ops/monitor/endpoints?orderBy=${orderBy}&limit=${limit}`, method: 'GET' })
}
export function metricSeries(keys: string | string[], minutes = 60) {
  return api<MetricSeries>({ url: `/ops/monitor/series?keys=${encodeURIComponent(Array.isArray(keys) ? keys.join(',') : keys)}&minutes=${minutes}`, method: 'GET' })
}
export function resetMetrics() {
  return api({ url: '/ops/monitor/metrics', method: 'DELETE' })
}

// ============ 系统资源 / 线程 ============
export function systemResource() {
  return api<any>({ url: '/ops/system/resource', method: 'GET' })
}
export function threadDump(state = '', limit = 500) {
  return api<any>({ url: `/ops/system/threads?state=${encodeURIComponent(state)}&limit=${limit}`, method: 'GET' })
}

// ============ 日志 ============
export function queryLogs(params: {
  level?: string
  keyword?: string
  logger?: string
  traceId?: string
  sinceId?: number
  limit?: number
}) {
  const q = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') q.append(k, String(v))
  })
  return api<LogResult>({ url: `/ops/logs?${q.toString()}`, method: 'GET' })
}
export function logStat() {
  return api<any>({ url: '/ops/logs/stat', method: 'GET' })
}
export function clearLogs() {
  return api({ url: '/ops/logs', method: 'DELETE' })
}
export function setLogLevel(level: string) {
  return api({ url: '/ops/logs/level', method: 'PUT', data: { level } })
}

// ============ 告警 ============
/** Dictionary only; alarm-only operators do not need monitor sample access. */
export function alarmMetricCatalog() {
  return api<MetricDefinition[]>({ url: '/ops/alarm/metric-catalog', method: 'GET' })
}

export function alarmRules(params: { keyword?: string; enabled?: number } = {}) {
  const q = new URLSearchParams()
  if (params.keyword) q.append('keyword', params.keyword)
  if (params.enabled !== undefined) q.append('enabled', String(params.enabled))
  return api<AlarmRule[]>({ url: `/ops/alarm/rules?${q.toString()}`, method: 'GET' })
}
export function createAlarmRule(data: AlarmRule) {
  return api({ url: '/ops/alarm/rules', method: 'POST', data })
}
export function updateAlarmRule(id: number, data: AlarmRule) {
  return api({ url: `/ops/alarm/rules/${id}`, method: 'PUT', data })
}
export function deleteAlarmRule(id: number) {
  return api({ url: `/ops/alarm/rules/${id}`, method: 'DELETE' })
}
export function toggleAlarmRule(id: number, enabled: number) {
  return api({ url: `/ops/alarm/rules/${id}/toggle`, method: 'PUT', data: { enabled } })
}
export function previewAlarmRule(data: AlarmRule) {
  return api<any>({ url: '/ops/alarm/rules/preview', method: 'POST', data })
}
export function alarmRecords(params: { status?: string; level?: string; keyword?: string; page?: number; pageSize?: number; from?: string; to?: string } = {}) {
  const q = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') q.append(k, String(v))
  })
  return api<Page<AlarmRecord>>({ url: `/ops/alarm/records?${q.toString()}`, method: 'GET' })
}
export function ackAlarm(id: number, remark: string) {
  return api({ url: `/ops/alarm/records/${id}/ack`, method: 'PUT', data: { remark } })
}
export function resolveAlarm(id: number) {
  return api({ url: `/ops/alarm/records/${id}/resolve`, method: 'PUT' })
}
export function alarmStat(days = 7) {
  return api<any>({ url: `/ops/alarm/stat?days=${days}`, method: 'GET' })
}
export function evaluateAlarms() {
  return api({ url: '/ops/alarm/evaluate', method: 'POST' })
}

// ============ 任务 ============
export function jobList(params: { keyword?: string; enabled?: number } = {}) {
  const q = new URLSearchParams()
  if (params.keyword) q.append('keyword', params.keyword)
  if (params.enabled !== undefined) q.append('enabled', String(params.enabled))
  return api<Job[]>({ url: `/ops/jobs?${q.toString()}`, method: 'GET' })
}
export function createJob(data: Job) {
  return api({ url: '/ops/jobs', method: 'POST', data })
}
export function updateJob(id: number, data: Job) {
  return api({ url: `/ops/jobs/${id}`, method: 'PUT', data })
}
export function deleteJob(id: number) {
  return api({ url: `/ops/jobs/${id}`, method: 'DELETE' })
}
export function toggleJob(id: number, enabled: number) {
  return api({ url: `/ops/jobs/${id}/toggle`, method: 'PUT', data: { enabled } })
}
export function triggerJob(id: number) {
  return api<TriggerResult>({ url: `/ops/jobs/${id}/trigger`, method: 'POST' })
}
export function jobRecords(params: { jobId?: number; status?: string; page?: number; pageSize?: number; from?: string; to?: string } = {}) {
  const q = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') q.append(k, String(v))
  })
  return api<Page<JobRecord>>({ url: `/ops/jobs/records?${q.toString()}`, method: 'GET' })
}
export function jobStat(days = 7) {
  return api<any>({ url: `/ops/jobs/stat?days=${days}`, method: 'GET' })
}
export function jobHandlers() {
  return api<JobHandler[]>({ url: '/ops/jobs/handlers', method: 'GET' })
}

// ============ 数据源 ============
export function dbStatus() {
  return api<any>({ url: '/ops/datasource/db', method: 'GET' })
}
export function dbTables() {
  return api<any[]>({ url: '/ops/datasource/db/tables', method: 'GET' })
}
export function redisStatus() {
  return api<any>({ url: '/ops/datasource/redis', method: 'GET' })
}

// ============ 存储 ============
export function storageOverview(days = 14) {
  return api<any>({ url: `/ops/storage/overview?days=${days}`, method: 'GET' })
}

// ============ 业务巡检 ============
export function bizInspect(hours = 24) {
  return api<any>({ url: `/ops/biz/inspect?hours=${hours}`, method: 'GET' })
}

// ============ 审计 ============
export function auditAdminLogs(params: { module?: string; action?: string; keyword?: string; page?: number; pageSize?: number; from?: string; to?: string } = {}) {
  const q = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') q.append(k, String(v))
  })
  return api<any>({ url: `/ops/audit/admin-logs?${q.toString()}`, method: 'GET' })
}
export function auditContentLogs(params: { checkResult?: string; contentType?: string; keyword?: string; page?: number; pageSize?: number; from?: string; to?: string } = {}) {
  const q = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') q.append(k, String(v))
  })
  return api<any>({ url: `/ops/audit/content-logs?${q.toString()}`, method: 'GET' })
}
export function auditStat() {
  return api<any>({ url: '/ops/audit/stat', method: 'GET' })
}

// ============ 配置 ============
export function opsConfigs() {
  return api<OpsConfig[]>({ url: '/ops/configs', method: 'GET' })
}
export function saveOpsConfig(data: { configKey: string; configValue: string; description?: string }) {
  return api({ url: '/ops/configs', method: 'POST', data })
}
export function deleteOpsConfig(key: string) {
  return api({ url: `/ops/configs?configKey=${encodeURIComponent(key)}`, method: 'DELETE' })
}

export default {
  login,
  logout,
  me,
  overview,
  health,
  instances,
  realtime,
  metricCatalog,
  httpTimeline,
  httpEndpoints,
  metricSeries,
  resetMetrics,
  systemResource,
  threadDump,
  queryLogs,
  logStat,
  clearLogs,
  setLogLevel,
  alarmMetricCatalog,
  alarmRules,
  createAlarmRule,
  updateAlarmRule,
  deleteAlarmRule,
  toggleAlarmRule,
  previewAlarmRule,
  alarmRecords,
  ackAlarm,
  resolveAlarm,
  alarmStat,
  evaluateAlarms,
  jobList,
  createJob,
  updateJob,
  deleteJob,
  toggleJob,
  triggerJob,
  jobRecords,
  jobStat,
  jobHandlers,
  dbStatus,
  dbTables,
  redisStatus,
  storageOverview,
  bizInspect,
  auditAdminLogs,
  auditContentLogs,
  auditStat,
  opsConfigs,
  saveOpsConfig,
  deleteOpsConfig
}

// Operational history and delivery receipts are persisted by the existing audit store.
export function configHistory(key: string) { return api<ConfigRevision[]>({ url: `/ops/configs/history?key=${encodeURIComponent(key)}`, method: 'GET' }) }
export function rollbackConfig(key: string, revision: string) { return api({ url: '/ops/configs/rollback', method: 'POST', data: { key, revision } }) }
export function alarmDeliveries() { return api<DeliveryReceipt[]>({ url: '/ops/alarm/deliveries', method: 'GET' }) }
export function retryDelivery(id: string | number) { return api<DeliveryReceipt>({ url: `/ops/alarm/deliveries/${id}/retry`, method: 'POST' }) }

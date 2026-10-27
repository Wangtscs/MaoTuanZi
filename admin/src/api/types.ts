export interface Pagination { page: number; pageSize: number; total: number; hasMore: boolean }
export interface Page<T> { list: T[]; pagination: Pagination }
export interface MetricDefinition { key: string; name: string; unit: string; snapshot?: boolean }
export interface TimelinePoint { time: number; count: number; errors: number; avgMs: number; maxMs: number }
export interface Timeline { seconds: number; timeline: TimelinePoint[]; window: Record<string, number> }
export interface MetricPoint { metricKey: string; sampleTime: string; metricValue: number | null; maxValue?: number; minValue?: number }
export interface MetricSeries { minutes: number; from: string; to: string; series: { key: string; name: string; unit: string; points: MetricPoint[] }[] }
export interface Realtime { values: Record<string, number | null>; serverTime: number; timeline: TimelinePoint[] }
export interface AlarmRule { id?: number; name: string; metricKey: string; operator: 'gt'|'gte'|'lt'|'lte'|'eq'|'ne'; threshold: number; durationChecks: number; silenceMinutes: number; level: 'P0'|'P1'|'P2'; enabled: number; channels?: string; description?: string; lastValue?: number | null; lastCheckAt?: string; firing?: number }
export interface AlarmRecord { id: number; ruleId: number; ruleName: string; metricKey: string; metricValue: number; threshold: number; level: string; status: 'FIRING'|'RESOLVED'; message: string; firedAt: string; resolvedAt?: string; ackBy?: string; ackAt?: string; remark?: string }
export interface Job { id?: number; jobCode: string; jobName: string; handler: string; cron: string; jobType?: string; params?: string; enabled: number; lastStatus?: string; lastRunAt?: string; nextRunAt?: string; lastDurationMs?: number; lastMessage?: string }
export interface JobRecord { id: number; jobId: number; jobCode: string; status: 'RUNNING'|'SUCCESS'|'FAILED'; triggerType: string; instanceId: string; startAt: string; endAt?: string; durationMs?: number; message?: string }
export interface JobHandler { handler: string; name: string; description: string }
export interface TriggerResult { jobCode: string; triggerType: string; status: 'SUCCESS'|'FAILED'|'SKIPPED'; message: string; durationMs?: number; operator?: string }
export interface LogEntry { id: number; timestamp: string | number; level: string; logger: string; thread: string; traceId?: string; message: string; stackTrace?: string }
export interface LogResult { list: LogEntry[]; latestId: number; captureLevel: string }
export interface OpsConfig { id?: number; configKey: string; configValue: string; description?: string; updatedBy?: string; updatedAt?: string }
export interface ConfigRevision { revision: string; key: string; action: string; operator: string; timestamp: string; existed: boolean; canRollback: boolean; previousValue?: string; previousDescription?: string }
export interface DeliveryReceipt { id: string; alarmId: number; channel: string; attempts: number; message: string; level: string; createdAt: string; updatedAt: string; status: 'UNCONFIGURED'|'SENDING'|'ACCEPTED'|'FAILED'; detail: string; retryable: boolean; httpStatus?: number }

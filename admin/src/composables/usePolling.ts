import { onBeforeUnmount, onMounted, ref, unref, watch, type Ref } from 'vue'

/** One request at a time. Retains the last successful view on transient failures. */
export function usePolling(task: () => Promise<unknown>, options: { interval?: number; enabled?: Ref<boolean> | boolean; immediate?: boolean; runWhenDisabled?: boolean } = {}) {
  const loading = ref(false)
  const error = ref('')
  const updatedAt = ref<Date | null>(null)
  const stale = ref(false)
  let timer: ReturnType<typeof setTimeout> | undefined
  let active: Promise<void> | null = null
  let stopped = false
  let failures = 0
  const interval = options.interval ?? 15000
  const enabled = () => unref(options.enabled) !== false
  function schedule() {
    clearTimeout(timer)
    if (!stopped && enabled() && !document.hidden && interval > 0) {
      timer = setTimeout(() => { void refresh() }, Math.min(interval * 2 ** failures, 120000))
    }
  }
  function refresh(): Promise<void> {
    if (stopped) return Promise.resolve()
    if (active) return active
    clearTimeout(timer)
    loading.value = true
    active = (async () => {
      try {
        await task()
        if (stopped) return
        updatedAt.value = new Date()
        error.value = ''
        stale.value = false
        failures = 0
      } catch (cause) {
        if (stopped) return
        error.value = cause instanceof Error ? cause.message : '请求失败，请重试'
        stale.value = updatedAt.value !== null
        failures = Math.min(failures + 1, 5)
      } finally {
        loading.value = false
        active = null
        schedule()
      }
    })()
    return active
  }
  function visibility() {
    clearTimeout(timer)
    if (!document.hidden && enabled()) void refresh()
  }
  watch(enabled, () => { if (enabled()) schedule(); else clearTimeout(timer) })
  onMounted(() => {
    document.addEventListener('visibilitychange', visibility)
    if (options.immediate !== false && (enabled() || options.runWhenDisabled) && !document.hidden) void refresh()
  })
  onBeforeUnmount(() => {
    stopped = true
    clearTimeout(timer)
    document.removeEventListener('visibilitychange', visibility)
  })
  return { loading, error, updatedAt, stale, refresh }
}

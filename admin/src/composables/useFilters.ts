import { reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
export function useFilters<T extends Record<string, string | number>>(defaults: T) {
  const route = useRoute()
  const router = useRouter()
  const filters = reactive({ ...defaults }) as T
  for (const key of Object.keys(defaults)) {
    const value = route.query[key]
    if (typeof value === 'string') (filters as any)[key] = typeof defaults[key] === 'number' ? Math.max(1, Number(value) || Number(defaults[key])) : value
  }
  watch(() => route.query, query => {
    for (const key of Object.keys(defaults)) {
      const value = query[key]
      ;(filters as any)[key] = typeof value === 'string' ? (typeof defaults[key] === 'number' ? Math.max(1, Number(value) || Number(defaults[key])) : value) : defaults[key]
    }
  })
  async function persist() {
    const query: Record<string, string> = {}
    for (const [key, value] of Object.entries(filters)) if (value !== '') query[key] = String(value)
    await router.replace({ query })
  }
  return { filters, persist }
}

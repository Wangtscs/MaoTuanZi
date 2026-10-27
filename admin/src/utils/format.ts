export function display(value: unknown, unit = ''): string {
  if (value === null || value === undefined || value === '') return '—'
  if (typeof value === 'number') return Number.isFinite(value) ? `${value.toLocaleString('zh-CN', { maximumFractionDigits: 2 })}${unit}` : '—'
  return `${String(value)}${unit}`
}
export function bytes(value: unknown): string {
  if (value == null || value === '' || !Number.isFinite(Number(value))) return '—'
  const n = Number(value)
  const exponent = n > 0 ? Math.min(Math.floor(Math.log(n) / Math.log(1024)), 4) : 0
  return display(n / 1024 ** exponent, ` ${['B','KB','MB','GB','TB'][exponent]}`)
}

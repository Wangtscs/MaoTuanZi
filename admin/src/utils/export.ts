export function csvCell(value: unknown) {
  let text = value == null ? '' : typeof value === 'object' ? JSON.stringify(value) : String(value)
  if (/^[=+\-@\t\r]/.test(text)) text = `'${text}`
  return `"${text.replace(/"/g, '""')}"`
}
export function exportCsv(filename: string, rows: Record<string, unknown>[]) {
  const keys = [...new Set(rows.flatMap(row => Object.keys(row)))]
  const content = '\uFEFF' + [keys.map(csvCell).join(','), ...rows.map(row => keys.map(key => csvCell(row[key])).join(','))].join('\r\n')
  const url = URL.createObjectURL(new Blob([content], { type: 'text/csv;charset=utf-8;' }))
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = filename.endsWith('.csv') ? filename : `${filename}.csv`
  anchor.click()
  setTimeout(() => URL.revokeObjectURL(url), 0)
}

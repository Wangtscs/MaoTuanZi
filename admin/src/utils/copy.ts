import { ElMessage } from 'element-plus'
export async function copyText(value: unknown) {
  try {
    await navigator.clipboard.writeText(typeof value === 'string' ? value : JSON.stringify(value, null, 2))
    ElMessage.success('已复制')
  } catch { ElMessage.error('复制失败，请选择文字后手动复制') }
}

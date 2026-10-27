import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
export async function confirmAction(message: string): Promise<boolean> {
  try { await ElMessageBox.confirm(message, '确认操作', { type: 'warning', confirmButtonText: '确认', cancelButtonText: '取消', distinguishCancelAndClose: true }); return true }
  catch (cause) { if (cause === 'cancel' || cause === 'close') return false; throw cause }
}
export function useMutation() {
  const pending = ref(false)
  const mutationError = ref('')
  async function mutate(action: () => Promise<unknown>, success = '操作成功') {
    if (pending.value) return false
    pending.value = true
    mutationError.value = ''
    try { await action(); if (success) ElMessage.success(success); return true }
    catch (cause) { mutationError.value = cause instanceof Error ? cause.message : '操作失败，请重试'; ElMessage.error(mutationError.value); return false }
    finally { pending.value = false }
  }
  return { pending, mutationError, mutate }
}

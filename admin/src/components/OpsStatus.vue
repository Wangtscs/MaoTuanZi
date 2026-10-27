<template><el-tag :type="kind" effect="light" size="small">{{ label }}</el-tag></template>
<script setup lang="ts">
import { computed } from 'vue'
const props = defineProps<{ value?: string | number | boolean | null }>()
const states: Record<string, [string, 'success' | 'danger' | 'warning' | 'info']> = {
  ACCEPTED:['接收方已接受','success'],UNCONFIGURED:['未配置渠道','info'],SENDING:['投递中','warning'],UP:['在线','success'],DOWN:['离线','danger'],UNKNOWN:['未知','info'],SUCCESS:['执行成功','success'],FAILED:['执行失败','danger'],RUNNING:['执行中','warning'],SKIPPED:['已跳过','info'],FIRING:['告警中','danger'],RESOLVED:['已恢复','success'],P0:['P0 严重','danger'],P1:['P1 警告','warning'],P2:['P2 提示','info'],ERROR:['ERROR','danger'],WARN:['WARN','warning'],RUNNABLE:['运行中','success'],BLOCKED:['阻塞','danger'],pass:['通过','success'],block:['拦截','danger'],report:['复核','warning']
}
const label = computed(() => states[String(props.value)]?.[0] ?? (props.value == null ? '未知' : String(props.value)))
const kind = computed(() => states[String(props.value)]?.[1] ?? 'info')
</script>

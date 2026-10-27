<template>
  <div class="ops-table-wrap">
    <el-table :data="rows" stripe :max-height="620" :row-key="rowKey" empty-text="暂无记录">
      <el-table-column v-for="column in columns" :key="column.key" :label="column.label" :min-width="column.width || 135" :prop="column.key" :sortable="column.sortable" show-overflow-tooltip>
        <template #default="{ row }"><OpsStatus v-if="column.status" :value="row[column.key]" /><span v-else>{{ column.format ? column.format(row[column.key], row) : display(row[column.key]) }}</span></template>
      </el-table-column>
      <el-table-column label="操作" :min-width="actionWidth || 110" fixed="right">
        <template #default="{ row }"><el-button link type="primary" @click="detail = row">详情</el-button><slot name="actions" :row="row" /></template>
      </el-table-column>
    </el-table>
    <el-drawer :model-value="!!detail" title="记录详情" size="min(600px, 94vw)" @close="detail=null">
      <template v-if="detail"><el-button @click="copyText(detail)">复制详情</el-button><el-descriptions :column="1" border style="margin-top:16px"><el-descriptions-item v-for="(value, key) in detail" :key="key" :label="String(key)"><pre class="detail-value">{{ typeof value === 'object' && value !== null ? JSON.stringify(value,null,2) : display(value) }}</pre></el-descriptions-item></el-descriptions></template>
    </el-drawer>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import OpsStatus from './OpsStatus.vue'
import { display } from '@/utils/format'
import { copyText } from '@/utils/copy'
defineProps<{ rows: any[]; columns: { key: string; label: string; width?: number; status?: boolean; sortable?: boolean; format?: (value: any,row: any) => string }[]; actionWidth?: number }>()
const detail = ref<any>(null)
function rowKey(row: Record<string, unknown>) { return row.id ?? row.instanceId ?? row.revision ?? row.key ?? row.tableName ?? row.prefix ?? row.bizType ?? row.userId ?? row.jobCode ?? JSON.stringify(row) }
</script>
<style scoped>.ops-table-wrap{min-width:0;overflow:auto}.detail-value{white-space:pre-wrap;word-break:break-word;font:inherit;margin:0;max-height:320px;overflow:auto}</style>

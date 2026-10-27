<template>
  <div class="page-state" aria-live="polite" :aria-busy="loading">
    <el-alert v-if="error" :title="stale ? '数据更新失败，当前显示上次成功结果' : '数据加载失败'" :description="error" type="error" :closable="false" show-icon>
      <el-button size="small" :loading="loading" @click="$emit('retry')">重新加载</el-button>
    </el-alert>
    <el-skeleton v-else-if="loading && !updatedAt" :rows="4" animated />
    <el-empty v-else-if="empty" description="当前条件下暂无数据" :image-size="64" />
    <span v-if="updatedAt" class="muted state-time">最近更新 {{ new Date(updatedAt).toLocaleTimeString() }}{{ loading ? ' · 更新中' : '' }}</span>
  </div>
</template>
<script setup lang="ts">
defineProps<{ loading?: boolean; error?: string; empty?: boolean; stale?: boolean; updatedAt?: Date | string | null }>()
defineEmits<{ retry: [] }>()
</script>
<style scoped>.page-state{margin:12px 0}.state-time{display:block;font-size:12px;margin-top:8px}.el-button{margin-top:8px}</style>

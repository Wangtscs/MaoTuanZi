<template>
  <div>
    <h2 class="page-title">仪表盘</h2>
    <el-row :gutter="16">
      <el-col :span="6"><div class="stat-card"><div class="icon">👥</div><div><div class="value">{{ formatCount(stats.user_total) }}</div><div class="label">用户总数</div></div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="icon">📦</div><div><div class="value">{{ formatCount(stats.paid_order_count) }}</div><div class="label">已支付订单</div></div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="icon">💰</div><div><div class="value">¥{{ formatCents(stats.gmv_cents) }}</div><div class="label">累计 GMV</div></div></div></el-col>
      <el-col :span="6"><div class="stat-card"><div class="icon">🎨</div><div><div class="value">{{ formatCount(stats.work_count) }}</div><div class="label">作品总数</div></div></div></el-col>
    </el-row>
    <el-alert v-if="error" class="load-error" type="error" :closable="false" show-icon>
      <template #title>运营数据暂时无法加载</template>
      <template #default>
        <span>{{ error }}</span>
        <el-button link type="primary" :loading="loading" @click="load">重试</el-button>
      </template>
    </el-alert>
    <div v-if="loading && !hasLoaded" class="loading-note">正在加载运营数据…</div>
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :span="12">
        <div class="page">
          <h3 class="page-title" style="font-size:16px">待处理事项</h3>
          <el-empty v-if="stats.pending_refund == null" description="待处理数据暂不可用" />
          <el-empty v-else-if="stats.pending_refund === 0" description="暂无待处理退款" />
          <div v-else class="pending-list">
            <div class="pending-item">
              <span class="dot" />
              <span>{{ stats.pending_refund }} 个退款申请待审核</span>
              <el-button size="small" type="primary" text @click="$router.push('/refunds')">去处理</el-button>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="page">
          <h3 class="page-title" style="font-size:16px">系统提示</h3>
          <ul class="hint-list">
            <li>运营数据按后台接口实时返回</li>
            <li>第三方服务状态以实际配置与回执为准</li>
            <li>退款审核请按工单队列及时处理</li>
          </ul>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { dashboardApi } from "@/api";
const stats = reactive<any>({});
const loading = ref(false);
const hasLoaded = ref(false);
const error = ref("");
async function load() {
  loading.value = true;
  error.value = "";
  try {
    Object.assign(stats, await dashboardApi.data());
    hasLoaded.value = true;
  } catch {
    error.value = "请检查网络或服务状态后重试。";
  } finally {
    loading.value = false;
  }
}
function formatCount(value?: number) {
  return typeof value === "number" && Number.isFinite(value) ? value.toLocaleString("zh-CN") : "—";
}
function formatCents(value?: number) {
  return typeof value === "number" && Number.isFinite(value) ? (value / 100).toFixed(2) : "—";
}
onMounted(load);
</script>

<style scoped>
.pending-list { display: flex; flex-direction: column; gap: 12px; }
.load-error { margin-top: 16px; }
.loading-note { margin-top: 16px; color: var(--color-text-secondary); font-size: 14px; }
.pending-item { display: flex; align-items: center; gap: 12px; padding: 12px 16px; background: #FFF8F0; border-radius: 10px; font-size: 14px; }
.dot { width: 8px; height: 8px; border-radius: 50%; background: var(--color-warning); }
.hint-list { margin: 0; padding-left: 20px; color: var(--color-text-secondary); line-height: 2; }
</style>

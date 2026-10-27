<template>
  <div class="page">
    <h2 class="page-title">退款审核</h2>
    <div class="toolbar">
      <el-select v-model="filter.status" placeholder="状态" clearable style="width:160px" @change="reload">
        <el-option label="待审核" :value="0" />
        <el-option label="退款中" :value="1" />
        <el-option label="审核拒绝" :value="2" />
        <el-option label="已退款" :value="3" />
      </el-select>
      <div class="spacer" />
    </div>
    <el-alert v-if="error" type="error" :closable="false" show-icon class="state-alert">
      <template #title>退款申请加载失败</template>
      <template #default>
        <span>{{ error }}</span>
        <el-button link type="primary" :loading="loading" @click="reload">重试</el-button>
      </template>
    </el-alert>
    <el-alert v-if="actionError" type="error" :closable="false" show-icon class="state-alert">
      <template #title>{{ actionError }}</template>
    </el-alert>
    <el-empty v-if="!loading && !error && list.length === 0" description="暂无符合条件的退款申请" />
    <el-table v-else :data="list" v-loading="loading" border>
      <el-table-column prop="refundNo" label="退款单号" width="200" />
      <el-table-column prop="orderId" label="订单" width="100" />
      <el-table-column prop="userId" label="用户" width="100" />
      <el-table-column label="金额" width="120"><template #default="{ row }">¥{{ (row.amountCents / 100).toFixed(2) }}</template></el-table-column>
      <el-table-column prop="reason" label="原因" show-overflow-tooltip />
      <el-table-column label="状态" width="120"><template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ statusName(row.status) }}</el-tag></template></el-table-column>
      <el-table-column label="申请时间" width="160"><template #default="{ row }">{{ formatDate(row.createdAt) }}</template></el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 0">
            <el-button size="small" type="success" :loading="submittingId === row.id && submittingAction === 'approve'" :disabled="submittingId !== null" @click="review(row, true)">通过</el-button>
            <el-button size="small" type="danger" :loading="submittingId === row.id && submittingAction === 'reject'" :disabled="submittingId !== null" @click="review(row, false)">拒绝</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="filter.page" v-model:page-size="filter.pageSize" :total="total" :page-sizes="[20, 50]" layout="total, sizes, prev, pager, next" style="margin-top:16px;justify-content:flex-end" @current-change="reload" @size-change="reload" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { orderApi } from "@/api";
const list = ref<any[]>([]);
const total = ref(0);
const loading = ref(false);
const error = ref("");
const actionError = ref("");
const submittingId = ref<number | null>(null);
const submittingAction = ref<"approve" | "reject" | null>(null);
const filter = reactive({ status: null as number | null, page: 1, pageSize: 20 });
async function reload() {
  loading.value = true;
  error.value = "";
  try {
    const data: any = await orderApi.refundList(filter);
    list.value = data.list || [];
    total.value = data.pagination?.total ?? data.total ?? list.value.length;
  } catch {
    error.value = "请检查网络或服务状态后重试。已加载的数据仍保留在列表中。";
  } finally {
    loading.value = false;
  }
}
async function review(row: any, approve: boolean) {
  if (submittingId.value !== null) return;
  let remark: string;
  try {
    const result = await ElMessageBox.prompt(
      approve ? "请填写审核备注，确认后会提交审核结果。" : "请填写拒绝原因，确认后会提交审核结果。",
      approve ? "审核通过" : "审核拒绝",
      { inputType: "textarea", confirmButtonText: "确认提交", cancelButtonText: "取消" }
    );
    remark = result.value.trim();
    if (!remark) {
      ElMessage.warning(approve ? "请填写审核备注" : "请填写拒绝原因");
      return;
    }
  } catch {
    // Element Plus rejects the promise when the operator cancels; cancellation has no API side effect.
    return;
  }
  submittingId.value = row.id;
  submittingAction.value = approve ? "approve" : "reject";
  actionError.value = "";
  try {
    await orderApi.reviewRefund(row.id, { approve, remark });
    ElMessage.success(approve ? "审核已通过" : "已拒绝退款申请");
    await reload();
  } catch {
    actionError.value = "审核未能确认，请刷新列表核对退款状态后再操作。";
  } finally {
    submittingId.value = null;
    submittingAction.value = null;
  }
}
function statusName(s: number) { return ["待审核", "退款中", "审核拒绝", "已退款"][s] ?? "-"; }
function statusType(s: number) { return ["warning", "info", "danger", "success"][s] ?? ""; }
function formatDate(s: string) { return s ? new Date(s).toLocaleString("zh-CN") : "-"; }
onMounted(reload);
</script>

<style scoped>
.state-alert { margin-bottom: 16px; }
</style>

<template>
  <div class="page">
    <h2 class="page-title">订单管理</h2>
    <div class="toolbar">
      <el-input v-model="filter.keyword" placeholder="订单号/用户ID" clearable style="width:280px" @clear="reload" @keyup.enter="reload" />
      <el-select v-model="filter.status" placeholder="状态" clearable style="width:160px" @change="reload">
        <el-option label="待支付" :value="0" />
        <el-option label="已支付" :value="1" />
        <el-option label="已完成" :value="2" />
        <el-option label="已关闭" :value="3" />
        <el-option label="退款中" :value="4" />
        <el-option label="已退款" :value="5" />
      </el-select>
      <div class="spacer" />
      <el-button type="primary" @click="reload">查询</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column prop="userId" label="用户" width="80" />
      <el-table-column prop="productName" label="商品" />
      <el-table-column label="金额" width="120"><template #default="{ row }">¥{{ (row.payAmountCents / 100).toFixed(2) }}</template></el-table-column>
      <el-table-column label="状态" width="120"><template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ statusName(row.status) }}</el-tag></template></el-table-column>
      <el-table-column prop="wxTransactionId" label="微信交易号" width="180" show-overflow-tooltip />
      <el-table-column label="支付时间" width="160"><template #default="{ row }">{{ formatDate(row.paidAt) }}</template></el-table-column>
      <el-table-column label="创建时间" width="160"><template #default="{ row }">{{ formatDate(row.createdAt) }}</template></el-table-column>
    </el-table>
    <el-pagination v-model:current-page="filter.page" v-model:page-size="filter.pageSize" :total="total" :page-sizes="[20, 50]" layout="total, sizes, prev, pager, next" style="margin-top:16px;justify-content:flex-end" @current-change="reload" @size-change="reload" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { orderApi } from "@/api";
const list = ref<any[]>([]);
const total = ref(0);
const loading = ref(false);
const filter = reactive({ keyword: "", status: null as number | null, page: 1, pageSize: 20 });
async function reload() { loading.value = true; try { const data: any = await orderApi.list(filter); list.value = data.list || []; total.value = data.pagination?.total || 0; } finally { loading.value = false; } }
function statusName(s: number) { return ["待支付", "已支付", "已完成", "已关闭", "退款中", "已退款", "拒绝"][s] ?? "-"; }
function statusType(s: number) { return ["warning", "success", "success", "info", "warning", "danger", "info"][s] ?? ""; }
function formatDate(s: string) { return s ? new Date(s).toLocaleString("zh-CN") : "-"; }
onMounted(reload);
</script>
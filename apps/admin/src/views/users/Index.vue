<template>
  <div class="page">
    <h2 class="page-title">用户管理</h2>
    <div class="toolbar">
      <el-input v-model="filter.keyword" placeholder="昵称/手机号/OpenID" clearable style="width:280px" @clear="reload" @keyup.enter="reload" />
      <el-select v-model="filter.status" placeholder="状态" clearable style="width:140px" @change="reload">
        <el-option label="正常" :value="0" />
        <el-option label="封禁" :value="1" />
        <el-option label="注销" :value="2" />
      </el-select>
      <div class="spacer" />
      <el-button type="primary" @click="reload">查询</el-button>
    </div>
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column type="index" label="#" width="60" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="头像" width="80">
        <template #default="{ row }"><el-avatar :src="row.avatarUrl" :size="36">{{ row.nickname?.[0] }}</el-avatar></template>
      </el-table-column>
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="openid" label="OpenID" width="200" show-overflow-tooltip />
      <el-table-column label="会员" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.vipLevel > 0" type="warning" size="small">{{ vipName(row.vipLevel) }}</el-tag>
          <span v-else style="color:var(--color-text-tertiary)">普通</span>
        </template>
      </el-table-column>
      <el-table-column label="到期时间" width="160">
        <template #default="{ row }">{{ formatDate(row.vipExpireAt) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ statusName(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="160">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" size="small" type="danger" @click="ban(row)">封禁</el-button>
          <el-button v-else-if="row.status === 1" size="small" type="success" @click="unban(row)">解封</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="filter.page" v-model:page-size="filter.pageSize" :total="total" :page-sizes="[20, 50, 100]" layout="total, sizes, prev, pager, next" style="margin-top:16px;justify-content:flex-end" @current-change="reload" @size-change="reload" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { userApi } from "@/api";

const list = ref<any[]>([]);
const total = ref(0);
const loading = ref(false);
const filter = reactive({ keyword: "", status: null as number | null, page: 1, pageSize: 20 });

async function reload() {
  loading.value = true;
  try {
    const data: any = await userApi.list(filter);
    list.value = data.list || [];
    total.value = data.pagination?.total || 0;
  } finally { loading.value = false; }
}
async function ban(row: any) {
  await ElMessageBox.confirm(`确定封禁「${row.nickname}」？`, "提示", { type: "warning" });
  await userApi.ban(row.id);
  ElMessage.success("已封禁");
  reload();
}
async function unban(row: any) {
  await ElMessageBox.confirm(`确定解封「${row.nickname}」？`, "提示", { type: "warning" });
  await userApi.unban(row.id);
  ElMessage.success("已解封");
  reload();
}
function statusName(s: number) { return ["正常", "封禁", "注销"][s] ?? "-"; }
function statusType(s: number) { return ["success", "danger", "info"][s] ?? ""; }
function vipName(v: number) { return ["", "月卡", "季卡", "年卡"][v] ?? "-"; }
function formatDate(s: string) { return s ? new Date(s).toLocaleString("zh-CN") : "-"; }
onMounted(reload);
</script>
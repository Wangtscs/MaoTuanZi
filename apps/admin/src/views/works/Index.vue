<template>
  <div class="page">
    <h2 class="page-title">作品审核</h2>
    <div class="toolbar">
      <el-select v-model="filter.status" placeholder="状态" clearable style="width:160px" @change="reload">
        <el-option label="生成中" :value="0" />
        <el-option label="预览态" :value="1" />
        <el-option label="已完成" :value="2" />
        <el-option label="已删除" :value="3" />
      </el-select>
      <div class="spacer" />
    </div>
    <el-table :data="list" v-loading="loading" border>
      <el-table-column type="index" label="#" width="60" />
      <el-table-column label="预览" width="120">
        <template #default="{ row }"><el-image :src="row.previewUrl" :preview-src-list="[row.previewUrl]" style="width:80px;height:80px;border-radius:8px" fit="cover" /></template>
      </el-table-column>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="用户" width="80" />
      <el-table-column prop="petId" label="宠物" width="80" />
      <el-table-column prop="workType" label="类型" width="120" />
      <el-table-column prop="title" label="标题" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }"><el-tag :type="statusType(row.status)" size="small">{{ statusName(row.status) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="公开" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.isPublic === 1" type="success" size="small">公开</el-tag>
          <el-tag v-else type="info" size="small">私密</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="160">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="filter.page" v-model:page-size="filter.pageSize" :total="total" :page-sizes="[20, 50]" layout="total, sizes, prev, pager, next" style="margin-top:16px;justify-content:flex-end" @current-change="reload" @size-change="reload" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { workApi } from "@/api";
const list = ref<any[]>([]);
const total = ref(0);
const loading = ref(false);
const filter = reactive({ status: null as number | null, page: 1, pageSize: 20 });
async function reload() {
  loading.value = true;
  try { const data: any = await workApi.list(filter); list.value = data.list || []; total.value = data.pagination?.total || 0; }
  finally { loading.value = false; }
}
function statusName(s: number) { return ["生成中", "预览态", "已完成", "已删除"][s] ?? "-"; }
function statusType(s: number) { return ["warning", "info", "success", "danger"][s] ?? ""; }
function formatDate(s: string) { return s ? new Date(s).toLocaleString("zh-CN") : "-"; }
onMounted(reload);
</script>
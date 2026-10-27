<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? ''64px'' : ''220px''" class="sidebar">
      <div class="logo">
        <div class="logo-icon">🐾</div>
        <span v-if="!collapsed" class="logo-text">毛团子AI</span>
      </div>
      <el-menu :default-active="activeMenu" :collapse="collapsed" router class="menu">
        <el-menu-item index="/dashboard"><el-icon><Odometer /></el-icon><template #title>仪表盘</template></el-menu-item>
        <el-menu-item index="/users"><el-icon><User /></el-icon><template #title>用户管理</template></el-menu-item>
        <el-menu-item index="/works"><el-icon><Picture /></el-icon><template #title>作品审核</template></el-menu-item>
        <el-menu-item index="/orders"><el-icon><Tickets /></el-icon><template #title>订单管理</template></el-menu-item>
        <el-menu-item index="/refunds"><el-icon><RefreshLeft /></el-icon><template #title>退款审核</template></el-menu-item>
        <el-menu-item index="/vip"><el-icon><Star /></el-icon><template #title>会员配置</template></el-menu-item>
        <el-menu-item index="/coupons"><el-icon><Discount /></el-icon><template #title>卡券管理</template></el-menu-item>
        <el-menu-item index="/content"><el-icon><ChatLineSquare /></el-icon><template #title>内容审核</template></el-menu-item>
        <el-menu-item index="/sensitive"><el-icon><Warning /></el-icon><template #title>敏感词</template></el-menu-item>
        <el-menu-item index="/statistics"><el-icon><DataAnalysis /></el-icon><template #title>数据统计</template></el-menu-item>
        <el-menu-item index="/settings"><el-icon><Setting /></el-icon><template #title>系统设置</template></el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-button text @click="collapsed = !collapsed">
            <el-icon><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: ''/'' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" class="avatar">{{ auth.admin?.nickname?.[0] || ''A'' }}</el-avatar>
              <span class="username">{{ auth.admin?.nickname || auth.admin?.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessageBox } from "element-plus";
import { Odometer, User, Picture, Tickets, RefreshLeft, Star, Discount, ChatLineSquare, Warning, DataAnalysis, Setting, Fold, Expand } from "@element-plus/icons-vue";
import { useAuthStore } from "@/stores/auth";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const collapsed = ref(false);
const activeMenu = computed(() => route.path);

async function handleCommand(cmd: string) {
  if (cmd === "logout") {
    await ElMessageBox.confirm("确定退出登录？", "提示", { type: "warning" });
    await auth.logout();
    router.push("/login");
  }
}
</script>

<style scoped>
.layout { height: 100vh; }
.sidebar {
  background: linear-gradient(180deg, #fff 0%, #FFF8F0 100%);
  border-right: 1px solid var(--color-divider);
  transition: width 0.3s;
}
.logo { display: flex; align-items: center; gap: 12px; padding: 20px 16px; border-bottom: 1px solid var(--color-divider); }
.logo-icon {
  width: 36px; height: 36px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 20px;
}
.logo-text { font-weight: 600; font-size: 16px; }
.menu { border-right: none; padding: 8px 0; }
.menu :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, rgba(255,182,193,0.15), rgba(255,170,128,0.05)) !important;
  border-left: 3px solid var(--color-primary);
  color: var(--color-primary) !important;
}
.header {
  background: var(--color-card);
  border-bottom: 1px solid var(--color-divider);
  display: flex; justify-content: space-between; align-items: center;
  padding: 0 24px;
}
.header-left, .header-right { display: flex; align-items: center; gap: 16px; }
.user-info { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.avatar {
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  color: white; font-weight: 600;
}
.main { background: var(--color-bg); padding: 24px; overflow: auto; }
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
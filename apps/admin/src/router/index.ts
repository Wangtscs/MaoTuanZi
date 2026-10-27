import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const routes: RouteRecordRaw[] = [
  { path: "/login", name: "Login", component: () => import("@/views/Login.vue"), meta: { public: true } },
  {
    path: "/",
    component: () => import("@/layouts/MainLayout.vue"),
    redirect: "/dashboard",
    children: [
      { path: "dashboard", name: "Dashboard", component: () => import("@/views/dashboard/Index.vue"), meta: { title: "仪表盘" } },
      { path: "users", name: "Users", component: () => import("@/views/users/Index.vue"), meta: { title: "用户管理" } },
      { path: "works", name: "Works", component: () => import("@/views/works/Index.vue"), meta: { title: "作品审核" } },
      { path: "orders", name: "Orders", component: () => import("@/views/orders/Index.vue"), meta: { title: "订单管理" } },
      { path: "refunds", name: "Refunds", component: () => import("@/views/orders/Refunds.vue"), meta: { title: "退款审核" } },
      { path: "vip", name: "Vip", component: () => import("@/views/vip/Index.vue"), meta: { title: "会员配置" } },
      { path: "coupons", name: "Coupons", component: () => import("@/views/coupons/Index.vue"), meta: { title: "卡券管理" } },
      { path: "content", name: "Content", component: () => import("@/views/content/Index.vue"), meta: { title: "内容审核" } },
      { path: "sensitive", name: "Sensitive", component: () => import("@/views/content/Sensitive.vue"), meta: { title: "敏感词" } },
      { path: "statistics", name: "Statistics", component: () => import("@/views/statistics/Index.vue"), meta: { title: "数据统计" } },
      { path: "settings", name: "Settings", component: () => import("@/views/settings/Index.vue"), meta: { title: "系统设置" } }
    ]
  }
];

const router = createRouter({ history: createWebHistory(), routes });

router.beforeEach((to, from, next) => {
  const auth = useAuthStore();
  if (to.meta.public) return next();
  if (!auth.token) return next("/login");
  next();
});

export default router;
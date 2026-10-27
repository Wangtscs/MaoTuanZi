import axios from "axios";
import { ElMessage } from "element-plus";
import { useAuthStore } from "@/stores/auth";
import router from "@/router";

const api = axios.create({ baseURL: "/", timeout: 15000 });

api.interceptors.request.use(config => {
  const auth = useAuthStore();
  if (auth.token) {
    config.headers["X-Admin-Token"] = `Bearer ${auth.token}`;
    config.headers["Authorization"] = `Bearer ${auth.token}`;
  }
  return config;
});

api.interceptors.response.use(
  res => {
    const data = res.data;
    if (data && typeof data === "object" && "code" in data) {
      if (data.code === 0) return data.data;
      if ([102001, 102002, 102003].includes(data.code)) {
        useAuthStore().logout();
        router.push("/login");
        ElMessage.error("登录已过期");
        return Promise.reject(data);
      }
      ElMessage.error(data.message || "操作失败");
      return Promise.reject(data);
    }
    return data;
  },
  err => {
    ElMessage.error(err.response?.data?.message || err.message || "网络错误");
    return Promise.reject(err);
  }
);

export default api;

export const authApi = {
  login: (username: string, password: string) => api.post("/admin/auth/login", { username, password }),
  logout: () => api.post("/admin/auth/logout"),
  me: () => api.get("/admin/auth/me")
};

export const dashboardApi = {
  data: () => api.get("/admin/dashboard")
};

export const userApi = {
  list: (params: any) => api.get("/admin/users", { params }),
  detail: (id: number) => api.get(`/admin/users/${id}`),
  ban: (id: number) => api.post(`/admin/users/${id}/ban`),
  unban: (id: number) => api.post(`/admin/users/${id}/unban`)
};

export const workApi = {
  list: (params: any) => api.get("/admin/works", { params }),
  review: (id: number, status: number) => api.post(`/admin/works/${id}/review`, { status })
};

export const orderApi = {
  list: (params: any) => api.get("/admin/orders", { params }),
  detail: (id: number) => api.get(`/admin/orders/${id}`),
  refundList: (params: any) => api.get("/admin/orders/refunds", { params }),
  reviewRefund: (id: number, body: any) => api.post(`/admin/orders/refunds/${id}/review`, body)
};

export const contentApi = {
  posts: (params: any) => api.get("/admin/content/posts", { params }),
  offline: (id: number) => api.post(`/admin/content/posts/${id}/offline`),
  sensitiveWords: () => api.get("/admin/content/sensitive-words"),
  addWord: (word: any) => api.post("/admin/content/sensitive-words", word),
  deleteWord: (id: number) => api.delete(`/admin/content/sensitive-words/${id}`)
};

export const settingsApi = {
  configs: () => api.get("/admin/settings/configs"),
  updateConfig: (key: string, value: string) =>
    api.put(`/admin/settings/configs/${key}`, { value })
};
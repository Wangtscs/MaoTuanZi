import { defineStore } from "pinia";
import { authApi } from "@/api";

interface AdminInfo {
  id: number;
  username: string;
  nickname: string;
  permissions: string[];
  roles: string[];
}

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: localStorage.getItem("admin_token") || "",
    admin: JSON.parse(localStorage.getItem("admin_info") || "null") as AdminInfo | null
  }),
  actions: {
    async login(username: string, password: string) {
      const data: any = await authApi.login(username, password);
      this.token = data.token;
      this.admin = data.admin;
      localStorage.setItem("admin_token", data.token);
      localStorage.setItem("admin_info", JSON.stringify(data.admin));
    },
    async logout() {
      try { await authApi.logout(); } catch (e) {}
      this.token = "";
      this.admin = null;
      localStorage.removeItem("admin_token");
      localStorage.removeItem("admin_info");
    },
    hasPerm(code: string) {
      return this.admin?.permissions?.includes(code) ?? false;
    }
  }
});
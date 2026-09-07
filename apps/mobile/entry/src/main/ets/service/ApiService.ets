/**
 * 业务 API 封装
 */
import { HttpClient } from "./HttpClient";

const http = HttpClient.getInstance();

export class ApiService {
  // 认证
  static wxLogin(code: string, nickname?: string, avatarUrl?: string, agreementVersion?: string) {
    return http.post("/v1/auth/wx-login", { code, nickname, avatarUrl, agreementVersion });
  }

  static refresh(refreshToken: string) {
    return http.post("/v1/auth/refresh", { refresh_token: refreshToken });
  }

  static logout() {
    return http.post("/v1/auth/logout");
  }

  static me() {
    return http.get("/v1/users/me");
  }

  static updateMe(patch: any) {
    return http.patch("/v1/users/me", patch);
  }

  static cancelAccount(reason: string) {
    return http.post("/v1/users/me/cancel", { reason });
  }

  // 宠物
  static listPets() { return http.get("/v1/pets"); }
  static getPet(id: number) { return http.get(`/v1/pets/${id}`); }
  static createPet(data: any) { return http.post("/v1/pets", data); }
  static updatePet(id: number, data: any) { return http.patch(`/v1/pets/${id}`, data); }
  static deletePet(id: number) { return http.delete(`/v1/pets/${id}`); }

  // 品种
  static listBreeds(species?: number) { return http.get("/v1/breeds", { species }); }

  // 商品
  static listProducts(type?: number) { return http.get("/v1/products", { type }); }
  static getProduct(id: string) { return http.get(`/v1/products/${id}`); }

  // AI
  static listStyles(category?: string) { return http.get("/v1/ai/styles", { category }); }
  static listTemplates(category?: string) { return http.get("/v1/ai/templates", { category }); }
  static createAiTask(data: any) { return http.post("/v1/ai/tasks", data); }
  static getAiTask(id: number) { return http.get(`/v1/ai/tasks/${id}`); }
  static cancelAiTask(id: number) { return http.post(`/v1/ai/tasks/${id}/cancel`); }
  static getWork(id: number) { return http.get(`/v1/ai/works/${id}`); }
  static getWorkResult(id: number) { return http.get(`/v1/ai/works/${id}/result`); }

  // 订单
  static createOrder(data: any) { return http.post("/v1/orders", data); }
  static getOrder(id: number) { return http.get(`/v1/orders/${id}`); }
  static listOrders(status?: number, page: number = 1, pageSize: number = 20) {
    return http.get("/v1/orders", { status, page, pageSize });
  }
  static closeOrder(id: number) { return http.post(`/v1/orders/${id}/close`); }

  // 作品
  static listWorks(status?: number, page: number = 1, pageSize: number = 20) {
    return http.get("/v1/works", { status, page, pageSize });
  }
  static unlockWork(id: number) { return http.post(`/v1/works/${id}/unlock`); }
  static deleteWork(id: number) { return http.delete(`/v1/works/${id}`); }

  // 工具
  static listReminders(petId?: number) { return http.get("/v1/tools/health-reminders", { petId }); }
  static createReminder(data: any) { return http.post("/v1/tools/health-reminders", data); }
  static updateReminder(id: number, data: any) { return http.patch(`/v1/tools/health-reminders/${id}`, data); }
  static deleteReminder(id: number) { return http.delete(`/v1/tools/health-reminders/${id}`); }

  static listWeights(petId: number) { return http.get("/v1/tools/weights", { petId }); }
  static addWeight(data: any) { return http.post("/v1/tools/weights", data); }

  static listJournal(petId: number, page: number = 1, pageSize: number = 20) {
    return http.get("/v1/tools/journal", { petId, page, pageSize });
  }
  static addJournal(data: any) { return http.post("/v1/tools/journal", data); }
  static updateJournal(id: number, data: any) { return http.patch(`/v1/tools/journal/${id}`, data); }
  static deleteJournal(id: number) { return http.delete(`/v1/tools/journal/${id}`); }

  static listFeedings(petId: number, limit: number = 20) {
    return http.get("/v1/tools/feedings", { petId, limit });
  }
  static addFeeding(data: any) { return http.post("/v1/tools/feedings", data); }

  static generateFeedingPlan(petId: number) { return http.post("/v1/tools/feeding-plan", { petId }); }
  static getFeedingPlan(petId: number) { return http.get("/v1/tools/feeding-plan", { petId }); }
  static querySymptom(keyword: string) { return http.get("/v1/tools/symptom", { keyword }); }

  // 广场
  static listPosts(topic?: string, page: number = 1, pageSize: number = 20) {
    return http.get("/v1/square/posts", { topic, page, pageSize });
  }
  /** GET /v1/square/posts?tab=latest|hottest|3d_zone|challenge（阶段5：tab 语义对齐） */
  static listPostsByTab(tab: string, page: number = 1, pageSize: number = 20) {
    return http.get("/v1/square/posts", { tab, page, pageSize });
  }
  static getPost(id: number) { return http.get(`/v1/square/posts/${id}`); }
  static publishPost(data: any) { return http.post("/v1/square/posts", data); }
  static deletePost(id: number) { return http.delete(`/v1/square/posts/${id}`); }
  static likePost(id: number) { return http.post(`/v1/square/posts/${id}/like`); }
  static unlikePost(id: number) { return http.delete(`/v1/square/posts/${id}/like`); }
  static reportPost(id: number, type: number, reason: string) {
    return http.post(`/v1/square/posts/${id}/report`, { type, reason });
  }
  static getComments(id: number) { return http.get(`/v1/square/posts/${id}/comments`); }
  static addComment(id: number, content: string) {
    return http.post(`/v1/square/posts/${id}/comments`, { content });
  }

  // 通知
  static listNotifications(page: number = 1, pageSize: number = 20) {
    return http.get("/v1/notifications", { page, pageSize });
  }
  static getUnreadCount() { return http.get("/v1/notifications/unread-count"); }
  static markRead(id: number) { return http.post(`/v1/notifications/${id}/read`); }
  static markAllRead() { return http.post("/v1/notifications/read-all"); }

  // 活动
  static getActivityCountdown() {
    return http.get("/v1/activities/countdown");
  }

  // 文件上传
  static uploadImage(fileData: string, filename: string) {
    return http.upload("/v1/uploads/image", { uri: fileData, name: filename, type: "image/jpeg" } as any);
  }

  // ===================== 商业化 B 类接口 =====================

  // --- 会员 / VIP ---
  /** GET /v1/vip/me → {vipLevel, vipExpireAt, benefits[]} */
  static vipMe() {
    return http.get("/v1/vip/me");
  }

  /** GET /v1/vip/plans → [{id, productType, name, priceCents, durationDays, tag}] */
  static vipPlans() {
    return http.get("/v1/vip/plans");
  }

  // --- 统一下单（支付核心）---
  /** POST /v1/orders/unified-order → {channel, wxPayParams, iapParams, expireAt} */
  static unifiedOrder(orderId: number, channel: string) {
    return http.post("/v1/orders/unified-order", { orderId, channel });
  }

  /** POST /v1/orders/{id}/refund → {refundId, status}（预留：支付成功后可申请退款） */
  static orderRefund(id: number) {
    return http.post(`/v1/orders/${id}/refund`);
  }

  // --- 卡券 ---
  /** POST /v1/coupons/claim → {id, status:"CLAIMED"} */
  static couponClaim(couponId: string) {
    return http.post("/v1/coupons/claim", { couponId });
  }

  // --- 分享 / 邀请 / 奖励 ---
  /** POST /v1/share/record → {reward, shareCount} */
  static shareRecord(postId: number, channel: string) {
    return http.post("/v1/share/record", { postId, channel });
  }

  /** POST /v1/share/invite → {rewarded:bool} */
  static shareInvite(inviterCode: string, inviteeUserId: number) {
    return http.post("/v1/share/invite", { inviterCode, inviteeUserId });
  }

  /** POST /v1/rewards/claim → {status:"CLAIMED"} */
  static rewardClaim(rewardId: string) {
    return http.post("/v1/rewards/claim", { rewardId });
  }

  /** GET /v1/user/invite → {inviteCode, inviteUrl, rewardRule} */
  static userInvite() {
    return http.get("/v1/user/invite");
  }

  // --- 3D 模型生成 ---
  /** POST /v1/ai/3d/tasks → {taskId, status:"PENDING"} */
  static createModel3dTask(data: { sourceImageUrl: string; style: string }) {
    return http.post("/v1/ai/3d/tasks", data);
  }

  /** GET /v1/ai/3d/tasks/{id} → {taskId, status, modelUrl} */
  static getModel3dTask(id: number | string) {
    return http.get(`/v1/ai/3d/tasks/${id}`);
  }
}
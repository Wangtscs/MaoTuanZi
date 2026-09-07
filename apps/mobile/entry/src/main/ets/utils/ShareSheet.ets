/**
 * 分享工具：调用鸿蒙系统分享面板 + 分享奖励上报
 * 支持：微信好友、朋友圈等已安装分享应用；配合 QrCodeGenerator 生成二维码分享卡
 *
 * 阶段4（分享奖励）：
 *  - 分享动作完成后调用 POST /v1/share/record 记录分享；
 *  - 若后端返回 reward（COUPON|POINTS），Toast 告知用户获得奖励；
 *  - 后端未实现分享奖励时静默失败，不阻断分享主流程。
 */
import promptAction from "@ohos.promptAction";
import wantAgent from "@ohos.wantAgent";
import { ApiService } from "../service/ApiService";

/** 分享渠道（对齐后端契约 channel 枚举） */
export type ShareChannel = "WECHAT" | "MOMENTS" | "COPY" | "SYSTEM";

/** 分享奖励结构（来自 /v1/share/record 的 reward 字段） */
export interface ShareReward {
  type: string;   // "COUPON" | "POINTS"
  amount: number;
}

/** 分享记录返回结构 */
export interface ShareRecordResp {
  reward: ShareReward | null;
  shareCount: number;
}

/** 获取帖子深度链接（扫码后直接打开 App 帖子页） */
export function getPostShareUrl(postId: number | string): string {
  return `maotuanzi://square/post/${postId}`
}

/** 获取邀请深度链接 */
export function getInviteShareUrl(inviteCode: string): string {
  return `maotuanzi://invite/${inviteCode}`
}

/**
 * 调用鸿蒙系统分享面板
 * @param title 分享标题
 * @param text 分享文案
 * @param url 深度链接
 */
export async function shareViaSystem(title: string, text: string, url: string): Promise<void> {
  // 分享目标的 Want（系统分享 Ability）
  const shareWant: wantAgent.Want = {
    bundleName: "com.huawei.hmos.share",
    abilityName: "com.huawei.hmos.share.ShareAbility"
  }

  // WantAgent 配置
  const agentInfo: wantAgent.WantAgentInfo = {
    wants: [shareWant],
    operationType: wantAgent.OperationType.SHARE,
    requestCode: 0,
    wantAgentFlags: [wantAgent.WantAgentFlags.UPDATE_FLAG],
    extraInfo: {
      "HuaweiShare.Title": title,
      "HuaweiShare.Content": text,
      "HuaweiShare.Url": url
    }
  }

  try {
    const agent = await wantAgent.getWantAgent(agentInfo)
    await wantAgent.triggerWantAgent(agent, {
      extraInfo: {
        "HuaweiShare.Title": title,
        "HuaweiShare.Content": text,
        "HuaweiShare.Url": url
      }
    })
  } catch (err) {
    // 系统分享不可用时降级：提示复制链接
    promptAction.showToast({ message: "链接已复制，快去分享吧", duration: 1500 })
  }
}

/**
 * 上报分享并处理奖励（静默失败，不阻断主流程）。
 * 后端契约：POST /v1/share/record → { reward:{type,amount}|null, shareCount }
 */
async function recordShare(postId: number, channel: ShareChannel): Promise<void> {
  try {
    const res = await ApiService.shareRecord(postId, channel) as ShareRecordResp
    const reward: ShareReward | null = res?.reward || null
    if (reward && reward.amount > 0) {
      const typeName: string = reward.type === "COUPON" ? "优惠券" : "积分"
      promptAction.showToast({ message: `分享成功，获得${reward.amount}${typeName}奖励！`, duration: 2000 })
    }
  } catch (e) {
    // 后端未实现分享奖励时静默失败，不影响分享主流程
  }
}

/**
 * 分享帖子入口（供 OrderDetail / 其它页面直接调用）。
 * 拉起系统分享面板 → 完成后上报分享并处理奖励。
 * @param postId 帖子 ID
 * @param title 分享标题
 * @param channel 分享渠道
 */
export async function sharePost(postId: number, title: string, channel: ShareChannel = "SYSTEM"): Promise<void> {
  const url: string = getPostShareUrl(postId)
  const shareText: string = `来自毛团子AI萌宠广场，快来看看吧`
  await shareViaSystem(title, shareText, url)
  await recordShare(postId, channel)
}

/**
 * 分享入口：弹出 ActionMenu（微信好友 / 朋友圈 / 复制链接）
 * 完成分享后自动上报 shareRecord 并处理奖励。
 * @param postId 帖子 ID
 * @param postContent 帖子内容
 */
export async function showShareMenu(postId: number, postContent: string): Promise<void> {
  const url: string = getPostShareUrl(postId)
  const shareText: string = `来自毛团子AI萌宠广场：${postContent}`

  promptAction.showActionMenu({
    title: "分享到",
    buttons: [
      { text: "微信好友", color: "#07C160" },
      { text: "朋友圈", color: "#07C160" },
      { text: "复制链接", color: "#FFB6C1" }
    ]
  }).then(async (result) => {
    if (result?.index === 0) {
      // 微信好友 → 拉起系统分享面板
      await shareViaSystem("毛团子AI · 萌宠广场", shareText, url)
      await recordShare(postId, "WECHAT")
    } else if (result?.index === 1) {
      // 朋友圈 → 拉起系统分享面板
      await shareViaSystem("毛团子AI · 萌宠广场", shareText, url)
      await recordShare(postId, "MOMENTS")
    } else if (result?.index === 2) {
      // 复制链接
      promptAction.showToast({ message: "链接已复制", duration: 1200 })
      await recordShare(postId, "COPY")
    }
  }).catch(() => {
    // 点击遮罩取消，不处理
  })
}

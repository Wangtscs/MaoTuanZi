package com.maotuanzi.common.enums;

/**
 * 错误码定义（与 docs/设计/接口错误码清单.md 对齐）
 */
public enum ErrorCode {

    SUCCESS(0, 200, "成功"),

    // 通用 1xxxxx
    SYSTEM_ERROR(100001, 500, "服务开小差了，请稍后再试"),
    SYSTEM_MAINTENANCE(100002, 503, "服务维护中"),
    GATEWAY_TIMEOUT(100003, 504, "网关超时"),
    PARAM_INVALID(101001, 400, "参数错误"),
    PARAM_MISSING(101002, 400, "缺少必填参数"),
    PARAM_FORMAT(101003, 400, "参数格式错误"),
    FILE_TOO_LARGE(101004, 400, "文件过大"),
    FILE_TYPE_INVALID(101005, 415, "文件类型不支持"),

    // 鉴权 10xxxx
    UNAUTHORIZED(102001, 401, "请先登录"),
    TOKEN_INVALID(102002, 401, "登录已失效，请重新登录"),
    TOKEN_EXPIRED(102003, 401, "登录已过期"),
    REFRESH_TOKEN_INVALID(102004, 401, "请重新登录"),

    // 权限 103xxx
    FORBIDDEN(103001, 403, "没有权限"),
    ACCOUNT_BANNED(103002, 403, "账号已被封禁"),
    ACCOUNT_CANCELLED(103003, 403, "账号已注销"),

    // 资源 104xxx
    NOT_FOUND(104001, 404, "资源不存在"),
    PET_NOT_FOUND(104002, 404, "宠物不存在"),
    WORK_NOT_FOUND(104003, 404, "作品不存在"),
    ORDER_NOT_FOUND(104004, 404, "订单不存在"),
    POST_NOT_FOUND(104005, 404, "帖子不存在"),
    REMINDER_NOT_FOUND(104006, 404, "提醒不存在"),
    STYLE_NOT_FOUND(104007, 404, "风格不存在"),
    PRODUCT_NOT_FOUND(104008, 404, "商品不存在"),
    COUPON_NOT_FOUND(104009, 404, "卡券不存在"),

    // 状态 105xxx
    STATE_CONFLICT(105001, 409, "状态冲突"),
    PET_LIMIT_EXCEEDED(105002, 409, "宠物数量已达上限，升级会员解锁更多"),
    REMINDER_LIMIT_EXCEEDED(105003, 409, "提醒数量已达上限"),
    QUOTA_INSUFFICIENT(105004, 409, "本月次数已用完，升级会员解锁更多"),
    ALREADY_REFUNDED(105005, 409, "已退款，不可重复"),
    ORDER_PAID(105006, 409, "订单已支付"),
    TASK_STATE_INVALID(105007, 409, "任务状态不允许此操作"),
    VIP_ALREADY_ACTIVE(105008, 409, "会员已激活"),
    POST_REVIEWING(105009, 409, "内容正在审核中"),

    // 三方 106xxx
    AI_PROVIDER_ERROR(106001, 502, "AI 生成服务异常，请稍后再试"),
    AI_TIMEOUT(106002, 504, "AI 生成超时，请重试"),
    WX_PAY_ERROR(106003, 502, "微信支付异常"),
    WX_LOGIN_ERROR(106004, 502, "微信登录异常"),
    OSS_ERROR(106005, 502, "文件上传失败"),
    CONTENT_AUDIT_FAILED(106006, 502, "内容安全审核失败"),

    // 限流 109xxx
    RATE_LIMIT_GLOBAL(109001, 429, "请求过于频繁"),
    RATE_LIMIT_USER(109002, 429, "操作过于频繁"),
    RATE_LIMIT_IP(109003, 429, "请求过于频繁"),
    RATE_LIMIT_AI_TASK(109004, 429, "已有任务进行中"),

    // 业务专属 30xxx 宠物
    PET_AVATAR_INVALID(300001, 422, "宠物头像格式不支持"),
    PET_ALLERGIES_INVALID(300002, 422, "过敏史数据格式错误"),

    // 40xxx AI
    AI_TASK_TYPE_INVALID(400001, 422, "AI 任务类型不支持"),
    AI_IMAGE_TOO_SMALL(400002, 422, "照片分辨率过低，请选择清晰图片"),
    AI_NO_PET_DETECTED(400003, 422, "没有检测到毛孩子，请换张照片试试"),
    AI_MULTIPLE_PETS(400004, 422, "检测到多只毛孩子，目前仅支持单只"),
    AI_FACE_DETECTED(400005, 422, "不支持人像，请上传毛孩子照片"),
    AI_STYLE_UNAVAILABLE(400006, 422, "该风格当前不可用"),
    AI_TASK_TIMEOUT(400007, 424, "生成超时，请重新尝试"),
    AI_PREVIEW_EXPIRED(400008, 422, "预览已过期，请重新生成"),

    // 50xxx 订单
    ORDER_AMOUNT_INVALID(500001, 422, "订单金额异常"),
    PRODUCT_OFFLINE(500002, 422, "商品已下架"),
    COUPON_USED(500003, 422, "卡券已使用"),
    COUPON_EXPIRED(500004, 422, "卡券已过期"),
    REFUND_TIME_EXCEEDED(500005, 422, "已超过可退款时间"),
    REFUND_LIMIT_EXCEEDED(500006, 422, "本月退款次数已用完"),
    ORDER_CLOSED(500007, 422, "订单已关闭"),

    // 60xxx 会员
    VIP_EXPIRED(600001, 422, "会员已到期"),

    // 70xxx 工具
    FEEDING_PLAN_EXISTS(700001, 422, "已生成过方案，可在记录中查看"),
    FEEDING_ADJUST_LIMIT(700002, 422, "方案调整次数已达上限"),
    REMINDER_TIME_INVALID(700003, 422, "提醒时间不能早于今天"),

    // 80xxx 社交
    POST_CONTENT_INVALID(800001, 422, "帖子内容违规"),
    ALREADY_LIKED(800002, 422, "已经点过赞了"),
    DAILY_GREETING_LIMIT(800003, 429, "今日打招呼次数已用完"),

    // 90xxx 系统
    OSS_DOWN(900001, 503, "对象存储故障"),
    DB_DOWN(900002, 503, "数据库故障"),
    REDIS_DOWN(900003, 503, "缓存故障"),
    AI_DOWN(900004, 504, "AI 服务整体不可用");

    private final int code;
    private final int httpStatus;
    private final String message;

    ErrorCode(int code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public int getCode() { return code; }
    public int getHttpStatus() { return httpStatus; }
    public String getMessage() { return message; }

    public static ErrorCode of(int code) {
        for (ErrorCode e : values()) {
            if (e.code == code) return e;
        }
        return SYSTEM_ERROR;
    }
}
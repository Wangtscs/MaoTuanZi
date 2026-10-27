package com.maotuanzi.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一 API 响应
 */
@Data
@ApiModel("统一响应")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 与 ErrorCode.SYSTEM_ERROR 对齐的默认失败码（避免 ApiResult 反向依赖 enums 包） */
    private static final int SYSTEM_ERROR_CODE = 100001;

    @ApiModelProperty("状态码，0 表示成功")
    private Integer code;

    @ApiModelProperty("提示信息")
    private String message;

    @ApiModelProperty("业务数据")
    private T data;

    @ApiModelProperty("链路追踪 ID")
    private String traceId;

    public static <T> ApiResult<T> ok() {
        return ok(null);
    }

    public static <T> ApiResult<T> ok(T data) {
        ApiResult<T> r = new ApiResult<>();
        r.setCode(0);
        r.setMessage("ok");
        r.setData(data);
        return r;
    }

    public static <T> ApiResult<T> fail(int code, String message) {
        ApiResult<T> r = new ApiResult<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    /** 仅带消息的失败响应，默认使用 SYSTEM_ERROR 错误码 */
    public static <T> ApiResult<T> fail(String message) {
        return fail(SYSTEM_ERROR_CODE, message);
    }

    public boolean isSuccess() {
        return code != null && code == 0;
    }
}
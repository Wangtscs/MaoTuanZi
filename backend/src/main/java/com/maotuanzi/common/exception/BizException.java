package com.maotuanzi.common.exception;

import com.maotuanzi.common.enums.ErrorCode;
import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BizException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final int code;
    private final int httpStatus;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }

    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }

    public BizException(int code, int httpStatus, String message) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public static BizException of(ErrorCode errorCode) {
        return new BizException(errorCode);
    }

    public static BizException of(ErrorCode errorCode, String message) {
        return new BizException(errorCode, message);
    }
}
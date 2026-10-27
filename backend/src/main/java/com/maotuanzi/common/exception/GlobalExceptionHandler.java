package com.maotuanzi.common.exception;

import com.maotuanzi.common.enums.ErrorCode;
import com.maotuanzi.common.result.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常拦截
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResult<Void>> handleBiz(BizException e, HttpServletRequest req) {
        log.warn("业务异常 code={} path={} msg={}", e.getCode(), req.getRequestURI(), e.getMessage());
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(ApiResult.fail(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.fail(ErrorCode.PARAM_INVALID.getCode(), msg));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResult<Void>> handleBind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.fail(ErrorCode.PARAM_INVALID.getCode(), msg));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<Void>> handleConstraint(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("；"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.fail(ErrorCode.PARAM_INVALID.getCode(), msg));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResult<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.fail(ErrorCode.PARAM_MISSING.getCode(), "缺少参数：" + e.getParameterName()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResult<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.fail(ErrorCode.PARAM_FORMAT.getCode(), "参数格式错误：" + e.getName()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResult<Void>> handleUploadSize(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.fail(ErrorCode.FILE_TOO_LARGE.getCode(), "文件过大"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleAll(Exception e, HttpServletRequest req) {
        log.error("未捕获异常 path={}", req.getRequestURI(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResult.fail(ErrorCode.SYSTEM_ERROR.getCode(), ErrorCode.SYSTEM_ERROR.getMessage()));
    }
}
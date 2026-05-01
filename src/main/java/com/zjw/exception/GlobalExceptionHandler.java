package com.zjw.exception;

import com.zjw.common.BaseResponse;
import com.zjw.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 自定义全局异常处理器
 *
 * @author ZhangJw
 * @date 2026年05月01日 13:58
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 统一处理业务异常
     *
     * @param e 异常
     * @return 响应
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * 统一处理运行时异常
     *
     * @param e 运行时异常
     * @return 响应
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}


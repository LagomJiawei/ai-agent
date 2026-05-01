package com.zjw.common;

import com.zjw.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 自定义响应包装类
 *
 * @author ZhangJw
 * @date 2026年05月01日 13:47
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}


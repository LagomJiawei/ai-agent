package com.zjw.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 自定义删除请求包装类
 *
 * @author ZhangJw
 * @date 2026年05月01日 14:07
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}

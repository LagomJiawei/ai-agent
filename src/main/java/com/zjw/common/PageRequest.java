package com.zjw.common;

import lombok.Data;

/**
 * 自定义请求包装类
 *
 * @author ZhangJw
 * @date 2026年05月01日 14:05
 */
@Data
public class PageRequest {

    /**
     * 当前页号
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认降序）
     */
    private String sortOrder = "descend";
}

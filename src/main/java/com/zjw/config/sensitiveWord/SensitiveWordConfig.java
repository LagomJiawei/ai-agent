package com.zjw.config.sensitiveWord;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建敏感词配置类：动态加载敏感词库
 *
 * @author ZhangJw
 * @date 2026年05月04日 14:02
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.sensitive")
public class SensitiveWordConfig {

    /**
     * 敏感词列表
     */
    private List<String> words = new ArrayList<>();

    /**
     * 是否启用敏感词校验
     */
    private boolean enabled = true;

    /**
     * 处理策略：BLOCK(拦截) / REPLACE(替换)
     */
    private String strategy = "BLOCK";
}
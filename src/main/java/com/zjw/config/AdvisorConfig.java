package com.zjw.config;

import com.zjw.advisor.SensitiveWordAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ZhangJw
 * @date 2026年05月04日 15:47
 */
@Configuration
public class AdvisorConfig {

    /**
     * 创建 SensitiveWordAdvisor对象的 Bean 实例
     *
     * @param sensitiveWordConfig 敏感词配置
     * @param customWordDeny      敏感词库的提供者
     * @return 敏感词校验 Advisor
     */
    @Bean
    public SensitiveWordAdvisor sensitiveWordAdvisor(SensitiveWordConfig sensitiveWordConfig, CustomWordDeny customWordDeny) {
        return new SensitiveWordAdvisor(sensitiveWordConfig, customWordDeny);
    }
}
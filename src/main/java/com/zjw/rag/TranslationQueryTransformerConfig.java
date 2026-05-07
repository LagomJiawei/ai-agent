package com.zjw.rag;

import com.zjw.service.ThirdPartyTranslationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 翻译查询转换器配置
 *
 * @author ZhangJw
 * @date 2026年05月07日 14:34
 */
@Configuration
public class TranslationQueryTransformerConfig {

    @Value("${translation.target-language:zh}")
    private String targetLanguage;

    /**
     * 创建基于第三方API的翻译查询转换器
     * 实现Spring AI的QueryTransformer接口
     *
     * @param translationService 翻译服务
     * @return ApiTranslationQueryTransformer实例
     */
    @Bean
    public ApiTranslationQueryTransformer apiTranslationQueryTransformer(ThirdPartyTranslationService translationService) {
        return new ApiTranslationQueryTransformer(translationService, targetLanguage);
    }
}
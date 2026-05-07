package com.zjw.rag;

import com.zjw.service.ThirdPartyTranslationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;

/**
 * 基于第三方翻译API的查询转换器
 * 实现Spring AI的QueryTransformer接口，使用第三方API而非LLM进行翻译，降低大模型的使用成本
 *
 * @author ZhangJw
 * @date 2026年05月07日 14:29
 */
@Slf4j
public class ApiTranslationQueryTransformer implements QueryTransformer {

    private final ThirdPartyTranslationService translationService;
    private final String targetLanguage;

    /**
     * 构造方法
     *
     * @param translationService 翻译服务
     * @param targetLanguage 目标语言代码（如：en, zh, ja等）
     */
    public ApiTranslationQueryTransformer(ThirdPartyTranslationService translationService, String targetLanguage) {
        this.translationService = translationService;
        this.targetLanguage = targetLanguage != null ? targetLanguage : "zh";
    }

    /**
     * 构造方法（默认翻译成中文）
     *
     * @param translationService 翻译服务
     */
    public ApiTranslationQueryTransformer(ThirdPartyTranslationService translationService) {
        this(translationService, "zh");
    }

    @Override
    public Query transform(Query query) {
        if (query == null || query.text() == null || query.text().trim().isEmpty()) {
            log.debug("查询为空，直接返回");
            return query;
        }

        String originalText = query.text();
        log.info("开始翻译查询，原文: {}", originalText);

        try {
            // 使用第三方翻译API翻译查询
            String translatedText = translationService.translate(originalText, targetLanguage);

            log.info("翻译完成，原文: {}, 译文: {}", originalText, translatedText);

            // 创建新的Query对象
            Query transformedQuery = new Query(translatedText);

            return transformedQuery;

        } catch (Exception e) {
            log.error("查询翻译失败，使用原文。错误: {}", e.getMessage(), e);
            // 翻译失败时返回原查询，保证系统可用性（降级策略）
            return query;
        }
    }

    /**
     * 简单的语言检测（可选实现）
     * 实际项目中可以集成语言检测库如langdetect
     */
    private String detectLanguage(String text) {
        // 简单判断是否包含中文字符
        if (text.matches(".*[\\u4e00-\\u9fa5].*")) {
            return "zh";
        }
        return "auto";
    }
}
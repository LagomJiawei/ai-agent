package com.zjw.service;

/**
 * 自定义接入第三方翻译服务
 *
 * @author ZhangJw
 * @date 2026年05月07日 14:45
 */
public interface TranslationService {

    /**
     * 翻译文本
     *
     * @param text 待翻译文本
     * @param targetLanguage 目标语言代码（如：en, zh, ja等）
     * @return 翻译后的文本
     */
    String translate(String text, String targetLanguage);

    /**
     * 翻译文本（使用默认目标语言）
     *
     * @param text 待翻译文本
     * @return 翻译后的文本
     */
    String translate(String text);
}

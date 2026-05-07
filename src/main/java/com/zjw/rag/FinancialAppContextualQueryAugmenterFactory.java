package com.zjw.rag;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

/**
 * @author ZhangJw
 * @date 2026年05月07日 9:05
 */
public class FinancialAppContextualQueryAugmenterFactory {

    public static ContextualQueryAugmenter createContextualQueryAugmenter() {
        PromptTemplate emptyContextPromptTemplate = new PromptTemplate("""
                你应该输出下面的内容：
                抱歉，我只能回答理财相关的问题，别的问题没办法帮到您。
                """);
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false) // 不允许空上下文查询：此时系统会使用【默认】的emptyContextPromptTemplate去替换userText
                .emptyContextPromptTemplate(emptyContextPromptTemplate) // 此时系统会使用【自定义】的emptyContextPromptTemplate去替换userText
                .build();
    }
}
package com.zjw.rag;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义一个 Spring AI中 ETL Pipeline 提供的文档元信息增强器（自动 为文档添加元信息）
 *
 * @author ZhangJw
 * @date 2026年05月07日 7:02
 */
@Component
public class MyKeywordEnricher {

    private final ChatModel dashscopeChatModel;

    MyKeywordEnricher(ChatModel dashscopeChatModel) {
        this.dashscopeChatModel = dashscopeChatModel;
    }

    public List<Document> enrichDocuments(List<Document> documents) {
        KeywordMetadataEnricher enricher = KeywordMetadataEnricher.builder(dashscopeChatModel)
                .keywordCount(5)
                .build();
        return  enricher.apply(documents);
    }
}
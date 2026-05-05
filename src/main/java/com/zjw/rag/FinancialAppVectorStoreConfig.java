package com.zjw.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 法1：集成本地知识库：初始化一个基于内存读写的向量数据库 SimpleVectorStore
 *
 * @author ZhangJw
 * @date 2026年05月05日 8:48
 */
@Configuration
public class FinancialAppVectorStoreConfig {

    @Resource
    private FinancialAppDocumentLoader financialAppDocumentLoader;

    @Bean
    VectorStore financialAppVectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel)
                .build();
        // 加载文档
        List<Document> documents = financialAppDocumentLoader.loadMarkdowns();
        simpleVectorStore.add(documents);
        return simpleVectorStore;
    }
}
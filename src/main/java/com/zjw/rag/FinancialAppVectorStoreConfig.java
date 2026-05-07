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

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Bean
    VectorStore financialAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel)
                .build();
        // 加载文档（此时可以手动批量为文档添加元信息）
        List<Document> documents = financialAppDocumentLoader.loadMarkdowns();
        // 文档切分
//        List<Document> splitDocuments = myTokenTextSplitter.splitDocuments(documents);
        // 使用文档元信息增强器自动为文档添加元信息
        List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(documents);
        simpleVectorStore.add(enrichedDocuments);
        return simpleVectorStore;
    }
}
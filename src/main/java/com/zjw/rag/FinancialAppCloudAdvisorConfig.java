package com.zjw.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 法2：集成阿里云知识库服务：自定义一个基于阿里云知识库服务的 RAG 增强顾问
 *
 * @author ZhangJw
 * @date 2026年05月05日 10:48
 */
@Slf4j
@Configuration
public class FinancialAppCloudAdvisorConfig {

    @Value("${spring.ai.dashScope.api-key}")
    private String dashScopeApiKey;

    @Bean
    public Advisor financialAppCloudAdvisor() {
        DashScopeApi dashScopeApi = new DashScopeApi(dashScopeApiKey);
        // 云知识库名称
        final String KNOWLEDGE_INDEX = "理财大师";
        DocumentRetriever documentRetriever = new DashScopeDocumentRetriever(dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                        .withIndexName(KNOWLEDGE_INDEX)
                        .build());
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .build();
    }
}
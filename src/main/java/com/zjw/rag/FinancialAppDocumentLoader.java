package com.zjw.rag;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.cloud.ai.parser.markdown.MarkdownDocumentParser;
import com.alibaba.cloud.ai.reader.feishu.FeiShuDocumentReader;
import com.alibaba.cloud.ai.reader.feishu.FeiShuResource;
import com.alibaba.cloud.ai.reader.yuque.YuQueDocumentReader;
import com.alibaba.cloud.ai.reader.yuque.YuQueResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Markdown 文档加载器
 *
 * @author ZhangJw
 * @date 2026年05月05日 8:10
 */
@Slf4j
@Component
public class FinancialAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    public FinancialAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载多篇 Markdown 文档
     *
     * @return
     */
    public List<Document> loadMarkdowns() {
        List<Document> allDocuments = new ArrayList<>();
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            for (Resource resource : resources) {
                String fileName = resource.getFilename();
                // 提取文档倒数第3个字到最后作为标签
                String status = fileName.substring(fileName.length() - 3);
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", fileName)
                        // 手动批量为文档添加元信息
                        .withAdditionalMetadata("status", status)
                        .build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                allDocuments.addAll(reader.get());
            }
        } catch (IOException e) {
            log.error("Markdown 文档加载失败", e);
        }
        return allDocuments;
    }


    @Value("${spring.ai.alibaba.document-reader.yuque.token}")
    private String yuQueToken;

    /**
     * 加载多篇语雀文档
     *
     * @param resourceUrls 语雀文档完整 URL 列表
     *                     示例：https://www.yuque.com/tech-team/knowledge-base/doc-xyz-123
     * @return 文档列表
     */
    public List<Document> loadYuQue(List<String> resourceUrls) {
        List<Document> allDocuments = new ArrayList<>();

        if (CollectionUtil.isEmpty(resourceUrls)) {
            log.warn("语雀文档 URL 列表为空");
            return allDocuments;
        }

        if (yuQueToken == null || yuQueToken.trim().isEmpty()) {
            log.error("语雀 Token 未配置，请在 application.yml 中配置 app.yuque.token");
            return allDocuments;
        }

        try {
            // 创建 Markdown 解析器（语雀文档通常是 Markdown 格式）
            MarkdownDocumentParser parser = new MarkdownDocumentParser();

            for (String resourceUrl : resourceUrls) {
                try {
                    // 验证 URL 格式
                    if (!resourceUrl.startsWith("https://www.yuque.com/")) {
                        log.warn("无效的语雀文档 URL: {}", resourceUrl);
                        continue;
                    }

                    // 使用 apiToken 和 DocumentParser 构造 YuQueDocumentReader
                    YuQueDocumentReader reader = new YuQueDocumentReader(
                            YuQueResource.builder()
                                    .yuQueToken(yuQueToken)
                                    .resourcePath(resourceUrl)
                                    .build(), parser);
                    List<Document> documents = reader.get();

                    // 为每个文档添加元信息
                    for (Document doc : documents) {
                        doc.getMetadata().put("source", "yuque");
                        doc.getMetadata().put("resourceUrl", resourceUrl);
                    }

                    allDocuments.addAll(documents);
                    log.info("成功加载语雀文档: {}", resourceUrl);
                } catch (Exception e) {
                    log.error("加载语雀文档失败: {}", resourceUrl, e);
                }
            }
        } catch (Exception e) {
            log.error("语雀文档批量加载失败", e);
        }

        log.info("共加载 {} 篇语雀文档", allDocuments.size());
        return allDocuments;
    }


    @Value("${spring.ai.alibaba.document-reader.larksuite.app-id}")
    private String feishuAppId;

    @Value("${spring.ai.alibaba.document-reader.larksuite.app-secret}")
    private String feishuAppSecret;

    /**
     * 加载多篇飞书文档
     *
     * @param documentTokens 飞书文档 token 列表
     *                       示例：从 URL https://xxx.feishu.cn/docx/AbCdEfGhIjKl 中提取 AbCdEfGhIjKl
     * @return 文档列表
     */
    public List<Document> loadFeishu(List<String> documentTokens) {
        List<Document> allDocuments = new ArrayList<>();

        if (CollectionUtil.isEmpty(documentTokens)) {
            log.warn("飞书文档 token 列表为空");
            return allDocuments;
        }

        if (feishuAppId == null || feishuAppId.trim().isEmpty() ||
                feishuAppSecret == null || feishuAppSecret.trim().isEmpty()) {
            log.error("飞书 App ID 或 App Secret 未配置，请在 application.yml 中配置 app.feishu.app-id 和 app.feishu.app-secret");
            return allDocuments;
        }

        try {
            // 创建 Markdown 解析器（飞书文档通常返回 Markdown 格式）
            MarkdownDocumentParser parser = new MarkdownDocumentParser();

            for (String documentToken : documentTokens) {
                try {
                    // 验证 token 格式（简单验证）
                    if (documentToken == null || documentToken.trim().isEmpty()) {
                        log.warn("无效的飞书文档 token: {}", documentToken);
                        continue;
                    }

                    // 使用 appId、appSecret 和 DocumentParser 构造 FeishuDocumentReader
                    FeiShuDocumentReader reader = new FeiShuDocumentReader(
                            FeiShuResource.builder()
                                    .appId(feishuAppId)
                                    .appSecret(feishuAppSecret)
                                    .build(), documentToken);
                    List<Document> documents = reader.get();

                    // 为每个文档添加元信息
                    for (Document doc : documents) {
                        doc.getMetadata().put("source", "feishu");
                        doc.getMetadata().put("documentToken", documentToken);
                    }

                    allDocuments.addAll(documents);
                    log.info("成功加载飞书文档: {}", documentToken);
                } catch (Exception e) {
                    log.error("加载飞书文档失败: {}", documentToken, e);
                }
            }
        } catch (Exception e) {
            log.error("飞书文档批量加载失败", e);
        }

        log.info("共加载 {} 篇飞书文档", allDocuments.size());
        return allDocuments;
    }
}
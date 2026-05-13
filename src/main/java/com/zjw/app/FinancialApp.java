package com.zjw.app;

import com.zjw.advisor.MyLoggerAdvisor;
import com.zjw.advisor.SensitiveWordAdvisor;
import com.zjw.chatMemory.FileBasedChatMemory;
import com.zjw.rag.ApiTranslationQueryTransformer;
import com.zjw.rag.FinancialAppRagAdvisorFactory;
import com.zjw.rag.MyQueryRewriter;
import com.zjw.service.ManualToolExecutionService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.rag.Query;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 理财咨询App
 *
 * @author ZhangJw
 * @date 2026年05月03日 9:30
 */
@Component
@Slf4j
public class FinancialApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "你是资深理财专家，请模拟真实咨询场景。不要一上来就推荐产品，" +
            "先通过“目前的收入与负债情况如何”“对收益和风险的承受底线在哪”“这笔钱计划多久不用”等引导性问题，层层摸清用户的财务全貌、" +
            "风险偏好与核心诉求。在全面掌握细节前绝不给笼统建议，务必基于深入了解后，提供个性化、可落地的理财方案，切实解决用户的理财痛点。";

    /**
     * 初始化 chatClient
     */
    public FinancialApp(ChatModel dashscopeChatModel, SensitiveWordAdvisor sensitiveWordAdvisor) {
        // 初始化自定义的基于文件读写的对话记忆持久化ChatMemory
        String fileDir = System.getProperty("user.dir") + "/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // 自定义敏感词 Advisor
                        sensitiveWordAdvisor
//                        // 自定义日志 Advisor
//                        new MyLoggerAdvisor(),
//                        // 自定义 Re2 Advisor
//                        new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * 简单对话
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                // 对话时动态设置Advisor的参数，如：对话记忆的ID 和 大小
                .advisors(spec -> spec.param("chat_memory_conversation_id", chatId)
                        .param("chat_memory_retrieve_size", 10))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 定义理财报告类：Java 14 record特性
     *
     * @param title
     * @param suggestions
     */
    record PsychologyReport(String title, List<String> suggestions) {

    }

    /**
     * 生成理财报告：实现结构化输出
     *
     * @param message
     * @param chatId
     * @return
     */
    public PsychologyReport doChatWithReport(String message, String chatId) {
        PsychologyReport psychologyReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成理财结果，标题为{用户名}的理财报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param("chat_memory_conversation_id", chatId))
                .call()
                .entity(PsychologyReport.class);
        log.info("psychologyReport: {}", psychologyReport);
        return psychologyReport;
    }

    @Resource
    private VectorStore financialAppVectorStore;

    @Resource
    private Advisor financialAppCloudAdvisor;

    @Resource
    private MyQueryRewriter queryRewriter;

    @Resource
    private ApiTranslationQueryTransformer translationQueryTransformer;

    /**
     * 使用 RAG 知识库 进行对话
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {
        // 1.先执行查询翻译（将非中文翻译成中文）
        Query translatedQuery = translationQueryTransformer.transform(
                new Query(message)
        );
        String translatedMessage = translatedQuery.text();

        // 2.执行查询重写（优化查询表达）
        String rewrittenMessage = queryRewriter.doQueryRewrite(translatedMessage);
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(rewrittenMessage) // 使用查询重写后的查询
                .advisors(FinancialAppRagAdvisorFactory.createRagAdvisor(financialAppVectorStore, "轻度"))
                .advisors(spec -> spec.param("chat_memory_conversation_id", chatId)
                        .param("chat_memory_retrieve_size", 10))
                .advisors(new MyLoggerAdvisor()) // 开启日志
                // 类型1：开启QuestionAnswerAdvisor 这种 RAG 知识库（更简单）
                .advisors(QuestionAnswerAdvisor.builder(financialAppVectorStore).build())
                // 类型2：开启 RetrievalAugmentationAdvisor 这种 RAG 知识库（更灵活）
//                .advisors(financialAppCloudAdvisor)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    @Resource
    private List<ToolCallback> allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param("chat_memory_conversation_id", chatId)
                        .param("chat_memory_retrieve_size", 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                .tools((Object) allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }


    @Resource
    private ManualToolExecutionService manualToolExecutionService;

    /**
     * 使用手动控制的工具执行进行对话（提高可观测性）
     * 适用于需要详细监控工具调用流程、添加自定义逻辑（如审批、过滤、审计）的场景
     *
     * @param message 用户消息
     * @param chatId  对话ID
     * @return AI回复
     */
    public String doChatWithManualToolControl(String message, String chatId) {
        log.info("🎯 Using manual tool execution control for chatId: {}", chatId);

        // 默认最大迭代次数为5，防止无限循环
        int maxIterations = 5;

        return manualToolExecutionService.executeWithManualToolControl(
                message,
                allTools,
                maxIterations
        );
    }

    /**
     * 使用手动控制的工具执行进行对话（自定义最大迭代次数）
     *
     * @param message       用户消息
     * @param chatId        对话ID
     * @param maxIterations 最大迭代次数
     * @return AI回复
     */
    public String doChatWithManualToolControl(String message, String chatId, int maxIterations) {
        log.info("🎯 Using manual tool execution control for chatId: {} with maxIterations: {}",
                chatId, maxIterations);

        return manualToolExecutionService.executeWithManualToolControl(
                message,
                allTools,
                maxIterations
        );
    }

    /**
     * 引入 spring-ai-starter-mcp-client 后，Spring Boot 会自动执行以下步骤：
     * 步骤 1：读取配置文件
     *      Spring AI 的自动配置类会读取 application.yml 中的 MCP 配置，找到 mcp-servers.json 文件。
     * 步骤 2：启动 MCP Server 子进程
     *      对于配置的每个 MCP 服务器（如 amap-maps），Spring AI 会：
     *          使用 ProcessBuilder 启动子进程（执行 npx.cmd -y @amap/amap-maps-mcp-server）
     *          通过 stdio（标准输入/输出） 与子进程通信
     *          遵循 MCP 协议（Model Context Protocol）进行握手和工具发现
     * 步骤 3：发现工具列表
     *      MCP Server 启动后，会通过 JSON-RPC 协议返回它提供的所有工具定义，包括：
     *          工具名称
     *          工具描述
     *          参数 （JSON Schema 格式）
     * 步骤 4：创建 ToolCallback 对象
     *     Spring AI 会为每个工具创建一个 ToolCallback 对象，这个对象封装了：
     *          工具的元数据（名称、描述、参数）
     *          调用逻辑（通过 stdio 向 MCP Server 发送请求）
     * 步骤 5：注册 ToolCallbackProvider Bean
     *      Spring AI 自动创建一个 ToolCallbackProvider Bean，作用是：
     *          聚合从 MCP Server 发现的所有工具
     *          提供 getToolCallbacks() 方法返回 ToolCallback[] 数组
     */
    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    /**
     * 使用MCP进行对话
     *
     * @param message 用户消息
     * @param chatId  对话ID
     * @return AI回复
     */
    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param("chat_memory_conversation_id", chatId)
                        .param("chat_memory_retrieve_size", 10))
                .advisors(new MyLoggerAdvisor())
                // 通过 ToolCallbackProvider 获取到 mcp-servers.json 配置中定义的 MCP 服务提供的所有工具
                /**
                 * 执行到 .tools(toolCallbackProvider) 时：
                 * Spring AI 会调用 toolCallbackProvider.getToolCallbacks() 获取 ToolCallbackProvider 中已添加的工具
                 * 这些工具会被注册到 ChatClient 的工具调用管理器中
                 * LLM 在生成回复时，可以根据需要选择调用这些工具
                 * 当 LLM 决定调用某个工具时会告知 Spring AI ，Spring AI 会通过 stdio 协议向对应的 MCP Server 发送请求
                 * MCP Server 执行工具并返回结果
                 * 结果被传回给 LLM，LLM 基于结果生成最终回复
                 */
                .tools(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 流式输出
     *
     * @param message 用户消息
     * @param chatId  对话ID
     * @return AI回复的文本信息
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param("chat_memory_conversation_id", chatId)
                        .param("chat_memory_retrieve_size", 10))
                .stream()
                .content();
    }

}

package com.zjw.app;

import com.zjw.advisor.MyLoggerAdvisor;
import com.zjw.advisor.SensitiveWordAdvisor;
import com.zjw.chatMemory.FileBasedChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

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

    /**
     * 使用 RAG 知识库 进行对话
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param("chat_memory_conversation_id", chatId)
                        .param("chat_memory_retrieve_size", 10))
                // 开启日志
                .advisors(new MyLoggerAdvisor())
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
}

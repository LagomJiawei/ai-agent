package com.zjw.app;

import com.zjw.advisor.SensitiveWordAdvisor;
import com.zjw.chatMemory.FileBasedChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 心理咨询App
 *
 * @author ZhangJw
 * @date 2026年05月03日 9:30
 */
@Component
@Slf4j
public class PsychologyApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    /**
     * 初始化 chatClient
     */
    public PsychologyApp(ChatModel dashscopeChatModel, SensitiveWordAdvisor sensitiveWordAdvisor) {
        // 初始化自定义的基于文件读写的对话记忆持久化ChatMemory
        String fileDir = System.getProperty("user.dir") + "/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        sensitiveWordAdvisor
//                        // 自定义日志 Advisor
//                        new MyLoggerAdvisor(),
//                        // 自定义 Re2 Advisor
//                        new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * 对话
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
                .advisors(spec -> spec.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(MessageChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    /**
     * 定义恋爱报告类：Java 14 record特性
     *
     * @param title
     * @param suggestions
     */
    record PsychologyReport(String title, List<String> suggestions) {

    }

    /**
     * 生成恋爱报告：实现结构化输出
     *
     * @param message
     * @param chatId
     * @return
     */
    public PsychologyReport doChatWithReport(String message, String chatId) {
        PsychologyReport psychologyReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成恋爱结果，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(MessageChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .call()
                .entity(PsychologyReport.class);
        log.info("psychologyReport: {}", psychologyReport);
        return psychologyReport;
    }

}

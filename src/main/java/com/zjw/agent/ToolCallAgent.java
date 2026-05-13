package com.zjw.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.zjw.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理工具调用的代理类，具体实现了 think 和 act 方法，可以用作创建实例的父类
 *
 * @author ZhangJw
 * @date 2026年05月11日 8:57
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    // 可用的工具
    private final List<ToolCallback> availableTools;

    // 保存工具调用信息的响应结果（要调用哪些工具）
    private ChatResponse toolCallChatResponse;

    // 工具调用管理者
    private final ToolCallingManager toolCallingManager;

    // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
    private final ChatOptions chatOptions;

    public ToolCallAgent(List<ToolCallback> availableTools, ToolCallingManager toolCallingManager) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = toolCallingManager;
        // 禁用 Spring AI 内置的自动工具调用机制，改为手动调用。自己维护选项和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();
    }

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动
     */
    @Override
    public boolean think() {
        // 1、校验提示词，拼接用户提示词
        if (StrUtil.isNotBlank(this.getNextStepPrompt())) {
            UserMessage userMessage = new UserMessage(this.getNextStepPrompt());
            this.getMessageList().add(userMessage);
        }
        // 2、调用 AI 大模型，获取工具调用结果
        List<Message> messageList = this.getMessageList();
        Prompt prompt = new Prompt(messageList, this.chatOptions);
        try {
            ChatResponse chatResponse = this.getChatClient().prompt(prompt)
                    .system(this.getSystemPrompt())
                    .tools((Object) availableTools)
                    .call()
                    .chatResponse();
            // 记录响应，用于等下 Act
            this.toolCallChatResponse = chatResponse;
            // 3、解析工具调用结果，获取要调用的工具
            // 助手消息
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 获取要调用的工具列表
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            // 输出提示信息
            String aMessage = assistantMessage.getText();
            log.info("{}的思考：{}", this.getName(), aMessage);
            log.info("{}选择了 {} 个工具来使用", this.getName(), toolCallList.size());
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称：%s，参数：%s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);

            // 记录执行历史到循环检测器中
            List<LoopDetector.ToolCallRecord> toolCallRecords = toolCallList.stream()
                    .map(tc -> new LoopDetector.ToolCallRecord(tc.name(), tc.arguments()))
                    .collect(Collectors.toList());
            this.getLoopDetector().recordExecution(
                    this.getCurrentStep(),
                    aMessage,
                    toolCallRecords
            );
            
            // 如果不需要调用工具，返回 false
            if (toolCallList.isEmpty()) {
                // 不需要调用工具时，才需要手动记录助手消息
                this.getMessageList().add(assistantMessage);
                return false;
            } else {
                // 需要调用工具时，无需记录助手消息，因为调用工具时会自动记录
                return true;
            }
        } catch (Exception e) {
            String errorMsg = String.format("%s在思考过程中遇到错误：%s", this.getName(), e.getMessage());
            log.error(errorMsg, e);
            this.getMessageList().add(new AssistantMessage(errorMsg));
            return false;
        }
    }

    /**
     * 执行工具调用并处理结果
     *
     * @return 执行结果
     */
    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "没有工具需要调用";
        }
        try {
            // 调用工具
            Prompt prompt = new Prompt(this.getMessageList(), this.chatOptions);
            ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
            // 记录消息上下文，conversationHistory 已经包含了助手消息和工具调用返回的结果
            this.setMessageList(toolExecutionResult.conversationHistory());
            ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
            // 判断是否调用了终止工具
            boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                    .anyMatch(response -> response.name().equals("doTerminate"));
            if (terminateToolCalled) {
                // 任务结束，更改状态
                this.setState(AgentState.FINISHED);
            }
            String results = toolResponseMessage.getResponses().stream()
                    .map(response -> "工具 " + response.name() + " 返回的结果：" + response.responseData())
                    .collect(Collectors.joining("\n"));
            log.info(results);
            return results;
        } catch (Exception e) {
            String errorMsg = String.format("%s在执行工具时遇到错误：%s", this.getName(), e.getMessage());
            log.error(errorMsg, e);
            this.getMessageList().add(new AssistantMessage(errorMsg));
            return errorMsg;
        }
    }
}
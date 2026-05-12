package com.zjw.service;

import com.zjw.advisor.ToolExecutionLoggerAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 手动工具执行服务 - 提供对工具调用流程的精细控制
 *
 * 手动工具执行的核心服务类，负责：
 * - 禁用框架自动工具执行
 * - 管理工具调用循环
 * - 记录执行指标
 *
 * @author ZhangJw
 * @date 2026年05月08日 14:41
 */
@Slf4j
@Service
public class ManualToolExecutionService {

    private final ChatModel chatModel;
    private final ToolCallingManager toolCallingManager;
    private final ToolCallback[] providedTools;

    public ManualToolExecutionService(ChatModel dashScopeChatModel, ToolCallingManager toolCallingManager, ToolCallback[] allTools) {
        this.chatModel = dashScopeChatModel;
        this.toolCallingManager = toolCallingManager;
        this.providedTools = allTools;
    }

    /**
     * 使用手动控制的工具执行进行对话
     *
     * @param message 用户消息
     * @param providedTools 可用的工具列表
     * @param maxIterations 最大迭代次数（防止无限循环）
     * @return 最终响应
     */
    public String executeWithManualToolControl(String message, ToolCallback[] providedTools, int maxIterations) {
        long startTime = System.currentTimeMillis();
        int iteration = 0;

        log.info("🚀 Starting manual tool execution for message: {}", message);
        log.info("🔧 Available tools: {}", Arrays.stream(providedTools)
                .map(tool -> tool.getToolDefinition().name())
                .collect(Collectors.joining(", ")));

        // 配置聊天选项，禁用内部工具自动执行
        ToolCallingChatOptions chatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(providedTools)
                .internalToolExecutionEnabled(false) // 关键：禁用自动执行
                .build();

        // 创建初始提示
        Prompt prompt = new Prompt(message, chatOptions);

        // 第一次调用模型
        ChatResponse chatResponse = chatModel.call(prompt);
        iteration++;

        // 手动处理工具调用循环
        while (chatResponse.hasToolCalls() && iteration < maxIterations) {
            ToolExecutionLoggerAdvisor.logToolExecutionLoop(iteration, true);

            // 记录模型决定调用的工具
            List<String> toolNames = chatResponse.getResults().stream()
                    .flatMap(result -> result.getOutput().getToolCalls().stream())
                    .map(toolCall -> toolCall.name())
                    .collect(Collectors.toList());

            ToolExecutionLoggerAdvisor.logToolCallDecision(toolNames.size(),
                    toolNames.toArray(new String[0]));

            // 执行工具调用
            long toolExecStartTime = System.currentTimeMillis();
            ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, chatResponse);
            long toolExecDuration = System.currentTimeMillis() - toolExecStartTime;

            log.info("⚙️  Tool execution completed in {} ms", toolExecDuration);

            // 使用工具执行结果创建新的提示，继续对话
            prompt = new Prompt(toolExecutionResult.conversationHistory(), chatOptions);
            chatResponse = chatModel.call(prompt);
            iteration++;

            log.info("📝 Model response received after tool execution (iteration {})", iteration);
        }

        if (iteration >= maxIterations && chatResponse.hasToolCalls()) {
            log.warn("⚠️  Reached maximum iterations ({}) with pending tool calls", maxIterations);
        }

        long totalTime = System.currentTimeMillis() - startTime;

        // 获取最终回答
        String finalResponse = chatResponse.getResult().getOutput().getText();
        ToolExecutionLoggerAdvisor.logFinalResponse(finalResponse, iteration, totalTime);

        log.info("✅ Manual tool execution completed in {} ms with {} iterations", totalTime, iteration);

        return finalResponse;
    }

    /**
     * 使用手动控制的工具执行进行对话（带对话历史）
     *
     * @param prompt 包含历史的提示
     * @param tools 可用的工具列表
     * @param maxIterations 最大迭代次数
     * @return 最终响应
     */
    public String executeWithManualToolControl(Prompt prompt, ToolCallback[] tools, int maxIterations) {
        long startTime = System.currentTimeMillis();
        int iteration = 0;

        log.info("🚀 Starting manual tool execution with existing conversation history");

        // 更新聊天选项，禁用内部工具自动执行
        ToolCallingChatOptions chatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(tools)
                .internalToolExecutionEnabled(false)
                .build();

        // 确保提示使用正确的选项
        Prompt updatedPrompt = new Prompt(prompt.getInstructions(), chatOptions);

        // 第一次调用模型
        ChatResponse chatResponse = chatModel.call(updatedPrompt);
        iteration++;

        // 手动处理工具调用循环
        while (chatResponse.hasToolCalls() && iteration < maxIterations) {
            ToolExecutionLoggerAdvisor.logToolExecutionLoop(iteration, true);

            // 记录模型决定调用的工具
            List<String> toolNames = chatResponse.getResults().stream()
                    .flatMap(result -> result.getOutput().getToolCalls().stream())
                    .map(toolCall -> toolCall.name())
                    .collect(Collectors.toList());

            ToolExecutionLoggerAdvisor.logToolCallDecision(toolNames.size(),
                    toolNames.toArray(new String[0]));

            // 执行工具调用
            long toolExecStartTime = System.currentTimeMillis();
            ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(updatedPrompt, chatResponse);
            long toolExecDuration = System.currentTimeMillis() - toolExecStartTime;

            log.info("⚙️  Tool execution completed in {} ms", toolExecDuration);

            // 使用工具执行结果创建新的提示
            updatedPrompt = new Prompt(toolExecutionResult.conversationHistory(), chatOptions);
            chatResponse = chatModel.call(updatedPrompt);
            iteration++;
        }

        long totalTime = System.currentTimeMillis() - startTime;
        String finalResponse = chatResponse.getResult().getOutput().getText();
        ToolExecutionLoggerAdvisor.logFinalResponse(finalResponse, iteration, totalTime);

        log.info("✅ Manual tool execution completed in {} ms with {} iterations", totalTime, iteration);

        return finalResponse;
    }

    /**
     * 检查并记录工具调用详情（用于调试和监控）
     *
     * @param chatResponse 聊天响应
     */
    public void inspectToolCalls(ChatResponse chatResponse) {
        if (!chatResponse.hasToolCalls()) {
            log.info("ℹ️  No tool calls in response");
            return;
        }

        chatResponse.getResults().forEach(result -> {
            result.getOutput().getToolCalls().forEach(toolCall -> {
                log.info("🔍 Tool Call Details:");
                log.info("   - Name: {}", toolCall.name());
                log.info("   - ID: {}", toolCall.id());
                log.info("   - Arguments: {}", toolCall.arguments());
            });
        });
    }
}
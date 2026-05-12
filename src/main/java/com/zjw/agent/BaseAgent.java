package com.zjw.agent;

import cn.hutool.core.util.StrUtil;
import com.zjw.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 基础代理抽象类，用于管理代理状态和执行流程。
 * 提供状态转换、内存管理和基于步骤的执行循环的基础功能。
 * 子类必须实现step方法。
 *
 * @author ZhangJw
 * @date 2026年05月11日 8:42
 */
@Data
@Slf4j
public abstract class BaseAgent {

    // 核心属性
    private String name;

    // 提示
    private String systemPrompt;
    private String nextStepPrompt;

    // 状态
    private AgentState state = AgentState.IDLE;

    // 执行控制
    private int maxSteps = 10;
    private int currentStep = 0;

    // LLM
    private ChatClient chatClient;

    // Memory（需要自主维护会话上下文）
    private List<Message> messageList = new ArrayList<>();

    // 循环检测器
    private LoopDetector loopDetector = new LoopDetector();

    /**
     * 运行代理
     *
     * @param userPrompt 用户提示词
     * @return 执行结果
     */
    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        // 更改状态
        state = AgentState.RUNNING;
        // 记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表
        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step " + stepNumber + "/" + maxSteps);
                // 单步执行
                String stepResult = this.step();
                String result = "Step " + stepNumber + ": " + stepResult;
                results.add(result);

                // 检查是否陷入循环
                LoopDetector.LoopDetectionResult loopResult = loopDetector.checkForLoop();
                if (loopResult.isStuck()) {
                    log.warn("🔄 Agent detected stuck state: {}", loopResult.getDescription());
                    this.handleStuckState(loopResult);
                    results.add("⚠️ 检测到循环状态: " + loopResult.getDescription());
                    results.add("💡 建议: " + String.join("; ", loopResult.getSuggestions()));
                    break;
                }
            }
            // 检查是否超出步骤限制
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "执行错误" + e.getMessage();
        } finally {
            // 清理资源
            this.cleanup();
        }
    }

    /**
     * 处理陷入循环的状态
     *
     * @param loopResult 循环检测结果
     */
    protected void handleStuckState(LoopDetector.LoopDetectionResult loopResult) {
        String stuckPrompt = String.format(
                "⚠️ 检测到循环状态：%s\n请避免重复已尝试过的无效路径，尝试新的策略。\n建议：%s",
                loopResult.getDescription(),
                String.join("；", loopResult.getSuggestions())
        );

        // 将循环警告添加到 nextStepPrompt
        if (this.nextStepPrompt != null && !this.nextStepPrompt.isEmpty()) {
            this.nextStepPrompt = stuckPrompt + "\n\n原提示: " + this.nextStepPrompt;
        } else {
            this.nextStepPrompt = stuckPrompt;
        }

        log.warn("Agent {} detected stuck state. Added prompt: {}", this.name, stuckPrompt);

        // 生成诊断报告
        String diagnosticReport = loopDetector.generateDiagnosticReport();
        log.info("📊 Diagnostic Report:\n{}", diagnosticReport);
    }

    /**
     * 执行单个步骤
     *
     * @return 步骤执行结果
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup() {
        // 子类可以重写此方法来清理资源
    }
}

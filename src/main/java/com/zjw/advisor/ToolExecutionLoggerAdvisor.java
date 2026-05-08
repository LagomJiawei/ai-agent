package com.zjw.advisor;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * 工具执行日志记录器 - 提供详细的工具调用可观测性
 *
 * 增强的日志记录器，提供：
 * - 工具调用开始/结束日志
 * - 执行时间追踪
 * - 参数和结果格式化输出
 * - 唯一执行ID用于追踪
 *
 * @author ZhangJw
 * @date 2026年05月08日 14:40
 */
@Slf4j
public class ToolExecutionLoggerAdvisor {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * 记录工具调用开始
     *
     * @param toolName 工具名称
     * @param arguments 工具参数
     * @return 执行ID，用于追踪整个执行流程
     */
    public static String logToolExecutionStart(String toolName, Map<String, Object> arguments) {
        String executionId = UUID.randomUUID().toString().substring(0, 8);
        String timestamp = LocalDateTime.now().format(FORMATTER);

        log.info("========== [TOOL EXECUTION START] ==========");
        log.info("Execution ID : {}", executionId);
        log.info("Timestamp    : {}", timestamp);
        log.info("Tool Name    : {}", toolName);
        log.info("Arguments    : {}", formatArguments(arguments));
        log.info("============================================");

        return executionId;
    }

    /**
     * 记录工具执行成功
     *
     * @param executionId 执行ID
     * @param result 执行结果
     * @param durationMs 执行耗时（毫秒）
     */
    public static void logToolExecutionSuccess(String executionId, String result, long durationMs) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        int resultLength = result != null ? result.length() : 0;
        String preview = result != null && result.length() > 200
                ? result.substring(0, 200) + "..."
                : result;

        log.info("========== [TOOL EXECUTION SUCCESS] ========");
        log.info("Execution ID : {}", executionId);
        log.info("Timestamp    : {}", timestamp);
        log.info("Duration     : {} ms", durationMs);
        log.info("Result Length: {} chars", resultLength);
        log.info("Result Preview: {}", preview);
        log.info("============================================");
    }

    /**
     * 记录工具执行失败
     *
     * @param executionId 执行ID
     * @param error 错误信息
     * @param durationMs 执行耗时（毫秒）
     */
    public static void logToolExecutionError(String executionId, String error, long durationMs) {
        String timestamp = LocalDateTime.now().format(FORMATTER);

        log.error("========== [TOOL EXECUTION ERROR] ==========");
        log.error("Execution ID : {}", executionId);
        log.error("Timestamp    : {}", timestamp);
        log.error("Duration     : {} ms", durationMs);
        log.error("Error        : {}", error);
        log.error("============================================");
    }

    /**
     * 记录工具调用决策
     *
     * @param toolCallCount 工具调用数量
     * @param toolNames 工具名称列表
     */
    public static void logToolCallDecision(int toolCallCount, String... toolNames) {
        log.info("🤖 Model decided to call {} tool(s): {}", toolCallCount, String.join(", ", toolNames));
    }

    /**
     * 记录完整工具执行循环
     *
     * @param iteration 迭代次数
     * @param hasMoreTools 是否还有更多工具调用
     */
    public static void logToolExecutionLoop(int iteration, boolean hasMoreTools) {
        log.info("🔄 Tool Execution Loop - Iteration: {}, HasMoreTools: {}", iteration, hasMoreTools);
    }

    /**
     * 记录最终响应
     *
     * @param response 最终响应内容
     * @param totalIterations 总迭代次数
     * @param totalTimeMs 总耗时（毫秒）
     */
    public static void logFinalResponse(String response, int totalIterations, long totalTimeMs) {
        String preview = response != null && response.length() > 300
                ? response.substring(0, 300) + "..."
                : response;

        log.info("========== [FINAL RESPONSE] =================");
        log.info("Total Iterations : {}", totalIterations);
        log.info("Total Time       : {} ms", totalTimeMs);
        log.info("Response Length  : {} chars", response != null ? response.length() : 0);
        log.info("Response Preview : {}", preview);
        log.info("============================================");
    }

    /**
     * 格式化参数为可读字符串
     */
    private static String formatArguments(Map<String, Object> arguments) {
        if (arguments == null || arguments.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        arguments.forEach((key, value) -> {
            sb.append(key).append("=");
            if (value instanceof String && ((String) value).length() > 100) {
                sb.append(((String) value).substring(0, 100)).append("...");
            } else {
                sb.append(value);
            }
            sb.append(", ");
        });
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 2); // 移除最后的逗号和空格
        }
        sb.append("}");
        return sb.toString();
    }
}
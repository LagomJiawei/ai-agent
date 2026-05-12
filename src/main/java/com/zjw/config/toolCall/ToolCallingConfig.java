package com.zjw.config.toolCall;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.tool.execution.ToolExecutionException;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 工具调用管理器配置
 *
 * ToolCallingManager 的配置类，支持：
 * - 自定义异常处理器
 * - 全局工具执行策略
 * - 统一的错误处理
 *
 * @author ZhangJw
 * @date 2026年05月08日 14:56
 */
@Slf4j
@Configuration
public class ToolCallingConfig {

    /**
     * 自定义 ToolCallingManager Bean
     * 可以在这里配置自定义的工具观察器、解析器和异常处理器
     *
     * @return ToolCallingManager 实例
     */
    @Bean
    public ToolCallingManager toolCallingManager() {
        log.info("🔧 Creating custom ToolCallingManager with enhanced observability");

        return ToolCallingManager.builder()
                // 可以自定义异常处理器
                .toolExecutionExceptionProcessor(this.customExceptionProcessor())
                .build();
    }

    /**
     * 自定义工具执行异常处理器
     * 提供统一的异常处理逻辑，包括：
     * - 详细的错误日志记录
     * - 根据异常类型返回不同的错误消息
     * - 对模型友好的错误提示
     *
     * @return 异常处理器
     */
    @Bean
    public ToolExecutionExceptionProcessor customExceptionProcessor() {
        return exception -> {
            String toolName = exception.getToolDefinition().name();
            Throwable cause = exception.getCause();
            String errorMessage = cause != null ? cause.getMessage() : exception.getMessage();

            // 记录详细错误日志
            log.error("❌ Tool execution failed - Tool: '{}', Error: {}", toolName, errorMessage, exception);

            // 根据异常类型决定处理策略
            switch (cause) {
                case IOException ioException -> {
                    log.warn("⚠️  IO/Network error in tool '{}': {}", toolName, errorMessage);
                    return String.format("工具 '%s' 无法访问外部资源，可能是网络问题或资源不可用。建议：检查网络连接或尝试其他方法。", toolName);
                }
                case SecurityException securityException -> {
                    log.error("🔒 Security violation in tool '{}': {}", toolName, errorMessage);
                    throw new ToolExecutionException(
                            exception.getToolDefinition(),
                            new SecurityException("工具执行被安全策略阻止：" + errorMessage)
                    );
                }
                case TimeoutException timeoutException -> {
                    log.warn("⏱️  Timeout in tool '{}': {}", toolName, errorMessage);
                    return String.format("工具 '%s' 执行超时，操作耗时过长。建议：简化请求或稍后重试。", toolName);
                }
                case IllegalArgumentException illegalArgumentException -> {
                    log.warn("⚠️  Invalid arguments for tool '{}': {}", toolName, errorMessage);
                    return String.format("工具 '%s' 收到无效参数：%s。请检查参数格式和取值范围。", toolName, errorMessage);
                }
                case null, default -> {
                    log.error("💥 Unexpected error in tool '{}': {}", toolName, errorMessage);
                    return String.format("工具 '%s' 执行失败：%s。详细信息已记录，请联系管理员或尝试其他方法。",
                            toolName,
                            errorMessage != null && errorMessage.length() > 200
                                    ? errorMessage.substring(0, 200) + "..."
                                    : errorMessage);
                }
            }
        };
    }
}
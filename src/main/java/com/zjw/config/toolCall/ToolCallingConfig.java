package com.zjw.config.toolCall;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.tool.execution.ToolExecutionExceptionProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

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
     * 提供更详细的错误日志和异常处理逻辑
     *
     * @return 异常处理器
     */
    @Bean
    public ToolExecutionExceptionProcessor customExceptionProcessor() {
        return exception -> {
            String toolName = exception.getToolDefinition().name();
            Throwable cause = exception.getCause();

            log.error("❌ Tool execution failed - Tool: {}, Error: {}",
                    toolName,
                    cause != null ? cause.getMessage() : exception.getMessage(),
                    exception);

            // 根据异常类型决定处理策略
            if (cause instanceof IOException) {
                // 网络/IO错误返回友好消息给模型
                return "无法访问外部资源，请尝试其他方法。";
            } else if (cause instanceof SecurityException) {
                // 安全异常直接抛出
                throw exception;
            } else {
                // 其他异常返回详细信息给模型
                return String.format("工具 '%s' 执行失败: %s",
                        toolName,
                        cause != null ? cause.getMessage() : exception.getMessage());
            }
        };
    }
}
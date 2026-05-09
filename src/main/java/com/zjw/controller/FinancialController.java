package com.zjw.controller;

import com.zjw.app.FinancialApp;
import com.zjw.common.BaseResponse;
import com.zjw.common.ResultUtils;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 控制器 - 提供手动工具执行的API接口
 *
 * @author ZhangJw
 * @date 2026年05月08日 14:47
 */
@RestController
@RequestMapping("/financial")
@Slf4j
public class FinancialController {

    @Resource
    private FinancialApp financialApp;

    /**
     * 简单对话
     */
    @PostMapping("/chat")
    public BaseResponse<String> chat(@RequestBody ChatRequest request) {
        String result = financialApp.doChat(request.getMessage(), request.getChatId());
        return ResultUtils.success(result);
    }

    /**
     * 使用工具的对话（框架自动控制）
     */
    @PostMapping("/chat-with-tools")
    public BaseResponse<String> chatWithTools(@RequestBody ChatRequest request) {
        String result = financialApp.doChatWithTools(request.getMessage(), request.getChatId());
        return ResultUtils.success(result);
    }

    /**
     * 使用工具的对话（手动控制）- 推荐使用，具有更好的可观测性
     */
    @PostMapping("/chat-with-manual-tools")
    public BaseResponse<String> chatWithManualTools(@RequestBody ChatRequest request) {
        String result = financialApp.doChatWithManualToolControl(request.getMessage(), request.getChatId());
        return ResultUtils.success(result);
    }

    /**
     * 使用工具的对话（手动控制，自定义迭代次数）
     */
    @PostMapping("/chat-with-manual-tools-custom")
    public BaseResponse<String> chatWithManualToolsCustom(
            @RequestBody ChatRequestWithIterations request) {
        String result = financialApp.doChatWithManualToolControl(request.getMessage(), request.getChatId(),
                request.getMaxIterations()
        );
        return ResultUtils.success(result);
    }

    /**
     * 使用RAG的对话
     */
    @PostMapping("/chat-with-rag")
    public BaseResponse<String> chatWithRag(@RequestBody ChatRequest request) {
        String result = financialApp.doChatWithRag(request.getMessage(), request.getChatId());
        return ResultUtils.success(result);
    }

    @Data
    public static class ChatRequest {
        private String message;
        private String chatId = "default-chat";
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ChatRequestWithIterations extends ChatRequest {
        private int maxIterations = 5;
    }
}
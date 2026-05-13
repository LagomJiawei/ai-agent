package com.zjw.controller;

import com.zjw.agent.LiCaiManus;
import com.zjw.app.FinancialApp;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

/**
 * 接口
 *
 * @author ZhangJw
 * @date 2026年05月13日 6:34
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private FinancialApp financialApp;

    @Resource
    private List<ToolCallback> allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    @Resource
    private ToolCallingManager toolCallingManager;

    /**
     * 同步调用 FinancialApp
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/financial_app/chat/sync")
    public String doChatWithFinancialAppSync(String message, String chatId) {
        return financialApp.doChat(message, chatId);
    }

    /**
     * 流式调用 FinancialApp
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/financial_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithFinancialAppSse(String message, String chatId) {
        return financialApp.doChatByStream(message, chatId);
    }

    /**
     * 流式调用 FinancialApp
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/financial_app/chat/sse/serverSentEvent")
    public Flux<ServerSentEvent<String>> doChatWithFinancialAppSse_2(String message, String chatId) {
        return financialApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     *  流式调用 FinancialApp
     *
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/financial_app/chat/sse/emitter")
    public SseEmitter doChatWithFinancialAppSse_3(String message, String chatId) {
        // 创建一个超时时间较长的 SseEmitter
        SseEmitter emitter = new SseEmitter(180000L); // 3分钟超时
        // 获取 Flux 数据流并直接订阅
        financialApp.doChatByStream(message, chatId)
                .subscribe(
                        // 处理每条消息
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        // 处理错误
                        emitter::completeWithError,
                        // 处理完成
                        emitter::complete
                );
        // 返回emitter
        return emitter;
    }

    /**
     * 流式调用 Manus 智能体
     *
     * @param message
     * @return
     */
    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        LiCaiManus liCaiManus = new LiCaiManus(allTools, dashscopeChatModel, toolCallingManager);
        return liCaiManus.runStream(message);
    }
}

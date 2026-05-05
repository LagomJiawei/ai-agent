package com.zjw.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

/**
 * 自定义 日志 Advisor
 * 打印 info 级别日志、只输出单次用户提示词和 AI 回复的文本
 *
 * @author ZhangJw
 * @date 2026年05月03日 13:20
 */
@Slf4j
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {

    /**
     * 拦截器名称（唯一）
     *
     * @return
     */
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 拦截器执行顺序（值越小，越靠前）
     *
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }

    private ChatClientRequest before(ChatClientRequest request) {
        log.info("AI Request: {}", request.prompt().getUserMessage().getText());
        return request;
    }

    private void observeAfter(ChatClientResponse response) {
        if (response != null && response.chatResponse() != null
                && response.chatResponse().getResult() != null
                && response.chatResponse().getResult().getOutput() != null) {
            log.info("AI Response: {}", response.chatResponse().getResult().getOutput().getText());
        }
    }

    /**
     * 处理同步（非流式）请求/响应
     *
     * @param request
     * @param chain
     * @return
     */
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        request = this.before(request);
        // 继续执行AdvisorChain中的下一个拦截器
        ChatClientResponse response = chain.nextCall(request);
        this.observeAfter(response);
        return response;
    }

    /**
     * 处理流式请求/响应
     *
     * @param request
     * @param chain
     * @return
     */
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        request = this.before(request);
        // 继续执行AdvisorChain中的下一个拦截器
        Flux<ChatClientResponse> response = chain.nextStream(request);
        // 创建消息聚合器实例MessageAggregator，将流式响应序列（Flux：包含多个响应片段）聚合成完整的响应之后，再回调observeAfter方法去记录完整的响应日志
        return new ChatClientMessageAggregator().aggregateChatClientResponse(response, this::observeAfter);
    }
}
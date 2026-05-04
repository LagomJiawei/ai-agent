package com.zjw.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

/**
 * 自定义 日志 Advisor
 * 打印 info 级别日志、只输出单次用户提示词和 AI 回复的文本
 *
 * @author ZhangJw
 * @date 2026年05月03日 13:20
 */
@Slf4j
public class MyLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

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

    private AdvisedRequest before(AdvisedRequest request) {
        log.info("AI Request: {}", request.userText());
        return request;
    }

    private void observeAfter(AdvisedResponse advisedResponse) {
        log.info("AI Response: {}", advisedResponse.response().getResult().getOutput().getText());
    }

    /**
     * 处理同步（非流式）请求/响应
     *
     * @param advisedRequest
     * @param chain
     * @return
     */
    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        advisedRequest = this.before(advisedRequest);
        // 继续执行AdvisorChain中的下一个拦截器
        AdvisedResponse advisedResponse = chain.nextAroundCall(advisedRequest);
        this.observeAfter(advisedResponse);
        return advisedResponse;
    }

    /**
     * 处理流式请求/响应
     *
     * @param advisedRequest
     * @param chain
     * @return
     */
    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        advisedRequest = this.before(advisedRequest);
        // 继续执行AdvisorChain中的下一个拦截器
        Flux<AdvisedResponse> advisedResponses = chain.nextAroundStream(advisedRequest);
        // 创建消息聚合器实例MessageAggregator，将流式响应序列（Flux：包含多个响应片段）聚合成完整的响应之后，再回调observeAfter方法去记录完整的响应日志
        return (new MessageAggregator()).aggregateAdvisedResponse(advisedResponses, this::observeAfter);
    }
}
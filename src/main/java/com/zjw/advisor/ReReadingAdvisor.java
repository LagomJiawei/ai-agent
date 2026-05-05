package com.zjw.advisor;

import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.prompt.PromptTemplate;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义 Re2 Advisor
 *
 * @author ZhangJw
 * @date 2026年05月03日 14:03
 */
public class ReReadingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

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
     * 拦截器执行顺序
     *
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 执行请求前，改写 Prompt
     *
     * @param advisedRequest
     * @return
     */
    private AdvisedRequest before(AdvisedRequest advisedRequest) {
        // 创建包含变量的prompt
        String template = """
                {re2_input_query}
                Read the question again: {re2_input_query}
                """;

        // 构建PromptTemplate
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Map<String, Object> variables = new HashMap<>();
        variables.put("re2_input_query", advisedRequest.userText());

        // 渲染模板
        String renderedText = promptTemplate.render(variables);

        return AdvisedRequest.from(advisedRequest)
                .userText(renderedText)
                .userParams(variables)
                .build();
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
        return chain.nextAroundCall(this.before(advisedRequest));
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
        return chain.nextAroundStream(this.before(advisedRequest));
    }
}
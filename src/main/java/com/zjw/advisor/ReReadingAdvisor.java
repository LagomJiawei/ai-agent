package com.zjw.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
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
public class ReReadingAdvisor implements CallAdvisor, StreamAdvisor {

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
     * @param request
     * @return
     */
    private ChatClientRequest before(ChatClientRequest request) {
        // 创建包含变量的prompt
        String template = """
                {re2_input_query}
                Read the question again: {re2_input_query}
                """;

        // 构建PromptTemplate
        PromptTemplate promptTemplate = new PromptTemplate(template);
        Map<String, Object> variables = new HashMap<>();
        variables.put("re2_input_query", request.prompt().getUserMessage().getText());

        // 渲染模板
        String renderedText = promptTemplate.render(variables);

        return request.mutate()
                .prompt(request.prompt().augmentUserMessage(renderedText))
                .build();
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
        return chain.nextCall(this.before(request));
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
        return chain.nextStream(this.before(request));
    }
}
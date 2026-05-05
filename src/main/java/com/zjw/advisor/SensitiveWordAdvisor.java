package com.zjw.advisor;

import com.github.houbb.sensitive.word.api.IWordResult;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import com.github.houbb.sensitive.word.support.deny.WordDenys;
import com.github.houbb.sensitive.word.support.result.WordResultHandlers;
import com.zjw.config.CustomWordDeny;
import com.zjw.config.SensitiveWordConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 自定义敏感词校验 Advisor
 * 在请求发送到LLM之前检查用户输入是否包含敏感词
 *
 * @author ZhangJw
 * @date 2026年05月04日 14:02
 */
@Slf4j
public class SensitiveWordAdvisor implements CallAdvisor, StreamAdvisor {

    private static final String DEFAULT_REPLACEMENT = "***";
    private static final String STRATEGY_BLOCK = "BLOCK";
    private static final String STRATEGY_REPLACE = "REPLACE";

    private final SensitiveWordConfig sensitiveWordConfig;

    private final CustomWordDeny customWordDeny;

    private SensitiveWordBs sensitiveWordBs;

    /**
     * 构造方法注入配置
     */
    public SensitiveWordAdvisor(SensitiveWordConfig sensitiveWordConfig, CustomWordDeny customWordDeny) {
        this.sensitiveWordConfig = sensitiveWordConfig;
        this.customWordDeny = customWordDeny;
    }

    /**
     * 初始化敏感词库（触发时机：SensitiveWordAdvisor 这个Bean实例化之后，在【所有】依赖注入（如 @Autowired、构造方法注入）完成后，在 Bean 正式投入使用之前）
     */
    @PostConstruct
    public void init() {
        if (!sensitiveWordConfig.isEnabled()) {
            log.info("敏感词校验已禁用");
            return;
        }

        List<String> words = sensitiveWordConfig.getWords();
        log.info("开始初始化敏感词库，共 {} 个敏感词", words.size());
        if (!words.isEmpty()) {
            log.info("已加载的敏感词: {}", words);
        }

        // 初始化 SensitiveWordBs，使用自定义的敏感词库
        this.sensitiveWordBs = SensitiveWordBs.newInstance()
                .wordDeny(WordDenys.chains(WordDenys.defaults(), customWordDeny))
                .init();
        log.info("敏感词库初始化完成");
    }

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
        // 优先执行，在其他Advisor之前进行违禁词检查
        return -1;
    }

    private ChatClientRequest before(ChatClientRequest request) {
        if (!sensitiveWordConfig.isEnabled()) {
            return request;
        }

        String userText = request.prompt().getUserMessage().getText();
        if (!this.hasSensitiveWord(userText)) {
            log.debug("敏感词校验通过");
            return request;
        }

        String firstSensitiveWord = this.getFirstSensitiveWord(userText);
        log.info("用户输入包含敏感词 [{}]，当前策略: {}", firstSensitiveWord, sensitiveWordConfig.getStrategy());

        return switch (sensitiveWordConfig.getStrategy().toUpperCase()) {
            case STRATEGY_BLOCK -> throw new IllegalArgumentException("您的输入包含不当内容，请修改后重试");
            case STRATEGY_REPLACE -> {
                String cleanText = this.replaceSensitiveWords(userText);
                log.info("已将敏感词替换为 {}，原文: {}, 处理后: {}", DEFAULT_REPLACEMENT, userText, cleanText);
                yield request.mutate()
                        .prompt(request.prompt().augmentUserMessage(cleanText))
                        .build();
            }
            default -> {
                log.error("未知的处理策略: {}，默认采用拦截策略", sensitiveWordConfig.getStrategy());
                throw new IllegalArgumentException("您的输入包含不当内容，请修改后重试");
            }
        };
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
        Flux<ChatClientResponse> responses = chain.nextStream(this.before(request));
        return new ChatClientMessageAggregator().aggregateChatClientResponse(responses, response ->
                log.debug("流式响应完成")
        );
    }

    private boolean hasSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        if (sensitiveWordBs == null) {
            log.warn("敏感词库未初始化");
            return false;
        }
        return sensitiveWordBs.contains(text);
    }

    /**
     * 获取第一个匹配到的敏感词
     *
     * @param text 待检查的文本
     * @return 匹配到的敏感词，如果没有则返回null
     */
    private String getFirstSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        List<IWordResult> results = SensitiveWordHelper.findAll(text, WordResultHandlers.raw());
        return results.isEmpty() ? null : this.extractWord(text, results.getFirst());
    }

    /**
     * 替换文本中的敏感词为***（使用 DFA 算法）
     *
     * @param text 原始文本
     * @return 替换后的文本
     */
    private String replaceSensitiveWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        List<IWordResult> results = SensitiveWordHelper.findAll(text, WordResultHandlers.raw());
        if (results.isEmpty()) {
            return text;
        }

        return this.buildReplacedText(text, results);
    }

    private String extractWord(String text, IWordResult result) {
        return text.substring(result.startIndex(), result.endIndex());
    }

    private String buildReplacedText(String text, List<IWordResult> results) {
        StringBuilder result = new StringBuilder();
        int lastIndex = 0;

        for (IWordResult wordResult : results) {
            result.append(text, lastIndex, wordResult.startIndex())
                    .append(DEFAULT_REPLACEMENT);
            lastIndex = wordResult.endIndex();
        }

        result.append(text.substring(lastIndex));
        return result.toString();
    }
}

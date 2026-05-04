package com.zjw.config;

import com.github.houbb.sensitive.word.api.IWordDeny;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义敏感词库的提供者
 *
 * @author ZhangJw
 * @date 2026年05月04日 15:30
 */
@Slf4j
@Component
public class CustomWordDeny implements IWordDeny {

    @Resource
    private SensitiveWordConfig sensitiveWordConfig;

    @Override
    public List<String> deny() {
        List<String> words = sensitiveWordConfig.getWords();
        log.debug("加载自定义敏感词，共 {} 个", words.size());
        return words;
    }
}
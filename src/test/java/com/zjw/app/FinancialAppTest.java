package com.zjw.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

/**
 * @author ZhangJw
 * @date 2026年05月03日 10:23
 */
@SpringBootTest
public class FinancialAppTest {

    @Resource
    private FinancialApp financialApp;

    /**
     * 测试简单对话
     */
    @Test
    void test() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是张三";
        String answer = financialApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);

        // 第二轮
        message = "我最近想理财";
        answer = financialApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);

        // 第三轮
        message = "给我推荐一个最具性价比的理财组合";
        answer = financialApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    /**
     * 测试生成报告
     */
    @Test
    void testChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是张三。给我推荐一个最具性价比的理财组合";
        FinancialApp.PsychologyReport psychologyReport = financialApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(psychologyReport);
    }

    /**
     * 测试敏感词过滤
     */
    @Test
    void testSensitiveWord() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我最近感觉很暴力";
        FinancialApp.PsychologyReport psychologyReport = financialApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(psychologyReport);
    }

    /**
     * 测试 rag
     */
    @Test
    void testChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我想理财，但是不了解这块内容。";
        String answer = financialApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }
}
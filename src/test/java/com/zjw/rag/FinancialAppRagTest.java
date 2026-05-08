package com.zjw.rag;

import com.zjw.app.FinancialApp;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/**
 * @author ZhangJw
 * @date 2026年05月08日 7:27
 */
public class FinancialAppRagTest {

    @Resource
    private FinancialApp financialApp;

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
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
public class PsychologyAppTest {

    @Resource
    private PsychologyApp psychologyApp;

    @Test
    void test() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是张三";
        String answer = psychologyApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);

        // 第二轮
        message = "我和女朋友吵架了";
        answer = psychologyApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);

        // 第三轮
        message = "我和谁吵架了？刚跟你说过，帮我回忆一下";
        answer = psychologyApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String message = "你好，我是张三。我和女朋友吵架了，不知道该怎么哄她";
        PsychologyApp.PsychologyReport psychologyReport = psychologyApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(psychologyReport);
    }

    @Test
    void testSensitiveWordMixedContent() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我最近感觉很暴力";

        Exception exception = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            psychologyApp.doChat(message, chatId);
        });

        Assertions.assertTrue(exception.getMessage().contains("不当内容"));
    }
}
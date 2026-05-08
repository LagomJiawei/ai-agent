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
        message = "给我推荐一个最具性价比理财组合";
        answer = financialApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    /**
     * 测试生成报告
     */
    @Test
    void testChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "你好，我是张三。给我推荐一个最具性价比理财组合";
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
     * 测试工具调用
     */
    @Test
    void testChatWithTools() {
        // 测试联网搜索问题的答案
        this.testMessage("周末想去上海，推荐几个小众打卡地？");

        // 测试网页抓取：理财案例分析
        this.testMessage("最近亏了，看看https://www.xxx.com上其他人是怎么解决的？");

        // 测试资源下载：图片下载
        this.testMessage("直接下载一张适合做手机壁纸的图片为文件");

        // 测试终端操作：执行代码
        this.testMessage("执行 Python3 脚本来生成分析报告");

        // 测试文件操作：保存用户档案
        this.testMessage("保存我的理财档案为文件");

        // 测试 PDF 生成
        this.testMessage("生成一份‘理财计划’PDF");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = financialApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

}
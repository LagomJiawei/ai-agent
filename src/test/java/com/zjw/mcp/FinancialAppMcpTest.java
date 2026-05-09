package com.zjw.mcp;

import com.zjw.app.FinancialApp;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

/**
 * @author ZhangJw
 * @date 2026年05月09日 10:25
 */
@SpringBootTest
public class FinancialAppMcpTest {

    @Resource
    private FinancialApp financialApp;

    @Test
    void doChatWithMcp() {
        String chatId = UUID.randomUUID().toString();
        // 测试地图 MCP
        String message = "";
        String answer =  financialApp.doChatWithMcp(message, chatId);
    }

}
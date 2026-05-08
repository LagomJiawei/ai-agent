package com.zjw.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author ZhangJw
 * @date 2026年05月08日 8:02
 */
@SpringBootTest
public class WebSearchToolTest {

    @Value("${search-api.api-key}")
    private String searchApiKey;

    @Test
    void searchWeb() {
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        String query = "提供一份小白编程指南";
        String result = webSearchTool.searchWeb(query);
        Assertions.assertNotNull(result);
    }
}
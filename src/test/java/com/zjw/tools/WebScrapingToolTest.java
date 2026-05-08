package com.zjw.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author ZhangJw
 * @date 2026年05月08日 8:20
 */
@SpringBootTest
public class WebScrapingToolTest {

    @Test
    void scrapeWebPage() {
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        String url = "https://www.xxx.com";
        String result = webScrapingTool.scrapeWebPage(url);
        Assertions.assertNotNull(result);
    }
}
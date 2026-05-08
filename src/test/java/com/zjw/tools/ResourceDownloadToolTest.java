package com.zjw.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author ZhangJw
 * @date 2026年05月08日 8:57
 */
@SpringBootTest
public class ResourceDownloadToolTest {

    @Test
    public void downloadResource() {
        ResourceDownloadTool tool = new ResourceDownloadTool();
        String url = "https://www.xxx.com";
        String fileName = "xxx.png";
        String result = tool.downloadResource(url, fileName);
        Assertions.assertNotNull(result);
    }
}
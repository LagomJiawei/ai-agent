package com.zjw.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author ZhangJw
 * @date 2026年05月08日 9:13
 */
@SpringBootTest
public class PDFGenerationToolTest {

    @Test
    void generatePDFLocal() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "xxx.pdf";
        String content = "你好";
        String result = tool.generatePDFLocal(fileName, content);
        Assertions.assertNotNull(result);
    }
}
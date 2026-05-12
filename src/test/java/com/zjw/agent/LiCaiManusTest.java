package com.zjw.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author ZhangJw
 * @date 2026年05月11日 9:56
 */
@SpringBootTest
public class LiCaiManusTest {

    @Resource
    private LiCaiManus liCaiManus;

    @Test
    public void run() {
        String userPrompt = """
                我是理财小白，请帮我制定一份详细的入门版理财计划，
                并以 PDF 格式输出""";
        String answer = liCaiManus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}
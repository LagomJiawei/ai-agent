package com.zjw.agent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;

/**
 * 循环检测器测试
 *
 * @author ZhangJw
 * @date 2026年05月12日 8:51
 */
@SpringBootTest
public class LoopDetectorTest {

    @Test
    public void testNoLoop() {
        LoopDetector detector = new LoopDetector();

        // 记录不同的执行步骤
        detector.recordExecution(1, "第一步执行", Collections.emptyList());
        detector.recordExecution(2, "第二步执行", Collections.emptyList());
        detector.recordExecution(3, "第三步执行", Collections.emptyList());

        LoopDetector.LoopDetectionResult result = detector.checkForLoop();
        Assertions.assertFalse(result.isStuck(), "不应该检测到循环");
    }

    @Test
    public void testDuplicateMessageLoop() {
        LoopDetector detector = new LoopDetector();

        // 记录重复的消息
        String repeatedMessage = "我需要搜索信息";
        detector.recordExecution(1, "初始消息", Collections.emptyList());
        detector.recordExecution(2, repeatedMessage, Collections.emptyList());
        detector.recordExecution(3, repeatedMessage, Collections.emptyList());
        detector.recordExecution(4, repeatedMessage, Collections.emptyList());

        LoopDetector.LoopDetectionResult result = detector.checkForLoop();
        Assertions.assertTrue(result.isStuck(), "应该检测到重复消息循环");
        Assertions.assertEquals(LoopDetector.LoopDetectionResult.LoopType.DUPLICATE_MESSAGE, result.getLoopType());
        System.out.println("检测结果: " + result.getDescription());
        System.out.println("建议: " + result.getSuggestions());
    }

    @Test
    public void testToolCallLoop() {
        LoopDetector detector = new LoopDetector();

        // 创建相同的工具调用记录
        LoopDetector.ToolCallRecord toolCallRecord = new LoopDetector.ToolCallRecord("web_search", "{\"query\":\"test\"}");

        detector.recordExecution(1, "搜索信息", List.of(toolCallRecord));
        detector.recordExecution(2, "继续搜索", List.of(toolCallRecord));
        detector.recordExecution(3, "再次搜索", List.of(toolCallRecord));
        detector.recordExecution(4, "还在搜索", List.of(toolCallRecord));

        LoopDetector.LoopDetectionResult result = detector.checkForLoop();
        Assertions.assertTrue(result.isStuck(), "应该检测到工具调用循环");
        Assertions.assertEquals(LoopDetector.LoopDetectionResult.LoopType.TOOL_CALL_LOOP, result.getLoopType());
        System.out.println("检测结果: " + result.getDescription());
    }

    @Test
    public void testDiagnosticReport() {
        LoopDetector detector = new LoopDetector();

        detector.recordExecution(1, "第一步", Collections.emptyList());
        detector.recordExecution(2, "第二步", Collections.emptyList());
        detector.recordExecution(3, "第三步", Collections.emptyList());

        String report = detector.generateDiagnosticReport();
        Assertions.assertNotNull(report);
        Assertions.assertTrue(report.contains("执行历史诊断报告"));
        System.out.println(report);
    }

    @Test
    public void testReset() {
        LoopDetector detector = new LoopDetector();

        detector.recordExecution(1, "消息1", Collections.emptyList());
        detector.recordExecution(2, "消息2", Collections.emptyList());

        Assertions.assertEquals(2, detector.getExecutionHistory().size());

        detector.reset();
        Assertions.assertEquals(0, detector.getExecutionHistory().size());
    }
}
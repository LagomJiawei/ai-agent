package com.zjw.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author ZhangJw
 * @date 2026年05月08日 8:52
 */
@SpringBootTest
public class TerminalOperationToolTest {

    @Test
    void executeTerminalCommand() {
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        String command = "ls -l";
        String result = terminalOperationTool.executeTerminalCommand(command);
        Assertions.assertNotNull(result);
    }
}
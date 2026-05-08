package com.zjw.tools;

import org.springframework.ai.tool.annotation.Tool;

/**
 * 停止调用工具：让智能体能够合理地中断工具调用链，避免无限循环
 *
 * @author ZhangJw
 * @date 2026年05月08日 9:23
 */
public class TerminateTool {

    @Tool(description = """
            Terminate the interaction when the request is met OR the assistant cannot proceed further with the task.
            "When you have finished all the tasks, call this tool to end the work.
            """)
    public String doTerminate() {
        return "任务结束";
    }
}
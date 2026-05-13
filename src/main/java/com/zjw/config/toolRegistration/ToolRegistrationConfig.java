package com.zjw.config.toolRegistration;

import com.zjw.tools.FileOperationTool;
import com.zjw.tools.PDFGenerationTool;
import com.zjw.tools.ResourceDownloadTool;
import com.zjw.tools.TerminalOperationTool;
import com.zjw.tools.TerminateTool;
import com.zjw.tools.WebScrapingTool;
import com.zjw.tools.WebSearchTool;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 统一注册和管理所有AI工具：注册到Spring容器，使LLM能够发现并调用这些工具完成各种任务
 *
 * @author ZhangJw
 * @date 2026年05月08日 9:19
 */
@Configuration
public class ToolRegistrationConfig {

    @Value("${search-api.api-key}")
    private String searchApiKey;

    @Bean
    public List<ToolCallback> allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        TerminateTool terminateTool = new TerminateTool();
        return List.of(ToolCallbacks.from(
                fileOperationTool,
                webSearchTool,
                webScrapingTool,
                resourceDownloadTool,
                terminalOperationTool,
                pdfGenerationTool,
                terminateTool
        ));
    }
}
package com.zjw;

import com.zjw.service.WeatherService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * SpringBoot启动时，自动创建 ToolCallbackProvider Bean
 *
 * @author ZhangJw
 * @date 2026年05月09日 11:05
 */
@SpringBootApplication
public class McpServerApplication {
    @Bean
    public ToolCallbackProvider weatherTools(WeatherService weatherService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(weatherService)
                .build();
    }
}

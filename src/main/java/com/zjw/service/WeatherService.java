package com.zjw.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * @author ZhangJw
 * @date 2026年05月09日 11:03
 */
@Service
public class WeatherService {
    @Tool(description = "获取指定城市的天气信息")
    public String getWeather(
            @ToolParam(description = "城市名称，如北京、上海") String cityName) {
        // 实现天气查询逻辑
        return "城市" + cityName + "的天气是晴天，温度22°C";
    }
}

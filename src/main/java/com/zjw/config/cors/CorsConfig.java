package com.zjw.config.cors;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 自定义全局跨域配置：允许前端应用跨域访问后端 API
 *
 * @author ZhangJw
 * @date 2026年05月01日 14:10
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")   // 覆盖后端所有 API
                .allowCredentials(true)         // 允许携带 Cookie/认证信息
                .allowedOriginPatterns("*")     // 放行全部域名（使用 patterns 避免与 credentials 冲突【Spring 5.3+ 的安全限制，防止凭据泄露】）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的 HTTP 方法
                .allowedHeaders("*")            // 允许任意请求头
                .exposedHeaders("*");           // 暴露所有响应头给前端
    }
}

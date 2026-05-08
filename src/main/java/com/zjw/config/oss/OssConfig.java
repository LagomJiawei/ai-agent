package com.zjw.config.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置
 *
 * @author ZhangJw
 * @date 2026年05月08日 14:01
 */
@Data
@Configuration
public class OssConfig {

    @Value("${oss.aliyun.endpoint}")
    private String endpoint;

    @Value("${oss.aliyun.access-key-id}")
    private String accessKeyId;

    @Value("${oss.aliyun.access-key-secret}")
    private String accessKeySecret;

    @Value("${oss.aliyun.bucket-name}")
    private String bucketName;

    @Value("${oss.aliyun.pdf-path-prefix:pdf-files}")
    private String pdfPathPrefix;

    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
}
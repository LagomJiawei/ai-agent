package com.zjw.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zjw.exception.BusinessException;
import com.zjw.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ZhangJw
 * @date 2026年05月07日 14:44
 */
@Slf4j
@Service
public class ThirdPartyTranslationService implements TranslationService{

    @Value("${translation.enabled:true}")
    private boolean enabled;

    @Value("${translation.provider:BAIDU}")
    private String provider;

    @Value("${translation.target-language:zh}")
    private String defaultTargetLanguage;

    // 百度翻译配置
    @Value("${translation.baidu.app-id:}")
    private String baiduAppId;

    @Value("${translation.baidu.app-key:}")
    private String baiduAppKey;

    // 有道翻译配置
    @Value("${translation.youdao.app-key:}")
    private String youdaoAppKey;

    @Value("${translation.youdao.app-secret:}")
    private String youdaoAppSecret;

    // DeepL配置
    @Value("${translation.deepl.api-key:}")
    private String deeplApiKey;

    @Value("${translation.deepl.endpoint:api-free.deepl.com}")
    private String deeplEndpoint;

    @Override
    public String translate(String text, String targetLanguage) {
        if (!enabled) {
            log.debug("翻译功能已禁用，返回原文");
            return text;
        }

        if (StrUtil.isBlank(text)) {
            return text;
        }

        log.debug("开始翻译，提供商: {}, 原文: {}, 目标语言: {}", provider, text, targetLanguage);

        try {
            String translatedText = switch (provider.toUpperCase()) {
                case "BAIDU" -> this.translateWithBaidu(text, targetLanguage);
                case "YOUDAO" -> this.translateWithYoudao(text, targetLanguage);
                case "DEEPL" -> this.translateWithDeepl(text, targetLanguage);
                default -> {
                    log.warn("未知的翻译提供商: {}, 使用原文", provider);
                    yield text;
                }
            };

            log.debug("翻译完成，译文: {}", translatedText);
            return translatedText;

        } catch (Exception e) {
            log.error("翻译失败，返回原文。错误信息: {}", e.getMessage(), e);
            return text;
        }
    }

    @Override
    public String translate(String text) {
        return translate(text, defaultTargetLanguage);
    }

    /**
     * 使用百度翻译API
     */
    private String translateWithBaidu(String text, String targetLanguage) {
        String translatedText = "";
        String salt = String.valueOf(System.currentTimeMillis());
        String sign = baiduAppId + text + salt + baiduAppKey;
        String md5Sign = SecureUtil.md5(sign);

        String url = "https://fanyi-api.baidu.com/api/trans/vip/translate";

        Map<String, Object> params = new HashMap<>();
        params.put("q", text);
        params.put("from", "auto");
        params.put("to", targetLanguage);
        params.put("appid", baiduAppId);
        params.put("salt", salt);
        params.put("sign", md5Sign);

        HttpResponse response = HttpRequest.get(url)
                .form(params)
                .timeout(5000)
                .execute();

        if (!response.isOk()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "百度翻译API调用失败: " + response.getStatus());
        }

        JSONObject jsonObject = JSONUtil.parseObj(response.body());

        // 解析翻译结果
        if (jsonObject.containsKey("trans_result")) {
            translatedText = jsonObject.getJSONArray("trans_result")
                    .getJSONObject(0)
                    .getStr("dst");
        }
        return translatedText;
    }

    /**
     * 使用有道翻译API
     */
    private String translateWithYoudao(String text, String targetLanguage) {
        String translatedText = "";
        String salt = String.valueOf(System.currentTimeMillis());
        String input = text.length() > 20 ? text.substring(0, 20) : text;
        String sign = SecureUtil.md5(youdaoAppKey + input + salt + youdaoAppSecret);

        String url = "https://openapi.youdao.com/api";

        Map<String, Object> params = new HashMap<>();
        params.put("q", text);
        params.put("from", "auto");
        params.put("to", targetLanguage);
        params.put("appKey", youdaoAppKey);
        params.put("salt", salt);
        params.put("sign", sign);
        params.put("signType", "v3");
        params.put("curtime", String.valueOf(System.currentTimeMillis() / 1000));

        HttpResponse response = HttpRequest.get(url)
                .form(params)
                .timeout(5000)
                .execute();

        if (!response.isOk()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "有道翻译API调用失败: " + response.getStatus());
        }

        JSONObject jsonObject = JSONUtil.parseObj(response.body());

        // 解析翻译结果
        if (jsonObject.containsKey("translation")) {
            translatedText = jsonObject.getJSONArray("translation").getStr(0);
        }
        return translatedText;
    }

    /**
     * 使用DeepL翻译API
     */
    private String translateWithDeepl(String text, String targetLanguage) {
        String translatedText = "";
        String url = "https://" + deeplEndpoint + "/v2/translate";

        // DeepL需要将中文目标语言转换为特定代码
        String deeplTargetLang = this.convertToDeeplLanguageCode(targetLanguage);

        HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "DeepL-Auth-Key " + deeplApiKey)
                .header("Content-Type", "application/json")
                .body(JSONUtil.createObj()
                        .set("text", new String[]{text})
                        .set("target_lang", deeplTargetLang)
                        .toString())
                .timeout(5000)
                .execute();

        if (!response.isOk()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "DeepL翻译API调用失败: " + response.getStatus());
        }

        JSONObject jsonObject = JSONUtil.parseObj(response.body());

        // 解析翻译结果
        if (jsonObject.containsKey("translations")) {
            translatedText = jsonObject.getJSONArray("translations")
                    .getJSONObject(0)
                    .getStr("text");
        }
        return translatedText;
    }

    /**
     * 转换语言代码为DeepL格式
     */
    private String convertToDeeplLanguageCode(String languageCode) {
        return switch (languageCode.toLowerCase()) {
            case "zh", "zh-cn" -> "ZH";
            case "en" -> "EN";
            case "ja" -> "JA";
            case "ko" -> "KO";
            case "fr" -> "FR";
            case "de" -> "DE";
            case "es" -> "ES";
            case "it" -> "IT";
            case "pt" -> "PT";
            case "ru" -> "RU";
            default -> languageCode.toUpperCase();
        };
    }
}
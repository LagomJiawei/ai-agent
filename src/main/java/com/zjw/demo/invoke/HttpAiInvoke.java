//package com.zjw.demo.invoke;
//
//import cn.hutool.http.HttpRequest;
//import cn.hutool.http.HttpResponse;
//import cn.hutool.json.JSONObject;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * HTTP接入LLM的示例代码
// *
// * @author ZhangJw
// * @date 2026年05月02日 7:26
// */
//public class HttpAiInvoke {
//
//    /**
//     * 调用通义千问模型进行文本生成
//     */
//    public static void main(String[] args) {
//        String API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation" ;
//        // 建议从环境变量中获取API Key，避免硬编码
//        String API_KEY = TestApiKey.API_KEY;
//
//        // 构建请求头
//        Map<String, String> headers = new HashMap<>();
//        headers.put("Authorization", "Bearer " + API_KEY);
//        headers.put("Content-Type", "application/json");
//
//        // 构建请求体
//        JSONObject requestBody = new JSONObject();
//        requestBody.set("model", "qwen-plus");
//
//        // 构建消息列表
//        List<JSONObject> messages = new ArrayList<>();
//
//        JSONObject systemMsg = new JSONObject();
//        systemMsg.set("role", "system");
//        systemMsg.set("content", "You are a helpful assistant.");
//        messages.add(systemMsg);
//
//        JSONObject userMsg = new JSONObject();
//        userMsg.set("role", "user");
//        userMsg.set("content", "你是谁？");
//        messages.add(userMsg);
//
//        JSONObject input = new JSONObject();
//        input.set("messages", messages);
//        requestBody.set("input", input);
//
//        JSONObject parameters = new JSONObject();
//        parameters.set("result_format", "message");
//        requestBody.set("parameters", parameters);
//
//        // 发送HTTP POST请求
//        HttpResponse response = HttpRequest.post(API_URL)
//                .addHeaders(headers)
//                .body(requestBody.toString())
//                .timeout(20000)
//                .execute(); // 执行请求
//
//        // 处理响应
//        if (response.isOk()) {
//            System.out.println("请求成功，响应内容：");
//            System.out.println(response.body());
//        } else {
//            System.out.println("请求失败，状态码: " + response.getStatus());
//            System.out.println("错误信息: " + response.body());
//        }
//    }
//}
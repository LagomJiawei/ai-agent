//package com.zjw.demo.invoke;
//
//import dev.langchain4j.community.model.dashscope.QwenChatModel;
//import dev.langchain4j.model.chat.ChatModel;
//
///**
// * LangChain4j接入LLM的示例代码
// *
// * @author ZhangJw
// * @date 2026年05月02日 8:52
// */
//public class LangChainAiInvoke {
//
//    public static void main(String[] args) {
//        ChatModel qwenModel = QwenChatModel.builder()
//                .apiKey(TestApiKey.API_KEY)
//                .modelName("qwen-max")
//                .build();
//        String answer = qwenModel.chat("你是谁？");
//        System.out.println(answer);
//    }
//}
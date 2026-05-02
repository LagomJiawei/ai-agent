//package com.zjw.demo.invoke;
//
//import jakarta.annotation.Resource;
//import org.springframework.ai.chat.messages.AssistantMessage;
//import org.springframework.ai.ollama.OllamaChatModel;
//import org.springframework.ai.chat.prompt.Prompt;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
///**
// * 接入本地LLM的示例代码
// *
// * @author ZhangJw
// * @date 2026年05月02日 9:58
// */
//@Component
//public class OllamaAiInvoke implements CommandLineRunner {
//
//    @Resource
//    private OllamaChatModel ollamaChatModel;
//
//    @Override
//    public void run(String... args) throws Exception {
//        AssistantMessage assistantMessage = ollamaChatModel.call(new Prompt("你好，你是谁"))
//                .getResult()
//                .getOutput();
//        System.out.println(assistantMessage.getText());
//    }
//}
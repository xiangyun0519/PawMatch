package com.petadopt.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAIConfig {

    @Bean
    public ChatClient dashScopeChatClient(ChatModel dashScopeChatModel) {
        return ChatClient
                .builder(dashScopeChatModel)
                .defaultSystem("你好，我是宠物服务师")
                .build();
    }

    @Bean
    public ChatClient openAiChatClient(ChatModel openAiChatModel) {
        return ChatClient
                .builder(openAiChatModel)
                .defaultSystem("你好，我是宠物服务师")
                .build();
    }
}

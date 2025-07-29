package org.miao.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * 应用程序配置类
 * 配置Spring AI相关的Bean和OpenRouter集成
 */
@Configuration
public class ApplicationConfig {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

    @Autowired
    private OpenRouterProperties openRouterProperties;

    /**
     * 配置RestTemplate Bean，包含超时和重试配置
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        logger.info("配置RestTemplate，超时时间: {}秒", openRouterProperties.getTimeoutSeconds());
        
        return builder
            .setConnectTimeout(Duration.ofSeconds(openRouterProperties.getTimeoutSeconds()))
            .setReadTimeout(Duration.ofSeconds(openRouterProperties.getTimeoutSeconds()))
            .build();
    }

    /**
     * 配置OpenAI API客户端，指向OpenRouter
     * OpenRouter兼容OpenAI API格式
     */
    @Bean
    public OpenAiApi openAiApi() {
        logger.info("配置OpenAI API客户端，基础URL: {}, 默认模型: {}", 
                   openRouterProperties.getBaseUrl(), 
                   openRouterProperties.getDefaultModel());
        
        return new OpenAiApi(
            openRouterProperties.getBaseUrl(),
            openRouterProperties.getApiKey()
        );
    }

    /**
     * 配置OpenAI聊天模型
     * 使用OpenRouter作为后端服务
     */
    @Bean
    public OpenAiChatModel openAiChatModel(OpenAiApi openAiApi) {
        logger.info("配置OpenAI聊天模型，使用模型: {}", openRouterProperties.getDefaultModel());
        
        OpenAiChatOptions defaultOptions = OpenAiChatOptions.builder()
            .withModel(openRouterProperties.getDefaultModel())
            .withTemperature(0.7)
            .withMaxTokens(1000)
            .build();

        return new OpenAiChatModel(openAiApi, defaultOptions);
    }

    /**
     * 配置ChatClient Bean
     * 这是Spring AI的高级聊天客户端接口
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        logger.info("配置ChatClient，启用调试模式: {}", openRouterProperties.isDebugEnabled());
        
        ChatClient.Builder builder = ChatClient.builder(chatModel)
            .defaultOptions(OpenAiChatOptions.builder()
                .withModel(openRouterProperties.getDefaultModel())
                .withTemperature(0.7)
                .withMaxTokens(1000)
                .build());

        // 如果启用调试模式，可以添加额外的配置
        if (openRouterProperties.isDebugEnabled()) {
            logger.debug("ChatClient调试模式已启用");
        }

        return builder.build();
    }
}
package org.miao.service.impl;

import org.miao.config.OpenRouterProperties;
import org.miao.dto.ChatRequest;
import org.miao.dto.ChatResponse;
import org.miao.exception.ApiConnectionException;
import org.miao.exception.InvalidRequestException;
import org.miao.exception.ModelNotAvailableException;
import org.miao.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 聊天服务实现类
 * 使用Spring AI ChatClient与OpenRouter API进行交互
 */
@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final ChatClient chatClient;
    private final OpenRouterProperties openRouterProperties;

    @Autowired
    public ChatServiceImpl(ChatClient chatClient, OpenRouterProperties openRouterProperties) {
        this.chatClient = chatClient;
        this.openRouterProperties = openRouterProperties;
        logger.info("ChatService初始化完成，默认模型: {}", openRouterProperties.getDefaultModel());
    }

    @Override
    public ChatResponse sendMessage(String message) {
        return sendMessage(message, openRouterProperties.getDefaultModel());
    }

    @Override
    public ChatResponse sendMessage(String message, String model) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        // 设置MDC用于日志追踪
        MDC.put("requestId", requestId);
        MDC.put("model", model);
        MDC.put("timestamp", timestamp);
        
        try {
            logger.info("开始处理聊天请求 [{}] - 模型: {}, 消息长度: {}", 
                       requestId, model, message != null ? message.length() : 0);
            
            // 验证输入参数
            validateMessage(message);
            validateModel(model);
            
            // 执行带重试的API调用
            String response = executeWithRetry(message, model, requestId);
            
            logger.info("聊天请求处理成功 [{}] - 响应长度: {}", requestId, response != null ? response.length() : 0);
            return ChatResponse.success(response, model);
            
        } catch (InvalidRequestException | ModelNotAvailableException e) {
            logger.warn("聊天请求参数错误 [{}] - {}", requestId, e.getMessage());
            throw e;
        } catch (ApiConnectionException e) {
            logger.error("聊天请求API连接失败 [{}] - {}", requestId, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("聊天请求处理异常 [{}] - {}", requestId, e.getMessage(), e);
            throw new ApiConnectionException("处理聊天请求时发生未知错误: " + e.getMessage(), e);
        } finally {
            // 清理MDC
            MDC.clear();
        }
    }

    @Override
    public ChatResponse sendMessage(ChatRequest request) {
        if (request == null) {
            throw new InvalidRequestException("请求对象不能为空");
        }
        
        String model = StringUtils.hasText(request.getModel()) ? 
            request.getModel() : openRouterProperties.getDefaultModel();
        
        return sendMessage(request.getMessage(), model);
    }

    @Override
    public List<String> getAvailableModels() {
        logger.debug("获取可用模型列表");
        return openRouterProperties.getAvailableModels();
    }

    @Override
    public boolean isModelAvailable(String model) {
        if (!StringUtils.hasText(model)) {
            return false;
        }
        
        boolean available = openRouterProperties.getAvailableModels().contains(model);
        logger.debug("检查模型可用性: {} -> {}", model, available);
        return available;
    }

    /**
     * 验证消息内容
     */
    private void validateMessage(String message) {
        if (!StringUtils.hasText(message)) {
            throw new InvalidRequestException("消息内容不能为空");
        }
        
        if (message.length() > 4000) {
            throw new InvalidRequestException("消息长度不能超过4000个字符");
        }
    }

    /**
     * 验证模型名称
     */
    private void validateModel(String model) {
        if (!StringUtils.hasText(model)) {
            throw new InvalidRequestException("模型名称不能为空");
        }
        
        if (!isModelAvailable(model)) {
            throw new ModelNotAvailableException("模型 '" + model + "' 不可用，支持的模型: " + 
                openRouterProperties.getAvailableModels());
        }
    }

    /**
     * 执行带重试机制的API调用
     */
    private String executeWithRetry(String message, String model, String requestId) {
        int maxRetries = openRouterProperties.getMaxRetries();
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.debug("执行API调用 [{}] - 尝试次数: {}/{}", requestId, attempt, maxRetries);
                
                long startTime = System.currentTimeMillis();
                
                String response = chatClient
                    .prompt()
                    .user(message)
                    .options(OpenAiChatOptions.builder()
                        .withModel(model)
                        .withTemperature(0.7)
                        .withMaxTokens(1000)
                        .build())
                    .call()
                    .content();
                
                long duration = System.currentTimeMillis() - startTime;
                logger.debug("API调用成功 [{}] - 耗时: {}ms, 尝试次数: {}", requestId, duration, attempt);
                
                return response;
                
            } catch (Exception e) {
                lastException = e;
                logger.warn("API调用失败 [{}] - 尝试次数: {}/{}, 错误: {}", 
                           requestId, attempt, maxRetries, e.getMessage());
                
                // 如果不是最后一次尝试，等待后重试
                if (attempt < maxRetries) {
                    try {
                        long waitTime = calculateBackoffTime(attempt);
                        logger.debug("等待 {}ms 后重试 [{}]", waitTime, requestId);
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        logger.error("重试等待被中断 [{}]", requestId);
                        throw new ApiConnectionException("API调用被中断", ie);
                    }
                } else {
                    logger.error("API调用最终失败 [{}] - 已达到最大重试次数: {}", requestId, maxRetries);
                }
            }
        }
        
        // 所有重试都失败了
        throw new ApiConnectionException(
            String.format("API调用失败，已重试 %d 次: %s", maxRetries, 
                         lastException != null ? lastException.getMessage() : "未知错误"), 
            lastException);
    }

    /**
     * 计算退避等待时间（指数退避）
     */
    private long calculateBackoffTime(int attempt) {
        // 指数退避：1秒, 2秒, 4秒...
        long baseDelay = 1000L; // 1秒
        return baseDelay * (1L << (attempt - 1));
    }

    /**
     * 记录性能指标
     */
    private void logPerformanceMetrics(String requestId, String model, long duration, boolean success) {
        if (openRouterProperties.isDebugEnabled()) {
            logger.info("性能指标 [{}] - 模型: {}, 耗时: {}ms, 成功: {}", 
                       requestId, model, duration, success);
        }
    }

    /**
     * 检查异常类型并进行分类处理
     */
    private void handleApiException(Exception e, String requestId) {
        String errorMessage = e.getMessage();
        
        if (errorMessage != null) {
            if (errorMessage.contains("timeout") || errorMessage.contains("连接超时")) {
                logger.error("API连接超时 [{}] - {}", requestId, errorMessage);
                throw new ApiConnectionException("API连接超时: " + errorMessage, e);
            } else if (errorMessage.contains("401") || errorMessage.contains("unauthorized")) {
                logger.error("API认证失败 [{}] - {}", requestId, errorMessage);
                throw new ApiConnectionException("API认证失败，请检查API密钥: " + errorMessage, e);
            } else if (errorMessage.contains("429") || errorMessage.contains("rate limit")) {
                logger.error("API请求频率限制 [{}] - {}", requestId, errorMessage);
                throw new ApiConnectionException("API请求频率超限，请稍后重试: " + errorMessage, e);
            } else if (errorMessage.contains("500") || errorMessage.contains("502") || errorMessage.contains("503")) {
                logger.error("API服务器错误 [{}] - {}", requestId, errorMessage);
                throw new ApiConnectionException("API服务器错误: " + errorMessage, e);
            }
        }
        
        // 默认处理
        logger.error("API调用异常 [{}] - {}", requestId, errorMessage, e);
        throw new ApiConnectionException("API调用失败: " + errorMessage, e);
    }
}
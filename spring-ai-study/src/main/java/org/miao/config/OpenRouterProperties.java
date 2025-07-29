package org.miao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;


import java.util.Arrays;
import java.util.List;

/**
 * OpenRouter配置属性类
 * 用于绑定application.properties中的OpenRouter相关配置
 */
@Component
@ConfigurationProperties(prefix = "openrouter")
@Validated
public class OpenRouterProperties {

    /**
     * OpenRouter API密钥
     */
    private String apiKey;

    /**
     * OpenRouter API基础URL
     */
    private String baseUrl = "https://openrouter.ai/api/v1";

    /**
     * 默认使用的模型
     */
    private String defaultModel = "openai/gpt-3.5-turbo";

    /**
     * 请求超时时间（秒）
     */
    private int timeoutSeconds = 30;

    /**
     * 最大重试次数
     */
    private int maxRetries = 3;

    /**
     * 是否启用调试模式
     */
    private boolean debugEnabled = false;

    /**
     * 应用程序名称（用于OpenRouter的HTTP Referer头）
     */
    private String appName = "Spring AI Study";

    /**
     * 支持的模型列表
     */
    private List<String> availableModels = Arrays.asList(
        "qwen/qwen3-coder:free"
    );

    /**
     * 配置验证
     */
    public void validateConfiguration() {
        if (apiKey != null && apiKey.equals("YOUR_OPENROUTER_API_KEY")) {
            throw new IllegalStateException("请配置有效的OpenRouter API密钥");
        }
        
        if (!availableModels.contains(defaultModel)) {
            throw new IllegalStateException("默认模型 '" + defaultModel + "' 不在支持的模型列表中");
        }
    }

    /**
     * 检查API密钥是否已正确配置
     */
    public boolean isApiKeyConfigured() {
        return apiKey != null && 
               !apiKey.trim().isEmpty() && 
               !apiKey.equals("YOUR_OPENROUTER_API_KEY");
    }

    // Getters and Setters
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getDefaultModel() {
        return defaultModel;
    }

    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public List<String> getAvailableModels() {
        return availableModels;
    }

    public void setAvailableModels(List<String> availableModels) {
        this.availableModels = availableModels;
    }

    @Override
    public String toString() {
        return "OpenRouterProperties{" +
                "baseUrl='" + baseUrl + '\'' +
                ", defaultModel='" + defaultModel + '\'' +
                ", timeoutSeconds=" + timeoutSeconds +
                ", maxRetries=" + maxRetries +
                ", debugEnabled=" + debugEnabled +
                ", appName='" + appName + '\'' +
                ", apiKeyConfigured=" + isApiKeyConfigured() +
                '}';
    }
}

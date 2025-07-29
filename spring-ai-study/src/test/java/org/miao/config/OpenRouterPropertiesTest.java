package org.miao.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OpenRouterProperties单元测试类
 * 测试配置属性的加载、验证和访问
 */
class OpenRouterPropertiesTest {

    private OpenRouterProperties openRouterProperties;

    @BeforeEach
    void setUp() {
        openRouterProperties = new OpenRouterProperties();
    }

    @Test
    void testDefaultValues() {
        // Assert default values
        assertEquals("https://openrouter.ai/api/v1", openRouterProperties.getBaseUrl());
        assertEquals("openai/gpt-3.5-turbo", openRouterProperties.getDefaultModel());
        assertEquals(30, openRouterProperties.getTimeoutSeconds());
        assertEquals(3, openRouterProperties.getMaxRetries());
        assertFalse(openRouterProperties.isDebugEnabled());
        assertEquals("Spring AI Study", openRouterProperties.getAppName());
        
        List<String> expectedModels = Arrays.asList(
            "openai/gpt-3.5-turbo",
            "openai/gpt-4",
            "anthropic/claude-3-haiku",
            "meta-llama/llama-2-70b-chat"
        );
        assertEquals(expectedModels, openRouterProperties.getAvailableModels());
    }

    @Test
    void testSetAndGetApiKey() {
        // Arrange
        String testApiKey = "test-api-key-123";

        // Act
        openRouterProperties.setApiKey(testApiKey);

        // Assert
        assertEquals(testApiKey, openRouterProperties.getApiKey());
    }

    @Test
    void testSetAndGetBaseUrl() {
        // Arrange
        String testBaseUrl = "https://custom.openrouter.ai/api/v1";

        // Act
        openRouterProperties.setBaseUrl(testBaseUrl);

        // Assert
        assertEquals(testBaseUrl, openRouterProperties.getBaseUrl());
    }

    @Test
    void testSetAndGetDefaultModel() {
        // Arrange
        String testModel = "openai/gpt-4";

        // Act
        openRouterProperties.setDefaultModel(testModel);

        // Assert
        assertEquals(testModel, openRouterProperties.getDefaultModel());
    }

    @Test
    void testSetAndGetTimeoutSeconds() {
        // Arrange
        int testTimeout = 60;

        // Act
        openRouterProperties.setTimeoutSeconds(testTimeout);

        // Assert
        assertEquals(testTimeout, openRouterProperties.getTimeoutSeconds());
    }

    @Test
    void testSetAndGetMaxRetries() {
        // Arrange
        int testMaxRetries = 5;

        // Act
        openRouterProperties.setMaxRetries(testMaxRetries);

        // Assert
        assertEquals(testMaxRetries, openRouterProperties.getMaxRetries());
    }

    @Test
    void testSetAndGetDebugEnabled() {
        // Act
        openRouterProperties.setDebugEnabled(true);

        // Assert
        assertTrue(openRouterProperties.isDebugEnabled());
    }

    @Test
    void testSetAndGetAppName() {
        // Arrange
        String testAppName = "Custom Spring AI App";

        // Act
        openRouterProperties.setAppName(testAppName);

        // Assert
        assertEquals(testAppName, openRouterProperties.getAppName());
    }

    @Test
    void testSetAndGetAvailableModels() {
        // Arrange
        List<String> testModels = Arrays.asList(
            "openai/gpt-3.5-turbo",
            "openai/gpt-4",
            "custom/model"
        );

        // Act
        openRouterProperties.setAvailableModels(testModels);

        // Assert
        assertEquals(testModels, openRouterProperties.getAvailableModels());
    }

    @Test
    void testIsApiKeyConfigured_WithValidKey() {
        // Arrange
        openRouterProperties.setApiKey("valid-api-key");

        // Act & Assert
        assertTrue(openRouterProperties.isApiKeyConfigured());
    }

    @Test
    void testIsApiKeyConfigured_WithNullKey() {
        // Arrange
        openRouterProperties.setApiKey(null);

        // Act & Assert
        assertFalse(openRouterProperties.isApiKeyConfigured());
    }

    @Test
    void testIsApiKeyConfigured_WithEmptyKey() {
        // Arrange
        openRouterProperties.setApiKey("");

        // Act & Assert
        assertFalse(openRouterProperties.isApiKeyConfigured());
    }

    @Test
    void testIsApiKeyConfigured_WithBlankKey() {
        // Arrange
        openRouterProperties.setApiKey("   ");

        // Act & Assert
        assertFalse(openRouterProperties.isApiKeyConfigured());
    }

    @Test
    void testIsApiKeyConfigured_WithPlaceholderKey() {
        // Arrange
        openRouterProperties.setApiKey("YOUR_OPENROUTER_API_KEY");

        // Act & Assert
        assertFalse(openRouterProperties.isApiKeyConfigured());
    }

    @Test
    void testValidateConfiguration_WithValidConfiguration() {
        // Arrange
        openRouterProperties.setApiKey("valid-api-key");
        openRouterProperties.setDefaultModel("openai/gpt-3.5-turbo");
        openRouterProperties.setAvailableModels(Arrays.asList(
            "openai/gpt-3.5-turbo",
            "openai/gpt-4"
        ));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> openRouterProperties.validateConfiguration());
    }

    @Test
    void testValidateConfiguration_WithPlaceholderApiKey() {
        // Arrange
        openRouterProperties.setApiKey("YOUR_OPENROUTER_API_KEY");

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> openRouterProperties.validateConfiguration()
        );
        assertEquals("请配置有效的OpenRouter API密钥", exception.getMessage());
    }

    @Test
    void testValidateConfiguration_WithInvalidDefaultModel() {
        // Arrange
        openRouterProperties.setApiKey("valid-api-key");
        openRouterProperties.setDefaultModel("invalid/model");
        openRouterProperties.setAvailableModels(Arrays.asList(
            "openai/gpt-3.5-turbo",
            "openai/gpt-4"
        ));

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> openRouterProperties.validateConfiguration()
        );
        assertTrue(exception.getMessage().contains("默认模型 'invalid/model' 不在支持的模型列表中"));
    }

    @Test
    void testToString_DoesNotExposeApiKey() {
        // Arrange
        openRouterProperties.setApiKey("secret-api-key");

        // Act
        String result = openRouterProperties.toString();

        // Assert
        assertNotNull(result);
        assertFalse(result.contains("secret-api-key"));
        assertTrue(result.contains("apiKeyConfigured=true"));
    }

    @Test
    void testToString_WithUnconfiguredApiKey() {
        // Arrange
        openRouterProperties.setApiKey(null);

        // Act
        String result = openRouterProperties.toString();

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("apiKeyConfigured=false"));
    }

    @Test
    void testToString_ContainsExpectedFields() {
        // Act
        String result = openRouterProperties.toString();

        // Assert
        assertTrue(result.contains("baseUrl"));
        assertTrue(result.contains("defaultModel"));
        assertTrue(result.contains("timeoutSeconds"));
        assertTrue(result.contains("maxRetries"));
        assertTrue(result.contains("debugEnabled"));
        assertTrue(result.contains("appName"));
        assertTrue(result.contains("apiKeyConfigured"));
    }
}
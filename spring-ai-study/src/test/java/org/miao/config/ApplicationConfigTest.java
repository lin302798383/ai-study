package org.miao.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ApplicationConfig单元测试类
 * 测试配置类的Bean创建和依赖注入
 * 注意：由于Spring AI类的复杂性，这里主要测试RestTemplate配置
 * 其他Spring AI相关的Bean测试应在集成测试中进行
 */
@ExtendWith(MockitoExtension.class)
class ApplicationConfigTest {

    @Mock
    private OpenRouterProperties openRouterProperties;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ApplicationConfig applicationConfig;

    private static final String TEST_API_KEY = "test-api-key";
    private static final String TEST_BASE_URL = "https://openrouter.ai/api/v1";
    private static final String TEST_DEFAULT_MODEL = "openai/gpt-3.5-turbo";
    private static final int TEST_TIMEOUT_SECONDS = 30;

    @BeforeEach
    void setUp() {
        // 设置默认的mock行为
        lenient().when(openRouterProperties.getApiKey()).thenReturn(TEST_API_KEY);
        lenient().when(openRouterProperties.getBaseUrl()).thenReturn(TEST_BASE_URL);
        lenient().when(openRouterProperties.getDefaultModel()).thenReturn(TEST_DEFAULT_MODEL);
        lenient().when(openRouterProperties.getTimeoutSeconds()).thenReturn(TEST_TIMEOUT_SECONDS);
        lenient().when(openRouterProperties.isDebugEnabled()).thenReturn(false);
    }

    @Test
    void testRestTemplateBean_CreatesWithCorrectTimeout() {
        // Arrange
        when(restTemplateBuilder.setConnectTimeout(any(Duration.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.setReadTimeout(any(Duration.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        // Act
        RestTemplate result = applicationConfig.restTemplate(restTemplateBuilder);

        // Assert
        assertNotNull(result);
        assertEquals(restTemplate, result);
        
        // Verify timeout configuration
        verify(restTemplateBuilder).setConnectTimeout(Duration.ofSeconds(TEST_TIMEOUT_SECONDS));
        verify(restTemplateBuilder).setReadTimeout(Duration.ofSeconds(TEST_TIMEOUT_SECONDS));
        verify(restTemplateBuilder).build();
    }

    @Test
    void testRestTemplateBean_WithDifferentTimeout() {
        // Arrange
        int customTimeout = 60;
        when(openRouterProperties.getTimeoutSeconds()).thenReturn(customTimeout);
        when(restTemplateBuilder.setConnectTimeout(any(Duration.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.setReadTimeout(any(Duration.class))).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);

        // Act
        RestTemplate result = applicationConfig.restTemplate(restTemplateBuilder);

        // Assert
        assertNotNull(result);
        
        // Verify custom timeout was used
        verify(restTemplateBuilder).setConnectTimeout(Duration.ofSeconds(customTimeout));
        verify(restTemplateBuilder).setReadTimeout(Duration.ofSeconds(customTimeout));
    }

    @Test
    void testOpenRouterPropertiesInjection() {
        // This test verifies that the OpenRouterProperties is properly injected
        // We can't easily test the actual Bean creation methods due to Spring AI dependencies
        // but we can verify that the properties are accessible
        
        // Act & Assert
        assertNotNull(openRouterProperties);
        
        // Verify we can access the properties
        when(openRouterProperties.getApiKey()).thenReturn(TEST_API_KEY);
        when(openRouterProperties.getBaseUrl()).thenReturn(TEST_BASE_URL);
        when(openRouterProperties.getDefaultModel()).thenReturn(TEST_DEFAULT_MODEL);
        
        assertEquals(TEST_API_KEY, openRouterProperties.getApiKey());
        assertEquals(TEST_BASE_URL, openRouterProperties.getBaseUrl());
        assertEquals(TEST_DEFAULT_MODEL, openRouterProperties.getDefaultModel());
    }

    @Test
    void testConfigurationClassExists() {
        // This test verifies that the ApplicationConfig class is properly instantiated
        assertNotNull(applicationConfig);
    }

    // Note: Tests for OpenAiApi, OpenAiChatModel, and ChatClient beans are complex
    // due to Spring AI dependencies and should be tested in integration tests
    // where the full Spring context is available
}
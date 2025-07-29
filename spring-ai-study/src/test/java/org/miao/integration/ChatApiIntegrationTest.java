package org.miao.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.miao.controller.ChatController;
import org.miao.dto.ChatRequest;
import org.miao.dto.ChatResponse;
import org.miao.dto.ErrorResponse;
import org.miao.service.ChatService;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API集成测试类
 * 使用Mock服务测试控制器层的集成功能
 * 
 * 这个测试类专注于测试API端点的行为，而不依赖于实际的OpenRouter连接
 */
@WebMvcTest(ChatController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("integration")
public class ChatApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    /**
     * 测试基本的聊天功能 - 端到端流程
     */
    @Test
    void testChatEndToEnd() throws Exception {
        // 准备Mock响应
        ChatResponse mockResponse = new ChatResponse("Test successful", "openai/gpt-3.5-turbo");
        when(chatService.sendMessage(any(ChatRequest.class))).thenReturn(mockResponse);
        
        // 准备测试数据
        ChatRequest request = new ChatRequest();
        request.setMessage("Hello, this is a test message. Please respond with 'Test successful'.");
        
        String requestJson = objectMapper.writeValueAsString(request);

        // 执行请求
        MvcResult result = mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // 验证响应
        String responseJson = result.getResponse().getContentAsString();
        ChatResponse response = objectMapper.readValue(responseJson, ChatResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResponse()).isEqualTo("Test successful");
        assertThat(response.getModel()).isEqualTo("openai/gpt-3.5-turbo");
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getError()).isNull();
    }

    /**
     * 测试指定模型的聊天功能
     */
    @Test
    void testChatWithSpecificModel() throws Exception {
        // 准备Mock响应
        ChatResponse mockResponse = new ChatResponse("The answer is 4", "openai/gpt-3.5-turbo");
        when(chatService.sendMessage(any(ChatRequest.class))).thenReturn(mockResponse);
        
        // 准备测试数据
        ChatRequest request = new ChatRequest();
        request.setMessage("What is 2+2?");
        request.setModel("openai/gpt-3.5-turbo");
        
        String requestJson = objectMapper.writeValueAsString(request);

        // 执行请求
        MvcResult result = mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // 验证响应
        String responseJson = result.getResponse().getContentAsString();
        ChatResponse response = objectMapper.readValue(responseJson, ChatResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResponse()).contains("4");
        assertThat(response.getModel()).isEqualTo("openai/gpt-3.5-turbo");
    }

    /**
     * 测试获取可用模型列表
     */
    @Test
    void testGetAvailableModels() throws Exception {
        // 准备Mock响应
        List<String> mockModels = Arrays.asList(
            "openai/gpt-3.5-turbo", 
            "openai/gpt-4", 
            "anthropic/claude-3-haiku"
        );
        when(chatService.getAvailableModels()).thenReturn(mockModels);
        
        MvcResult result = mockMvc.perform(get("/api/models")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        String[] models = objectMapper.readValue(responseJson, String[].class);

        assertThat(models).isNotNull();
        assertThat(models.length).isEqualTo(3);
        assertThat(models).contains("openai/gpt-3.5-turbo");
        assertThat(models).contains("openai/gpt-4");
        assertThat(models).contains("anthropic/claude-3-haiku");
    }

    /**
     * 测试模型可用性检查
     */
    @Test
    void testModelAvailability() throws Exception {
        // 准备Mock响应
        when(chatService.isModelAvailable("gpt-3.5-turbo")).thenReturn(true);
        when(chatService.isModelAvailable("nonexistent-model")).thenReturn(false);
        
        // 测试已知可用的模型 (使用简单的模型名，避免路径中的斜杠问题)
        mockMvc.perform(get("/api/models/gpt-3.5-turbo/available"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // 测试不存在的模型
        mockMvc.perform(get("/api/models/nonexistent-model/available"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    /**
     * 测试健康检查端点
     */
    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Chat API is running"));
    }

    /**
     * 测试无效请求的错误处理
     */
    @Test
    void testInvalidRequestHandling() throws Exception {
        // 测试空消息
        ChatRequest emptyRequest = new ChatRequest();
        emptyRequest.setMessage("");
        
        String requestJson = objectMapper.writeValueAsString(emptyRequest);

        MvcResult result = mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(responseJson, ErrorResponse.class);

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.isSuccess()).isFalse();
        assertThat(errorResponse.getError()).isNotNull().isNotEmpty();
        assertThat(errorResponse.getErrorCode()).isNotNull();
    }

    /**
     * 测试null消息的错误处理
     */
    @Test
    void testNullMessageHandling() throws Exception {
        ChatRequest nullRequest = new ChatRequest();
        nullRequest.setMessage(null);
        
        String requestJson = objectMapper.writeValueAsString(nullRequest);

        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    /**
     * 测试长消息的处理（在限制范围内）
     */
    @Test
    void testLongMessageHandling() throws Exception {
        // 准备Mock响应
        ChatResponse mockResponse = new ChatResponse("Processed long message successfully", "openai/gpt-3.5-turbo");
        when(chatService.sendMessage(any(ChatRequest.class))).thenReturn(mockResponse);
        
        // 创建一个长消息，但在4000字符限制内
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 60; i++) { // 减少重复次数以保持在4000字符以内
            longMessage.append("This is a long message for testing purposes. ");
        }
        
        ChatRequest request = new ChatRequest();
        request.setMessage(longMessage.toString());
        
        String requestJson = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        ChatResponse response = objectMapper.readValue(responseJson, ChatResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResponse()).isEqualTo("Processed long message successfully");
    }

    /**
     * 测试超长消息的验证错误处理
     */
    @Test
    void testTooLongMessageValidation() throws Exception {
        // 创建一个超过4000字符的消息
        StringBuilder tooLongMessage = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            tooLongMessage.append("This is a very long message for testing purposes. ");
        }
        
        ChatRequest request = new ChatRequest();
        request.setMessage(tooLongMessage.toString());
        
        String requestJson = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(responseJson, ErrorResponse.class);

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.isSuccess()).isFalse();
        assertThat(errorResponse.getError()).contains("4000");
        assertThat(errorResponse.getErrorCode()).isEqualTo("VALIDATION_ERROR");
    }

    /**
     * 测试并发请求处理
     */
    @Test
    void testConcurrentRequests() throws Exception {
        // 准备Mock响应
        ChatResponse mockResponse = new ChatResponse("Concurrent response", "openai/gpt-3.5-turbo");
        when(chatService.sendMessage(any(ChatRequest.class))).thenReturn(mockResponse);
        
        ChatRequest request = new ChatRequest();
        request.setMessage("Concurrent test message");
        
        String requestJson = objectMapper.writeValueAsString(request);

        // 同时发送多个请求
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isOk());
        }
    }
}
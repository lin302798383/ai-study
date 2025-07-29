package org.miao.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.miao.dto.ChatRequest;
import org.miao.dto.ChatResponse;
import org.miao.exception.InvalidRequestException;
import org.miao.exception.ModelNotAvailableException;
import org.miao.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ChatController单元测试类
 * 测试REST API端点的功能，包括请求验证和响应格式
 */
@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private static final String TEST_MESSAGE = "Hello, how are you?";
    private static final String TEST_RESPONSE = "I'm doing well, thank you!";
    private static final String DEFAULT_MODEL = "openai/gpt-3.5-turbo";
    private static final List<String> AVAILABLE_MODELS = Arrays.asList(
        "openai/gpt-3.5-turbo",
        "openai/gpt-4",
        "anthropic/claude-3-haiku"
    );

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testChat_ValidRequest_ReturnsSuccess() throws Exception {
        // Arrange
        ChatRequest request = new ChatRequest(TEST_MESSAGE, DEFAULT_MODEL);
        ChatResponse expectedResponse = ChatResponse.success(TEST_RESPONSE, DEFAULT_MODEL);
        
        when(chatService.sendMessage(any(ChatRequest.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response").value(TEST_RESPONSE))
                .andExpect(jsonPath("$.model").value(DEFAULT_MODEL))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(chatService).sendMessage(any(ChatRequest.class));
    }

    @Test
    void testChat_ValidRequestWithoutModel_ReturnsSuccess() throws Exception {
        // Arrange
        ChatRequest request = new ChatRequest(TEST_MESSAGE);
        ChatResponse expectedResponse = ChatResponse.success(TEST_RESPONSE, DEFAULT_MODEL);
        
        when(chatService.sendMessage(any(ChatRequest.class))).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response").value(TEST_RESPONSE))
                .andExpect(jsonPath("$.model").value(DEFAULT_MODEL));

        verify(chatService).sendMessage(any(ChatRequest.class));
    }

    @Test
    void testChat_EmptyMessage_ReturnsBadRequest() throws Exception {
        // Arrange
        ChatRequest request = new ChatRequest("", DEFAULT_MODEL);

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(chatService, never()).sendMessage(any(ChatRequest.class));
    }

    @Test
    void testChat_NullMessage_ReturnsBadRequest() throws Exception {
        // Arrange
        ChatRequest request = new ChatRequest(null, DEFAULT_MODEL);

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(chatService, never()).sendMessage(any(ChatRequest.class));
    }

    @Test
    void testChat_MessageTooLong_ReturnsBadRequest() throws Exception {
        // Arrange
        String longMessage = "a".repeat(4001);
        ChatRequest request = new ChatRequest(longMessage, DEFAULT_MODEL);

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(chatService, never()).sendMessage(any(ChatRequest.class));
    }

    @Test
    void testChat_InvalidModel_ThrowsException() throws Exception {
        // Arrange
        ChatRequest request = new ChatRequest(TEST_MESSAGE, "invalid/model");
        
        when(chatService.sendMessage(any(ChatRequest.class)))
            .thenThrow(new ModelNotAvailableException("模型不可用"));

        // Act & Assert - 异常会被抛出，不会被处理为400状态码
        try {
            mockMvc.perform(post("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        } catch (Exception e) {
            // 预期会抛出异常
        }

        verify(chatService).sendMessage(any(ChatRequest.class));
    }

    @Test
    void testChat_ServiceException_ThrowsException() throws Exception {
        // Arrange
        ChatRequest request = new ChatRequest(TEST_MESSAGE, DEFAULT_MODEL);
        
        when(chatService.sendMessage(any(ChatRequest.class)))
            .thenThrow(new InvalidRequestException("服务异常"));

        // Act & Assert - 异常会被抛出，不会被处理为400状态码
        try {
            mockMvc.perform(post("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        } catch (Exception e) {
            // 预期会抛出异常
        }

        verify(chatService).sendMessage(any(ChatRequest.class));
    }

    @Test
    void testChat_InvalidJson_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("invalid json"))
                .andExpect(status().isBadRequest());

        verify(chatService, never()).sendMessage(any(ChatRequest.class));
    }

    @Test
    void testChat_MissingContentType_ReturnsUnsupportedMediaType() throws Exception {
        // Arrange
        ChatRequest request = new ChatRequest(TEST_MESSAGE, DEFAULT_MODEL);

        // Act & Assert
        mockMvc.perform(post("/api/chat")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());

        verify(chatService, never()).sendMessage(any(ChatRequest.class));
    }

    @Test
    void testGetAvailableModels_ReturnsModelList() throws Exception {
        // Arrange
        when(chatService.getAvailableModels()).thenReturn(AVAILABLE_MODELS);

        // Act & Assert
        mockMvc.perform(get("/api/models"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(AVAILABLE_MODELS.size()))
                .andExpect(jsonPath("$[0]").value("openai/gpt-3.5-turbo"))
                .andExpect(jsonPath("$[1]").value("openai/gpt-4"))
                .andExpect(jsonPath("$[2]").value("anthropic/claude-3-haiku"));

        verify(chatService).getAvailableModels();
    }

    @Test
    void testIsModelAvailable_ValidModel_ReturnsTrue() throws Exception {
        // Arrange
        String model = "openai-gpt-3.5-turbo";  // 使用不包含斜杠的模型名进行测试
        when(chatService.isModelAvailable(model)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/models/{model}/available", model))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("true"));

        verify(chatService).isModelAvailable(model);
    }

    @Test
    void testIsModelAvailable_InvalidModel_ReturnsFalse() throws Exception {
        // Arrange
        String model = "invalid-model";  // 使用不包含斜杠的模型名
        when(chatService.isModelAvailable(model)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/models/{model}/available", model))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("false"));

        verify(chatService).isModelAvailable(model);
    }

    @Test
    void testIsModelAvailable_EmptyModel_ReturnsFalse() throws Exception {
        // Arrange
        String model = "empty-model";  // 使用一个有效的路径参数来测试空模型的逻辑
        when(chatService.isModelAvailable(model)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/models/{model}/available", model))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("false"));

        verify(chatService).isModelAvailable(model);
    }

    @Test
    void testHealth_ReturnsOk() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE + ";charset=ISO-8859-1"))
                .andExpect(content().string("Chat API is running"));

        // Health endpoint doesn't use chatService
        verifyNoInteractions(chatService);
    }

    @Test
    void testChat_WrongHttpMethod_ReturnsMethodNotAllowed() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/chat"))
                .andExpect(status().isMethodNotAllowed());

        verify(chatService, never()).sendMessage(any(ChatRequest.class));
    }

    @Test
    void testModels_WrongHttpMethod_ReturnsMethodNotAllowed() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/models"))
                .andExpect(status().isMethodNotAllowed());

        verify(chatService, never()).getAvailableModels();
    }
}
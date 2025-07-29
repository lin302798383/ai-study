package org.miao.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.miao.dto.ChatRequest;
import org.miao.dto.ChatResponse;
import org.miao.dto.ErrorResponse;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 集成测试工具类
 * 提供常用的测试辅助方法
 */
public class IntegrationTestUtils {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public IntegrationTestUtils(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    /**
     * 发送聊天请求并返回响应
     * 
     * @param message 消息内容
     * @return 聊天响应
     * @throws Exception 如果请求失败
     */
    public ChatResponse sendChatMessage(String message) throws Exception {
        return sendChatMessage(message, null);
    }

    /**
     * 发送聊天请求并返回响应
     * 
     * @param message 消息内容
     * @param model 指定的模型
     * @return 聊天响应
     * @throws Exception 如果请求失败
     */
    public ChatResponse sendChatMessage(String message, String model) throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage(message);
        if (model != null) {
            request.setModel(model);
        }

        String requestJson = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseJson, ChatResponse.class);
    }

    /**
     * 发送无效的聊天请求并返回错误响应
     * 
     * @param message 消息内容
     * @return 错误响应
     * @throws Exception 如果请求失败
     */
    public ErrorResponse sendInvalidChatMessage(String message) throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage(message);

        String requestJson = objectMapper.writeValueAsString(request);

        MvcResult result = mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        return objectMapper.readValue(responseJson, ErrorResponse.class);
    }

    /**
     * 创建测试用的聊天请求
     * 
     * @param message 消息内容
     * @param model 模型名称（可选）
     * @return 聊天请求对象
     */
    public static ChatRequest createChatRequest(String message, String model) {
        ChatRequest request = new ChatRequest();
        request.setMessage(message);
        if (model != null) {
            request.setModel(model);
        }
        return request;
    }

    /**
     * 验证聊天响应的基本结构
     * 
     * @param response 聊天响应
     * @return 是否有效
     */
    public static boolean isValidChatResponse(ChatResponse response) {
        return response != null &&
               response.isSuccess() &&
               response.getResponse() != null &&
               !response.getResponse().isEmpty() &&
               response.getModel() != null &&
               !response.getModel().isEmpty() &&
               response.getTimestamp() != null;
    }

    /**
     * 验证错误响应的基本结构
     * 
     * @param errorResponse 错误响应
     * @return 是否有效
     */
    public static boolean isValidErrorResponse(ErrorResponse errorResponse) {
        return errorResponse != null &&
               !errorResponse.isSuccess() &&
               errorResponse.getError() != null &&
               !errorResponse.getError().isEmpty() &&
               errorResponse.getErrorCode() != null &&
               !errorResponse.getErrorCode().isEmpty();
    }

    /**
     * 生成测试用的长消息
     * 
     * @param length 重复次数
     * @return 长消息字符串
     */
    public static String generateLongMessage(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append("This is a test message for integration testing purposes. ");
        }
        return sb.toString();
    }

    /**
     * 检查环境变量是否设置
     * 
     * @param variableName 环境变量名
     * @return 是否设置且不为空
     */
    public static boolean isEnvironmentVariableSet(String variableName) {
        String value = System.getenv(variableName);
        return value != null && !value.trim().isEmpty() && !value.equals("test-api-key");
    }
}
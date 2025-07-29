package org.miao.dto;

import java.time.LocalDateTime;

/**
 * 聊天响应数据传输对象
 * 封装AI模型的响应结果和相关元数据
 */
public class ChatResponse {
    
    private String response;
    private String model;
    private LocalDateTime timestamp;
    private boolean success;
    private String error;
    
    public ChatResponse() {
        this.timestamp = LocalDateTime.now();
        this.success = true;
    }
    
    public ChatResponse(String response, String model) {
        this();
        this.response = response;
        this.model = model;
    }
    
    public static ChatResponse success(String response, String model) {
        return new ChatResponse(response, model);
    }
    
    public static ChatResponse error(String error) {
        ChatResponse chatResponse = new ChatResponse();
        chatResponse.success = false;
        chatResponse.error = error;
        return chatResponse;
    }
    
    public String getResponse() {
        return response;
    }
    
    public void setResponse(String response) {
        this.response = response;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    @Override
    public String toString() {
        return "ChatResponse{" +
                "response='" + response + '\'' +
                ", model='" + model + '\'' +
                ", timestamp=" + timestamp +
                ", success=" + success +
                ", error='" + error + '\'' +
                '}';
    }
}
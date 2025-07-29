package org.miao.dto;



/**
 * 聊天请求数据传输对象
 * 封装用户发送的聊天消息和可选的模型选择
 */
public class ChatRequest {
    

    private String message;
    

    private String model;
    
    public ChatRequest() {}
    
    public ChatRequest(String message) {
        this.message = message;
    }
    
    public ChatRequest(String message, String model) {
        this.message = message;
        this.model = model;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    @Override
    public String toString() {
        return "ChatRequest{" +
                "message='" + message + '\'' +
                ", model='" + model + '\'' +
                '}';
    }
}

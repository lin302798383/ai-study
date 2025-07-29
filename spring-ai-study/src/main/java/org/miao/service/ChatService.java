package org.miao.service;

import org.miao.dto.ChatRequest;
import org.miao.dto.ChatResponse;
import java.util.List;

/**
 * 聊天服务接口
 * 定义与AI模型交互的核心功能
 */
public interface ChatService {
    
    /**
     * 发送聊天消息并获取响应
     * 使用默认模型
     * 
     * @param message 用户消息
     * @return AI响应
     */
    ChatResponse sendMessage(String message);
    
    /**
     * 发送聊天消息并获取响应
     * 指定使用的模型
     * 
     * @param message 用户消息
     * @param model 指定的AI模型
     * @return AI响应
     */
    ChatResponse sendMessage(String message, String model);
    
    /**
     * 使用ChatRequest对象发送消息
     * 
     * @param request 聊天请求对象
     * @return AI响应
     */
    ChatResponse sendMessage(ChatRequest request);
    
    /**
     * 获取可用的AI模型列表
     * 
     * @return 可用模型名称列表
     */
    List<String> getAvailableModels();
    
    /**
     * 检查指定模型是否可用
     * 
     * @param model 模型名称
     * @return 是否可用
     */
    boolean isModelAvailable(String model);
}
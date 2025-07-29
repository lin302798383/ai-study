package org.miao.controller;

import org.miao.dto.ChatRequest;
import org.miao.dto.ChatResponse;
import org.miao.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天REST API控制器
 * 提供聊天消息处理和模型查询的REST端点
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
        logger.info("ChatController初始化完成");
    }

    /**
     * 处理聊天消息的POST端点
     * 
     * @param request 聊天请求对象
     * @return 聊天响应
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        logger.info("收到聊天请求 - 消息长度: {}, 指定模型: {}", 
                   request.getMessage() != null ? request.getMessage().length() : 0,
                   request.getModel());
        
        ChatResponse response = chatService.sendMessage(request);
        logger.info("聊天请求处理成功 - 响应长度: {}, 使用模型: {}", 
                   response.getResponse() != null ? response.getResponse().length() : 0,
                   response.getModel());
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取可用模型列表的GET端点
     * 
     * @return 可用模型列表
     */
    @GetMapping("/models")
    public ResponseEntity<List<String>> getAvailableModels() {
        logger.debug("收到获取模型列表请求");
        
        List<String> models = chatService.getAvailableModels();
        logger.debug("返回模型列表，数量: {}", models.size());
        
        return ResponseEntity.ok(models);
    }

    /**
     * 检查特定模型是否可用的GET端点
     * 
     * @param model 要检查的模型名称
     * @return 模型可用性状态
     */
    @GetMapping("/models/{model}/available")
    public ResponseEntity<Boolean> isModelAvailable(@PathVariable String model) {
        logger.debug("检查模型可用性: {}", model);
        
        boolean available = chatService.isModelAvailable(model);
        logger.debug("模型 {} 可用性: {}", model, available);
        
        return ResponseEntity.ok(available);
    }

    /**
     * 健康检查端点
     * 
     * @return 服务状态
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.debug("收到健康检查请求");
        return ResponseEntity.ok("Chat API is running");
    }
}

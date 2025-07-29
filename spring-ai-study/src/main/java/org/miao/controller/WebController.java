package org.miao.controller;

import org.miao.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Web界面控制器
 * 处理Web页面路由和模板渲染
 */
@Controller
public class WebController {

    private final ChatService chatService;

    @Autowired
    public WebController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * 根路径处理器 - 显示聊天界面
     * 
     * @param model 模板模型
     * @return 聊天页面模板名称
     */
    @GetMapping("/")
    public String chatPage(Model model) {
        try {
            // 获取可用的AI模型列表
            List<String> availableModels = chatService.getAvailableModels();
            model.addAttribute("availableModels", availableModels);
        } catch (Exception e) {
            // 如果获取模型列表失败，提供默认模型
            model.addAttribute("availableModels", List.of("openai/gpt-3.5-turbo"));
        }
        
        return "chat";
    }

    /**
     * 聊天页面的直接访问路径
     * 
     * @param model 模板模型
     * @return 聊天页面模板名称
     */
    @GetMapping("/chat")
    public String chat(Model model) {
        return chatPage(model);
    }
}
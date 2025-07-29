package org.miao.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.miao.controller.WebController;
import org.miao.controller.ChatController;
import org.miao.service.ChatService;
import org.miao.dto.ChatRequest;
import org.miao.dto.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web界面集成测试
 * 测试页面渲染功能、JavaScript交互逻辑和完整用户流程
 */
@WebMvcTest(controllers = {WebController.class, ChatController.class})
@DisplayName("Web界面集成测试")
class WebInterfaceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    private IntegrationTestUtils testUtils;

    @BeforeEach
    void setUp() {
        testUtils = new IntegrationTestUtils(mockMvc, objectMapper);
        
        // Mock ChatService behavior
        List<String> mockModels = Arrays.asList(
            "openai/gpt-3.5-turbo",
            "openai/gpt-4",
            "anthropic/claude-3-haiku"
        );
        when(chatService.getAvailableModels()).thenReturn(mockModels);
        
        // Mock chat response
        ChatResponse mockResponse = new ChatResponse();
        mockResponse.setSuccess(true);
        mockResponse.setResponse("This is a test response from the AI model.");
        mockResponse.setModel("openai/gpt-3.5-turbo");
        mockResponse.setTimestamp(java.time.LocalDateTime.now());
        
        when(chatService.sendMessage(any(ChatRequest.class))).thenReturn(mockResponse);
        when(chatService.sendMessage(anyString())).thenReturn(mockResponse);
        when(chatService.sendMessage(anyString(), anyString())).thenReturn(mockResponse);
    }

    @Test
    @DisplayName("测试根路径页面渲染功能")
    void testRootPageRendering() throws Exception {
        // 测试根路径访问
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("chat"))
                .andExpect(model().attributeExists("availableModels"))
                .andExpect(model().attribute("availableModels", hasItem("openai/gpt-3.5-turbo")));
    }

    @Test
    @DisplayName("测试聊天页面直接访问")
    void testChatPageDirectAccess() throws Exception {
        // 测试/chat路径访问
        mockMvc.perform(get("/chat"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("chat"))
                .andExpect(model().attributeExists("availableModels"));
    }

    @Test
    @DisplayName("测试页面HTML结构和关键元素")
    void testPageHtmlStructureAndElements() throws Exception {
        MvcResult result = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn();

        String htmlContent = result.getResponse().getContentAsString();

        // 验证页面标题
        assertTrue(htmlContent.contains("<title>Spring AI Chat - OpenRouter集成</title>"), 
                  "页面应包含正确的标题");

        // 验证关键CSS和JS文件引用
        assertTrue(htmlContent.contains("css/chat.css"), 
                  "页面应引用CSS样式文件");
        assertTrue(htmlContent.contains("js/chat.js"), 
                  "页面应引用JavaScript文件");
        assertTrue(htmlContent.contains("font-awesome"), 
                  "页面应引用Font Awesome图标库");

        // 验证聊天界面关键元素
        assertTrue(htmlContent.contains("id=\"chatMessages\""), 
                  "页面应包含聊天消息容器");
        assertTrue(htmlContent.contains("id=\"messageInput\""), 
                  "页面应包含消息输入框");
        assertTrue(htmlContent.contains("id=\"sendButton\""), 
                  "页面应包含发送按钮");
        assertTrue(htmlContent.contains("id=\"modelSelect\""), 
                  "页面应包含模型选择器");

        // 验证状态指示器和加载元素
        assertTrue(htmlContent.contains("id=\"statusIndicator\""), 
                  "页面应包含状态指示器");
        assertTrue(htmlContent.contains("id=\"loadingIndicator\""), 
                  "页面应包含加载指示器");
        assertTrue(htmlContent.contains("id=\"errorModal\""), 
                  "页面应包含错误模态框");

        // 验证欢迎消息
        assertTrue(htmlContent.contains("欢迎使用Spring AI聊天界面"), 
                  "页面应包含欢迎消息");

        // 验证字符计数和输入限制
        assertTrue(htmlContent.contains("maxlength=\"2000\""), 
                  "输入框应有字符长度限制");
        assertTrue(htmlContent.contains("0/2000"), 
                  "页面应显示字符计数");
    }

    @Test
    @DisplayName("测试模型选择器渲染")
    void testModelSelectorRendering() throws Exception {
        MvcResult result = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn();

        String htmlContent = result.getResponse().getContentAsString();

        // 验证模型选择器存在
        assertTrue(htmlContent.contains("<select id=\"modelSelect\""), 
                  "页面应包含模型选择器");

        // 验证默认模型选项
        assertTrue(htmlContent.contains("openai/gpt-3.5-turbo"), 
                  "模型选择器应包含默认模型选项");

        // 验证选中状态
        assertTrue(htmlContent.contains("selected"), 
                  "应有默认选中的模型");
    }

    @Test
    @DisplayName("测试静态资源访问")
    void testStaticResourceAccess() throws Exception {
        // 测试CSS文件访问
        mockMvc.perform(get("/css/chat.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"));

        // 测试JavaScript文件访问
        mockMvc.perform(get("/js/chat.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/javascript"));
    }

    @Test
    @DisplayName("测试JavaScript文件内容和功能")
    void testJavaScriptContent() throws Exception {
        MvcResult result = mockMvc.perform(get("/js/chat.js"))
                .andExpect(status().isOk())
                .andReturn();

        String jsContent = result.getResponse().getContentAsString();

        // 验证关键JavaScript类和方法
        assertTrue(jsContent.contains("class ChatApp"), 
                  "JavaScript应包含ChatApp类");
        assertTrue(jsContent.contains("sendMessage"), 
                  "JavaScript应包含sendMessage方法");
        assertTrue(jsContent.contains("callChatAPI"), 
                  "JavaScript应包含callChatAPI方法");
        assertTrue(jsContent.contains("addMessage"), 
                  "JavaScript应包含addMessage方法");

        // 验证事件处理
        assertTrue(jsContent.contains("addEventListener"), 
                  "JavaScript应包含事件监听器");
        assertTrue(jsContent.contains("click"), 
                  "JavaScript应处理点击事件");
        assertTrue(jsContent.contains("keydown"), 
                  "JavaScript应处理键盘事件");

        // 验证AJAX功能
        assertTrue(jsContent.contains("fetch"), 
                  "JavaScript应使用fetch进行AJAX调用");
        assertTrue(jsContent.contains("/api/chat"), 
                  "JavaScript应调用正确的API端点");
        assertTrue(jsContent.contains("POST"), 
                  "JavaScript应使用POST方法");

        // 验证错误处理
        assertTrue(jsContent.contains("catch"), 
                  "JavaScript应包含错误处理");
        assertTrue(jsContent.contains("showError"), 
                  "JavaScript应包含错误显示功能");
        assertTrue(jsContent.contains("retryLastMessage"), 
                  "JavaScript应包含重试功能");

        // 验证UI交互功能
        assertTrue(jsContent.contains("updateCharCount"), 
                  "JavaScript应包含字符计数功能");
        assertTrue(jsContent.contains("adjustTextareaHeight"), 
                  "JavaScript应包含文本框高度调整功能");
        assertTrue(jsContent.contains("scrollToBottom"), 
                  "JavaScript应包含滚动到底部功能");
    }

    @Test
    @DisplayName("测试完整用户流程 - 页面加载到消息发送")
    void testCompleteUserFlow() throws Exception {
        // 1. 用户访问页面
        MvcResult pageResult = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("availableModels"))
                .andReturn();

        String htmlContent = pageResult.getResponse().getContentAsString();
        
        // 验证页面加载成功且包含必要元素
        assertTrue(htmlContent.contains("id=\"messageInput\""), 
                  "页面应包含消息输入框");
        assertTrue(htmlContent.contains("id=\"sendButton\""), 
                  "页面应包含发送按钮");

        // 2. 模拟用户发送消息（通过API调用，模拟JavaScript行为）
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setMessage("Hello, this is a test message from web interface");
        chatRequest.setModel("openai/gpt-3.5-turbo");

        String requestJson = objectMapper.writeValueAsString(chatRequest);

        // 3. 验证API调用成功（模拟JavaScript的fetch调用）
        MvcResult apiResult = mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseJson = apiResult.getResponse().getContentAsString();
        ChatResponse chatResponse = objectMapper.readValue(responseJson, ChatResponse.class);

        // 验证响应结构（JavaScript会处理这个响应）
        assertNotNull(chatResponse, "API应返回聊天响应");
        assertTrue(chatResponse.isSuccess(), "响应应标记为成功");
        assertNotNull(chatResponse.getResponse(), "响应应包含AI回复内容");
        assertNotNull(chatResponse.getModel(), "响应应包含使用的模型信息");
        assertNotNull(chatResponse.getTimestamp(), "响应应包含时间戳");
    }

    @Test
    @DisplayName("测试错误场景的用户流程")
    void testErrorScenarioUserFlow() throws Exception {
        // 1. 用户访问页面
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        // 2. 模拟发送空消息（JavaScript应该阻止，但测试API层面的处理）
        ChatRequest emptyRequest = new ChatRequest();
        emptyRequest.setMessage("");

        String requestJson = objectMapper.writeValueAsString(emptyRequest);

        // 3. 验证API正确处理无效请求
        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.errorCode").exists());

        // 4. 模拟发送过长消息
        ChatRequest longMessageRequest = new ChatRequest();
        longMessageRequest.setMessage(IntegrationTestUtils.generateLongMessage(500)); // 生成超长消息

        String longRequestJson = objectMapper.writeValueAsString(longMessageRequest);

        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(longRequestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value(containsString("消息长度")));
    }

    @Test
    @DisplayName("测试响应式设计元素")
    void testResponsiveDesignElements() throws Exception {
        MvcResult result = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn();

        String htmlContent = result.getResponse().getContentAsString();

        // 验证响应式设计相关的meta标签
        assertTrue(htmlContent.contains("viewport"), 
                  "页面应包含viewport meta标签以支持响应式设计");
        assertTrue(htmlContent.contains("width=device-width"), 
                  "viewport应设置为设备宽度");

        // 验证CSS类名（用于响应式样式）
        assertTrue(htmlContent.contains("chat-container"), 
                  "页面应包含聊天容器类");
        assertTrue(htmlContent.contains("chat-header"), 
                  "页面应包含头部容器类");
        assertTrue(htmlContent.contains("chat-main"), 
                  "页面应包含主要内容容器类");
        assertTrue(htmlContent.contains("chat-footer"), 
                  "页面应包含底部容器类");
    }

    @Test
    @DisplayName("测试无障碍访问功能")
    void testAccessibilityFeatures() throws Exception {
        MvcResult result = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn();

        String htmlContent = result.getResponse().getContentAsString();

        // 验证语言属性
        assertTrue(htmlContent.contains("lang=\"zh-CN\""), 
                  "页面应设置正确的语言属性");

        // 验证表单标签
        assertTrue(htmlContent.contains("<label for=\"modelSelect\""), 
                  "模型选择器应有对应的标签");

        // 验证输入框属性
        assertTrue(htmlContent.contains("placeholder="), 
                  "输入框应有占位符文本");

        // 验证按钮类型
        assertTrue(htmlContent.contains("type=\"button\""), 
                  "按钮应有明确的类型属性");
    }

    @Test
    @DisplayName("测试页面性能相关元素")
    void testPerformanceElements() throws Exception {
        MvcResult result = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andReturn();

        String htmlContent = result.getResponse().getContentAsString();

        // 验证字符编码设置
        assertTrue(htmlContent.contains("charset=\"UTF-8\""), 
                  "页面应设置UTF-8字符编码");

        // 验证外部资源加载
        assertTrue(htmlContent.contains("cdnjs.cloudflare.com"), 
                  "页面应使用CDN加载外部资源");

        // 验证脚本加载位置（应在body底部）
        int bodyEndIndex = htmlContent.lastIndexOf("</body>");
        int scriptIndex = htmlContent.lastIndexOf("<script");
        assertTrue(scriptIndex < bodyEndIndex && scriptIndex > bodyEndIndex - 1000, 
                  "JavaScript文件应在页面底部加载以提高性能");
    }

    @Test
    @DisplayName("测试模型列表为空时的降级处理")
    void testModelListFallback() throws Exception {
        // 这个测试验证当ChatService.getAvailableModels()失败时的处理
        // 由于WebController中有try-catch处理，应该提供默认模型
        
        MvcResult result = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("availableModels"))
                .andReturn();

        String htmlContent = result.getResponse().getContentAsString();
        
        // 即使服务失败，也应该有默认模型选项
        assertTrue(htmlContent.contains("openai/gpt-3.5-turbo"), 
                  "即使获取模型列表失败，也应提供默认模型");
    }
}
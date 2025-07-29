/**
 * Spring AI Chat - 前端交互脚本
 * 实现AJAX聊天消息发送、实时响应显示和错误处理
 */

class ChatApp {
    constructor() {
        this.initializeElements();
        this.bindEvents();
        this.setupAutoResize();
        this.updateCharCount();
        this.setStatus('ready');
    }

    /**
     * 初始化DOM元素引用
     */
    initializeElements() {
        this.messageInput = document.getElementById('messageInput');
        this.sendButton = document.getElementById('sendButton');
        this.chatMessages = document.getElementById('chatMessages');
        this.modelSelect = document.getElementById('modelSelect');
        this.loadingIndicator = document.getElementById('loadingIndicator');
        this.errorModal = document.getElementById('errorModal');
        this.errorMessage = document.getElementById('errorMessage');
        this.statusIndicator = document.getElementById('statusIndicator');
        this.charCount = document.querySelector('.char-count');
        this.retryButton = document.getElementById('retryButton');
        this.dismissErrorButton = document.getElementById('dismissErrorButton');
        this.closeErrorModal = document.getElementById('closeErrorModal');
        
        // 存储最后发送的消息，用于重试功能
        this.lastMessage = '';
        this.lastModel = '';
    }

    /**
     * 绑定事件监听器
     */
    bindEvents() {
        // 发送按钮点击事件
        this.sendButton.addEventListener('click', () => this.sendMessage());
        
        // 输入框回车事件（Shift+Enter换行，Enter发送）
        this.messageInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                this.sendMessage();
            }
        });
        
        // 输入框内容变化事件
        this.messageInput.addEventListener('input', () => {
            this.updateCharCount();
            this.adjustTextareaHeight();
        });
        
        // 输入框焦点事件
        this.messageInput.addEventListener('focus', () => {
            this.messageInput.parentElement.classList.add('focused');
        });
        
        this.messageInput.addEventListener('blur', () => {
            this.messageInput.parentElement.classList.remove('focused');
        });
        
        // 模型选择变化事件
        this.modelSelect.addEventListener('change', () => {
            this.setStatus('ready');
            this.addVisualFeedback(this.modelSelect, 'success');
        });
        
        // 错误模态框事件
        this.retryButton.addEventListener('click', () => this.retryLastMessage());
        this.dismissErrorButton.addEventListener('click', () => this.hideErrorModal());
        this.closeErrorModal.addEventListener('click', () => this.hideErrorModal());
        
        // 点击模态框背景关闭
        this.errorModal.addEventListener('click', (e) => {
            if (e.target === this.errorModal) {
                this.hideErrorModal();
            }
        });
        
        // ESC键关闭模态框
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && this.errorModal.style.display !== 'none') {
                this.hideErrorModal();
            }
        });
        
        // 添加进度条元素
        this.createProgressBar();
    }

    /**
     * 设置文本框自动调整高度
     */
    setupAutoResize() {
        this.adjustTextareaHeight();
    }

    /**
     * 调整文本框高度
     */
    adjustTextareaHeight() {
        this.messageInput.style.height = 'auto';
        this.messageInput.style.height = Math.min(this.messageInput.scrollHeight, 120) + 'px';
    }



    /**
     * 设置状态指示器
     */
    setStatus(status) {
        this.statusIndicator.className = `status-indicator ${status}`;
        
        switch (status) {
            case 'ready':
                this.statusIndicator.textContent = '就绪';
                break;
            case 'sending':
                this.statusIndicator.textContent = '发送中...';
                break;
            case 'error':
                this.statusIndicator.textContent = '错误';
                break;
            default:
                this.statusIndicator.textContent = status;
        }
    }



    /**
     * 调用聊天API
     */
    async callChatAPI(message, model) {
        const requestBody = {
            message: message,
            model: model
        };
        
        const response = await fetch('/api/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(requestBody)
        });
        
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.error || `HTTP ${response.status}: ${response.statusText}`);
        }
        
        return await response.json();
    }

    /**
     * 添加消息到聊天区域
     */
    addMessage(content, type) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${type}-message`;
        
        const avatarDiv = document.createElement('div');
        avatarDiv.className = 'message-avatar';
        avatarDiv.innerHTML = type === 'user' ? '<i class="fas fa-user"></i>' : '<i class="fas fa-robot"></i>';
        
        const contentDiv = document.createElement('div');
        contentDiv.className = 'message-content';
        
        // 处理消息内容，支持简单的换行
        const formattedContent = this.formatMessageContent(content);
        contentDiv.innerHTML = formattedContent;
        
        messageDiv.appendChild(avatarDiv);
        messageDiv.appendChild(contentDiv);
        
        this.chatMessages.appendChild(messageDiv);
        this.scrollToBottom();
    }

    /**
     * 格式化消息内容
     */
    formatMessageContent(content) {
        // 转义HTML特殊字符
        const escaped = content
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
        
        // 将换行符转换为<br>标签
        return escaped.replace(/\n/g, '<br>');
    }

    /**
     * 显示打字机指示器
     */
    showTypingIndicator() {
        const typingDiv = document.createElement('div');
        typingDiv.className = 'message bot-message';
        typingDiv.id = 'typing-indicator';
        
        const avatarDiv = document.createElement('div');
        avatarDiv.className = 'message-avatar';
        avatarDiv.innerHTML = '<i class="fas fa-robot"></i>';
        
        const contentDiv = document.createElement('div');
        contentDiv.className = 'typing-indicator';
        contentDiv.innerHTML = `
            <span>AI正在思考</span>
            <div class="typing-dots">
                <div class="typing-dot"></div>
                <div class="typing-dot"></div>
                <div class="typing-dot"></div>
            </div>
        `;
        
        typingDiv.appendChild(avatarDiv);
        typingDiv.appendChild(contentDiv);
        
        this.chatMessages.appendChild(typingDiv);
        this.scrollToBottom();
        
        return typingDiv;
    }

    /**
     * 隐藏打字机指示器
     */
    hideTypingIndicator(typingElement) {
        if (typingElement && typingElement.parentNode) {
            typingElement.parentNode.removeChild(typingElement);
        }
    }

    /**
     * 带打字机效果添加消息
     */
    async addMessageWithTypewriter(content, type) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${type}-message`;
        
        const avatarDiv = document.createElement('div');
        avatarDiv.className = 'message-avatar';
        avatarDiv.innerHTML = type === 'user' ? '<i class="fas fa-user"></i>' : '<i class="fas fa-robot"></i>';
        
        const contentDiv = document.createElement('div');
        contentDiv.className = 'message-content';
        
        messageDiv.appendChild(avatarDiv);
        messageDiv.appendChild(contentDiv);
        
        this.chatMessages.appendChild(messageDiv);
        
        // 打字机效果
        const formattedContent = this.formatMessageContent(content);
        await this.typewriterEffect(contentDiv, formattedContent);
        
        this.scrollToBottom();
    }

    /**
     * 打字机效果实现
     */
    async typewriterEffect(element, text) {
        // 检查用户是否偏好减少动画
        const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
        
        if (prefersReducedMotion) {
            // 如果用户偏好减少动画，直接显示全部文本
            element.innerHTML = text;
            return;
        }
        
        element.innerHTML = '';
        const delay = Math.max(10, Math.min(50, 2000 / text.length)); // 动态调整速度
        
        // 处理HTML标签，确保不会在标签中间断开
        const htmlRegex = /<[^>]*>/g;
        let htmlTags = [];
        let plainText = text.replace(htmlRegex, (match, offset) => {
            htmlTags.push({ tag: match, position: offset });
            return '\0'.repeat(match.length); // 用空字符占位
        });
        
        for (let i = 0; i <= plainText.length; i++) {
            let currentText = plainText.substring(0, i);
            
            // 恢复HTML标签
            htmlTags.forEach(({ tag, position }) => {
                if (position < i) {
                    const start = position;
                    const end = position + tag.length;
                    currentText = currentText.substring(0, start) + tag + currentText.substring(end);
                }
            });
            
            element.innerHTML = currentText;
            this.scrollToBottom();
            
            if (i < plainText.length) {
                await new Promise(resolve => setTimeout(resolve, delay));
            }
        }
    }

    /**
     * 滚动到底部
     */
    scrollToBottom() {
        setTimeout(() => {
            this.chatMessages.scrollTop = this.chatMessages.scrollHeight;
        }, 100);
    }



    /**
     * 隐藏错误模态框
     */
    hideErrorModal() {
        this.errorModal.style.display = 'none';
    }

    /**
     * 重试最后一条消息
     */
    async retryLastMessage() {
        this.hideErrorModal();
        
        if (this.lastMessage) {
            // 恢复输入框内容
            this.messageInput.value = this.lastMessage;
            this.modelSelect.value = this.lastModel;
            this.updateCharCount();
            this.adjustTextareaHeight();
            
            // 发送消息
            await this.sendMessage();
        }
    }

    /**
     * 创建进度条
     */
    createProgressBar() {
        const progressBar = document.createElement('div');
        progressBar.className = 'progress-bar';
        progressBar.id = 'progressBar';
        
        const progressFill = document.createElement('div');
        progressFill.className = 'progress-bar-fill';
        
        progressBar.appendChild(progressFill);
        document.body.appendChild(progressBar);
        
        this.progressBar = progressBar;
    }

    /**
     * 显示进度条
     */
    showProgressBar() {
        if (this.progressBar) {
            this.progressBar.classList.add('active');
        }
    }

    /**
     * 隐藏进度条
     */
    hideProgressBar() {
        if (this.progressBar) {
            this.progressBar.classList.remove('active');
        }
    }

    /**
     * 添加视觉反馈
     */
    addVisualFeedback(element, type) {
        // 移除现有的反馈类
        element.classList.remove('success', 'error', 'sending');
        
        // 添加新的反馈类
        element.classList.add(type);
        
        // 在动画完成后移除类
        setTimeout(() => {
            element.classList.remove(type);
        }, 600);
    }

    /**
     * 增强的显示加载指示器
     */
    showLoading() {
        this.loadingIndicator.style.display = 'flex';
        this.showProgressBar();
        this.sendButton.classList.add('sending');
    }

    /**
     * 增强的隐藏加载指示器
     */
    hideLoading() {
        this.loadingIndicator.style.display = 'none';
        this.hideProgressBar();
        this.sendButton.classList.remove('sending');
    }

    /**
     * 增强的错误处理
     */
    showError(message) {
        this.errorMessage.textContent = message;
        this.errorModal.style.display = 'flex';
        this.addVisualFeedback(this.sendButton, 'error');
        this.addVisualFeedback(this.messageInput.parentElement, 'error');
    }

    /**
     * 增强的字符计数更新
     */
    updateCharCount() {
        const count = this.messageInput.value.length;
        this.charCount.textContent = `${count}/2000`;
        
        // 移除现有的错误类
        this.charCount.classList.remove('error');
        
        if (count > 2000) {
            this.charCount.style.color = '#ef4444';
            this.charCount.classList.add('error');
        } else if (count > 1800) {
            this.charCount.style.color = '#ef4444';
        } else if (count > 1500) {
            this.charCount.style.color = '#f59e0b';
        } else {
            this.charCount.style.color = '#9ca3af';
        }
    }

    /**
     * 增强的发送消息方法
     */
    async sendMessage() {
        const message = this.messageInput.value.trim();
        const selectedModel = this.modelSelect.value;
        
        if (!message) {
            this.messageInput.focus();
            this.addVisualFeedback(this.messageInput.parentElement, 'error');
            return;
        }
        
        if (message.length > 2000) {
            this.showError('消息长度不能超过2000个字符');
            return;
        }
        
        // 保存消息用于重试
        this.lastMessage = message;
        this.lastModel = selectedModel;
        
        // 显示用户消息
        this.addMessage(message, 'user');
        
        // 清空输入框并重置高度
        this.messageInput.value = '';
        this.adjustTextareaHeight();
        this.updateCharCount();
        
        // 设置发送状态
        this.setStatus('sending');
        this.sendButton.disabled = true;
        this.showLoading();
        
        // 显示打字机效果
        const typingIndicator = this.showTypingIndicator();
        
        try {
            const response = await this.callChatAPI(message, selectedModel);
            
            if (response.success) {
                // 移除打字机效果
                this.hideTypingIndicator(typingIndicator);
                
                // 添加机器人响应，带有打字机效果
                await this.addMessageWithTypewriter(response.response, 'bot');
                this.setStatus('ready');
                this.addVisualFeedback(this.sendButton, 'success');
            } else {
                this.hideTypingIndicator(typingIndicator);
                throw new Error(response.error || '未知错误');
            }
        } catch (error) {
            console.error('Chat API Error:', error);
            this.hideTypingIndicator(typingIndicator);
            this.setStatus('error');
            this.showError(this.getErrorMessage(error));
        } finally {
            this.hideLoading();
            this.sendButton.disabled = false;
            this.messageInput.focus();
        }
    }

    /**
     * 获取友好的错误消息
     */
    getErrorMessage(error) {
        const message = error.message || error.toString();
        
        if (message.includes('Failed to fetch') || message.includes('NetworkError')) {
            return '网络连接失败，请检查网络连接后重试';
        } else if (message.includes('timeout')) {
            return '请求超时，请稍后重试';
        } else if (message.includes('401')) {
            return 'API认证失败，请检查API密钥配置';
        } else if (message.includes('403')) {
            return 'API访问被拒绝，请检查权限设置';
        } else if (message.includes('429')) {
            return 'API调用频率过高，请稍后重试';
        } else if (message.includes('500')) {
            return '服务器内部错误，请稍后重试';
        } else if (message.includes('502') || message.includes('503') || message.includes('504')) {
            return '服务暂时不可用，请稍后重试';
        } else {
            return message || '发生未知错误，请重试';
        }
    }
}

// 页面加载完成后初始化聊天应用
document.addEventListener('DOMContentLoaded', () => {
    window.chatApp = new ChatApp();
    
    // 添加一些调试信息
    console.log('Spring AI Chat 应用已初始化');
    console.log('支持的功能：');
    console.log('- AJAX聊天消息发送');
    console.log('- 实时响应显示');
    console.log('- 加载状态指示');
    console.log('- 错误处理和重试');
    console.log('- 响应式设计');
});
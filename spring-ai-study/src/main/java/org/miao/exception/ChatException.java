package org.miao.exception;

/**
 * 聊天服务基础异常类
 * 所有聊天相关异常的父类
 */
public class ChatException extends RuntimeException {
    
    public ChatException(String message) {
        super(message);
    }
    
    public ChatException(String message, Throwable cause) {
        super(message, cause);
    }
}
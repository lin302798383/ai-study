package org.miao.exception;

/**
 * API连接异常
 * 当无法连接到OpenRouter API时抛出
 */
public class ApiConnectionException extends ChatException {
    
    public ApiConnectionException(String message) {
        super(message);
    }
    
    public ApiConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
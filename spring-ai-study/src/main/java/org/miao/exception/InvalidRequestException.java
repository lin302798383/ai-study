package org.miao.exception;

/**
 * 无效请求异常
 * 当请求参数无效或格式错误时抛出
 */
public class InvalidRequestException extends ChatException {
    
    public InvalidRequestException(String message) {
        super(message);
    }
    
    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
package org.miao.exception;

/**
 * 模型不可用异常
 * 当请求的AI模型不可用时抛出
 */
public class ModelNotAvailableException extends ChatException {
    
    public ModelNotAvailableException(String message) {
        super(message);
    }
    
    public ModelNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
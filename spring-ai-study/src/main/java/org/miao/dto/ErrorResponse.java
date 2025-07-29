package org.miao.dto;

import java.time.LocalDateTime;

/**
 * 统一错误响应数据传输对象
 * 用于API错误响应的标准格式
 */
public class ErrorResponse {
    
    private boolean success;
    private String error;
    private String errorCode;
    private LocalDateTime timestamp;
    private String path;
    
    public ErrorResponse() {
        this.success = false;
        this.timestamp = LocalDateTime.now();
    }
    
    public ErrorResponse(String error, String errorCode) {
        this();
        this.error = error;
        this.errorCode = errorCode;
    }
    
    public ErrorResponse(String error, String errorCode, String path) {
        this(error, errorCode);
        this.path = path;
    }
    
    public static ErrorResponse of(String error, String errorCode) {
        return new ErrorResponse(error, errorCode);
    }
    
    public static ErrorResponse of(String error, String errorCode, String path) {
        return new ErrorResponse(error, errorCode, path);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    @Override
    public String toString() {
        return "ErrorResponse{" +
                "success=" + success +
                ", error='" + error + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", timestamp=" + timestamp +
                ", path='" + path + '\'' +
                '}';
    }
}
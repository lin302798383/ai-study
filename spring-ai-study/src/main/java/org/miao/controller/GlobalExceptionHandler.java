package org.miao.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.miao.dto.ErrorResponse;
import org.miao.exception.ApiConnectionException;
import org.miao.exception.ChatException;
import org.miao.exception.InvalidRequestException;
import org.miao.exception.ModelNotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 全局异常处理器
 * 统一处理应用程序中的各种异常，返回标准化的错误响应
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理无效请求异常
     */
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequestException(
            InvalidRequestException e, WebRequest request) {
        
        logger.warn("无效请求异常: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            e.getMessage(), 
            "INVALID_REQUEST", 
            getRequestPath(request)
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 处理模型不可用异常
     */
    @ExceptionHandler(ModelNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleModelNotAvailableException(
            ModelNotAvailableException e, WebRequest request) {
        
        logger.warn("模型不可用异常: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            e.getMessage(), 
            "MODEL_NOT_AVAILABLE", 
            getRequestPath(request)
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 处理API连接异常
     */
    @ExceptionHandler(ApiConnectionException.class)
    public ResponseEntity<ErrorResponse> handleApiConnectionException(
            ApiConnectionException e, WebRequest request) {
        
        logger.error("API连接异常: {}", e.getMessage(), e);
        
        ErrorResponse errorResponse = ErrorResponse.of(
            e.getMessage(), 
            "API_CONNECTION_ERROR", 
            getRequestPath(request)
        );
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    /**
     * 处理通用聊天异常
     */
    @ExceptionHandler(ChatException.class)
    public ResponseEntity<ErrorResponse> handleChatException(
            ChatException e, WebRequest request) {
        
        logger.error("聊天服务异常: {}", e.getMessage(), e);
        
        ErrorResponse errorResponse = ErrorResponse.of(
            e.getMessage(), 
            "CHAT_SERVICE_ERROR", 
            getRequestPath(request)
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 处理请求参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, WebRequest request) {
        
        logger.warn("请求参数验证失败: {}", e.getMessage());
        
        List<String> errors = new ArrayList<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        
        String errorMessage = "请求参数验证失败: " + String.join(", ", errors);
        
        ErrorResponse errorResponse = ErrorResponse.of(
            errorMessage, 
            "VALIDATION_ERROR", 
            getRequestPath(request)
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(
            BindException e, WebRequest request) {
        
        logger.warn("数据绑定异常: {}", e.getMessage());
        
        List<String> errors = new ArrayList<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        
        String errorMessage = "数据绑定失败: " + String.join(", ", errors);
        
        ErrorResponse errorResponse = ErrorResponse.of(
            errorMessage, 
            "BINDING_ERROR", 
            getRequestPath(request)
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException e, WebRequest request) {
        
        logger.warn("约束违反异常: {}", e.getMessage());
        
        List<String> errors = new ArrayList<>();
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            errors.add(violation.getPropertyPath() + ": " + violation.getMessage());
        }
        
        String errorMessage = "约束验证失败: " + String.join(", ", errors);
        
        ErrorResponse errorResponse = ErrorResponse.of(
            errorMessage, 
            "CONSTRAINT_VIOLATION", 
            getRequestPath(request)
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 处理HTTP消息不可读异常（JSON格式错误等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e, WebRequest request) {
        
        logger.warn("HTTP消息不可读异常: {}", e.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "请求格式错误，请检查JSON格式是否正确", 
            "MESSAGE_NOT_READABLE", 
            getRequestPath(request)
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 处理HTTP请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e, WebRequest request) {
        
        logger.warn("HTTP请求方法不支持异常: {}", e.getMessage());
        
        String errorMessage = String.format("请求方法 '%s' 不支持，支持的方法: %s", 
            e.getMethod(), String.join(", ", e.getSupportedMethods()));
        
        ErrorResponse errorResponse = ErrorResponse.of(
            errorMessage, 
            "METHOD_NOT_SUPPORTED", 
            getRequestPath(request)
        );
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException e, WebRequest request) {
        
        logger.warn("请求路径未找到: {}", e.getRequestURL());
        
        String errorMessage = String.format("请求路径 '%s' 未找到", e.getRequestURL());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            errorMessage, 
            "NOT_FOUND", 
            getRequestPath(request)
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 处理所有其他未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception e, WebRequest request) {
        
        logger.error("未处理的异常: {}", e.getMessage(), e);
        
        ErrorResponse errorResponse = ErrorResponse.of(
            "服务器内部错误，请稍后重试", 
            "INTERNAL_SERVER_ERROR", 
            getRequestPath(request)
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 从WebRequest中提取请求路径
     */
    private String getRequestPath(WebRequest request) {
        try {
            return request.getDescription(false).replace("uri=", "");
        } catch (Exception e) {
            logger.debug("无法获取请求路径: {}", e.getMessage());
            return "unknown";
        }
    }
}

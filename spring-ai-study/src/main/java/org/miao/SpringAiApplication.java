package org.miao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring AI Study应用程序主类
 * 集成OpenRouter服务进行AI模型调用
 */
@SpringBootApplication
public class SpringAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringAiApplication.class, args);
    }
}

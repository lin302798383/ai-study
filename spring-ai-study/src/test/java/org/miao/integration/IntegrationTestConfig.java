package org.miao.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * 集成测试配置类
 * 提供集成测试所需的特殊配置和Bean
 */
@TestConfiguration
@Profile("integration")
public class IntegrationTestConfig {

    /**
     * 配置MockMvc用于集成测试
     * 
     * @param webApplicationContext Web应用程序上下文
     * @return 配置好的MockMvc实例
     */
    @Bean
    @Primary
    public MockMvc mockMvc(WebApplicationContext webApplicationContext) {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
    }
}
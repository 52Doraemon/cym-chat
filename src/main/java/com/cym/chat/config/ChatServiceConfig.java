package com.cym.chat.config;

import com.cym.chat.service.ChatAuthService;
import com.cym.chat.service.impl.DefaultChatAuthServiceImpl;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author Yummy
 * chat服务配置信息
 */
@SpringBootConfiguration
public class ChatServiceConfig {
    // Chat 鉴权服务
    @Bean
    @ConditionalOnMissingBean(value = ChatAuthService.class)
    public ChatAuthService chatAuthService(){
        return new DefaultChatAuthServiceImpl();
    }
}

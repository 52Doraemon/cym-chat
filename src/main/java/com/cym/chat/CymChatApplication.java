package com.cym.chat;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@EnableKnife4j
@SpringBootApplication
public class CymChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(CymChatApplication.class, args);
        log.info("GPT-3.5 聊天小助手启动完成!");
    }
}
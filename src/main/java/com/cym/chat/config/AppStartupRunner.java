package com.cym.chat.config;

import com.cym.chat.utils.ApiKeyPoolUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 密钥池
 * */
@Slf4j
@Component
public class AppStartupRunner implements CommandLineRunner {


    @Autowired
    private ApiKeyPoolUtil apiKeyPoolUtil;

    /**
     * 所有的 Beans 都已经被创建，Spring ApplicationContext 已经被初始化，且应用已经准备好开始接收请求
     * AppStartupRunner 中的 run 方法是在 Spring Boot 应用启动后，但在应用开始接收请求之前被调用的。
     * @param args
     */
    @Override
    public void run(String... args) {
        // 这里可以处理应用启动后需要执行的逻辑
        log.info("所有的 Beans 都已经被创建，Spring ApplicationContext 已经被初始化，且应用已经准备好开始接收请求");
        apiKeyPoolUtil.printAllApiKeys();
    }

}

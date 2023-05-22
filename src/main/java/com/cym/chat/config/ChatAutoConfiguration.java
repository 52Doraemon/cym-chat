package com.cym.chat.config;

import com.dtflys.forest.springboot.annotation.ForestScan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

/**
 * @author Yummy
 * Chat 模块
 */
@Slf4j
@AutoConfiguration // 该类是一个自动配置类，Spring Boot 会自动读取并应用这个类中的配置。
@ComponentScan(basePackages = {"com.cym.chat"})
@ForestScan(basePackages = {"com.cym.chat.api"})
public class ChatAutoConfiguration implements EnvironmentAware {

    /**
     * Spring 在初始化类时会调用这个方法，并传入当前的 Environment
     * @param environment 可以用来获取环境属性，比如系统属性、环境变量和 application.properties 或 application.yml 文件中的属性。
     */
    @Override
    public void setEnvironment(Environment environment) {
        // 此处注册全局环境
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profileName : activeProfiles) {
            if (profileName.equals("development")) {
                log.info("Running in development 开发环境!--------------------------------------------");
                // do something specific for development mode
            } else if (profileName.equals("test")) {
                log.info("Running in test 测试环境!---------------------------------------------------");
                // do something specific for test mode
            } else if (profileName.equals("production")) {
                log.info("Running in production 生产环境!---------------------------------------------");
                // do something specific for production mode
            }
        }
    }
}

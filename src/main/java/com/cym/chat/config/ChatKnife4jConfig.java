package com.cym.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class ChatKnife4jConfig {

    @Value("${server.port}")
    private int serverPort;

    @Bean(value = "chatApiDoc")
    public Docket chatApiDoc() {
        String groupName = "ChatGPT客服";
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("ChatGPT-API")
                .description("ChatGPT服务接口文档")
                .termsOfServiceUrl(String.format("http://localhost:%d/doc.html", serverPort))
                .version("@latest")
                .build();
        return new Docket(DocumentationType.SWAGGER_2)
                .host(String.format("http://localhost:%d/", serverPort))
                .apiInfo(apiInfo)
                .groupName(groupName)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.cym.chat"))
                .paths(PathSelectors.any())
                .build();
    }
}
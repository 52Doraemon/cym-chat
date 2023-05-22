package com.cym.chat.service.impl;

import com.cym.chat.params.chat.ChatParams;
import com.cym.chat.params.chat.ChatResultStream;
import com.cym.chat.service.ChatApiService;
import com.cym.chat.utils.ApiKeyPoolUtil;
import com.dtflys.forest.exceptions.ForestRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class ChatApiServiceImpl implements ChatApiService {

    private final WebClient webClient;

    private ApiKeyPoolUtil apiKeyPoolUtil;

    private static final int MAX_RETRIES = 3;

    @Autowired
    public ChatApiServiceImpl(ApiKeyPoolUtil apiKeyPoolUtil) {
        this.webClient = WebClient.create("https://api.openai-proxy.com");
        this.apiKeyPoolUtil = apiKeyPoolUtil;
    }

    /**
     * 数据的流式响应
     * @param params
     * @return
     */
    @Override
    public Flux<ChatResultStream> doChat(ChatParams params) {
        params.setStream(true);
        ObjectMapper objectMapper = new ObjectMapper();

        return webClient.post()
                .uri("/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKeyPoolUtil.getAvailableApiKeyByRoundRobin())
                .bodyValue(params)
                .retrieve()
                .bodyToFlux(String.class) // 解析为字符串的Flux
                .map(responseJson -> {
                    if (responseJson.equals("[DONE]")) {
                        System.out.println(responseJson);
                        // 在请求接受完成时执行逻辑
                        // 可以返回特定的ChatResult对象，或者使用EmptyChatResult表示请求已完成
                        return new ChatResultStream(); // 返回一个空的ChatResult对象，表示请求已完成
                    } else {
                        try {
                            System.out.println(objectMapper.readValue(responseJson, ChatResultStream.class));
                            return objectMapper.readValue(responseJson, ChatResultStream.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
                .retryWhen(Retry.backoff(MAX_RETRIES, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof WebClientResponseException.Unauthorized))
                .onErrorMap(WebClientResponseException.class, ex -> new ForestRuntimeException("调用API时发生错误，HTTP状态码：" + ex.getStatusCode()));
    }
}

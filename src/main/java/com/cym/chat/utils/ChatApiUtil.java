package com.cym.chat.utils;

import com.cym.chat.api.chat.ChatApi;
import com.cym.chat.params.chat.ChatParams;
import com.cym.chat.params.chat.ChatResult;
import com.dtflys.forest.exceptions.ForestRuntimeException;
import com.dtflys.forest.http.ForestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ChatApiUtil {

    @Resource
    private ChatApi chatApi;
    private static final int MAX_RETRIES = 3; // 最大重试次数
    private ApiKeyPoolUtil apiKeyPoolUtil;

    @Autowired
    public ChatApiUtil(ApiKeyPoolUtil apiKeyPoolUtil) {
        this.apiKeyPoolUtil = apiKeyPoolUtil;
    }

    public ChatResult doChat(ChatParams params) throws ForestRuntimeException {
        int numRetries = 0; // 已重试次数
        while (true) {
            Map<String, Object> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + apiKeyPoolUtil.getAvailableApiKeyByRoundRobin());
            // 调用API
            ForestResponse<ChatResult> response = chatApi.ChatCompletions(headers, params);
            log.info("请求状态：" + response.getStatusCode() + "|" + response.getContent());

            // 判断HTTP状态码
            if (response.getStatusCode() == 200) { // 成功
                ChatResult result = response.getResult();
                // 返回结果
                return result;
            } else if (response.getStatusCode() == 401) { // 需要重新授权
                numRetries++;
                // 密钥失效，加入黑名单
                apiKeyPoolUtil.handleInvalidApiKey(response.getRequest().getHeaders().get("Authorization").substring(7));
                if (numRetries >= MAX_RETRIES) {
                    // 获取可用的API密钥
                    if (apiKeyPoolUtil.getApiKeys().size() == 0) {
                        // 没有可用的API密钥
                        log.error("没有可用的API密钥，Chat请求失败");
                        throw new ForestRuntimeException("没有可用的API密钥");
                    }
                    throw new ForestRuntimeException("尝试重新授权达到最大次数，接口发送异常，请检查接口密钥[" + response.getRequest().getHeaders().get("Authorization").substring(7) + "]准确性。");
                }
            } else if (response.getStatusCode() == 429) { // 其他错误
                log.warn("HTTP状态码：" + response.getStatusCode() + "接口被限速啦！请十秒后重试~");
                //throw new ForestRuntimeException("HTTP状态码：" + response.getStatusCode() + "接口被限速啦！请十秒后重试~");
            } else if (response.getStatusCode() == -1) { // 请求超时
                log.warn("请求超时，正在重试... (" + (numRetries + 1) + "/" + MAX_RETRIES + ")");
                numRetries++;
                if (numRetries == MAX_RETRIES)
                    throw new ForestRuntimeException("请求超时，重试" + MAX_RETRIES + "次后仍然失败。");
            } else if (response.getStatusCode()==400) {
                throw new IllegalCallerException("单次输入字符串过多，请重新输入！");
            }
        }
    }
}
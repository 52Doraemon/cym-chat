package com.cym.chat.utils;

import com.cym.chat.params.apikey.ApiKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Slf4j
@Component
public class ApiKeyPoolUtil {

    @Value("${openai.chat.key}")
    private String apiKeysConfig;

    // API密钥列表
    private static List<ApiKey> apiKeys;

    // 轮询索引
    private static int index = 0;

    // 不可用密钥
    private static Set<ApiKey> blacklistedKeys;

    /**
     * 在Spring容器创建ApiKeyPoolUtil的Bean并完成依赖注入之后执行,初始化配置
     */
    @PostConstruct
    public void init() {
        if (apiKeysConfig.isEmpty()) {
            log.warn("密钥池为空！");
            return;
        }
        apiKeys = new ArrayList<>();
        blacklistedKeys = new HashSet<>();
        String[] keysArray = apiKeysConfig.split(",");
        for (String key : keysArray) {
            apiKeys.add(new ApiKey(key));
        }
        log.info("初始化密钥池成功！");
    }

    /**
     * ApiKeyPool 构造函数
     *
     * @param apiKeyStrings API密钥字符串列表
     */
    public ApiKeyPoolUtil(List<String> apiKeyStrings) {
        this.apiKeys = new ArrayList<>();
        for (String key : apiKeyStrings) {
            apiKeys.add(new ApiKey(key));
        }
        this.blacklistedKeys = new HashSet<>();
    }

    /**
     * 轮询获取一个可用的API密钥
     *
     * @return 轮询选取的可用API密钥字符串，如果没有可用的密钥则返回 null
     */
    public synchronized static String getAvailableApiKeyByRoundRobin() {
        // 存储可用API密钥的列表
        List<ApiKey> availableKeys = new ArrayList<>();

        // 遍历所有API密钥，将可用的密钥添加到 availableKeys 列表中
        for (ApiKey key : apiKeys) {
            if (key.isAvailable() && !blacklistedKeys.contains(key)) {
                availableKeys.add(key);
            }
        }

        if (availableKeys.isEmpty()) {
            // 如果没有可用的密钥
            throw new IllegalStateException();
        }

        // 从 availableKeys 列表中轮询选取一个API密钥，并返回密钥字符串
        index = index % availableKeys.size();
        String key = availableKeys.get(index).getKey();
        index++;
        return key;
    }

    /**
     * 处理失效密钥
     *
     * @param apiKey 密钥
     */
    public synchronized static void handleInvalidApiKey(String apiKey) {
        ApiKey key = new ApiKey(apiKey);

        // 遍历 apiKeys 列表，找到对应的密钥并设置为不可用
        for (ApiKey apikey : apiKeys) {
            if (apikey.getKey().equals(apiKey)) {
                apikey.setAvailable(false);
                break;
            }
        }

        // 将不可用的密钥添加到 blacklistedKeys 集合中
        blacklistedKeys.add(key);
        log.info("失效密钥处理：{}", apiKey);
    }

    /**
     * 获取API密钥列表
     *
     * @return API密钥列表
     */
    public List<ApiKey> getApiKeys() {
        return apiKeys;
    }

    /**
     * 获取所有有效的API密钥
     *
     * @return 所有有效的API密钥列表
     */
    public synchronized static List<String> getAllAvailableApiKeys() {
        int count = 0;
        List<String> availableKeys = new ArrayList<>();
        log.info("---------------------------------------有效密钥列表：");
        for (ApiKey key : apiKeys) {
            if (key.isAvailable() && !blacklistedKeys.contains(key.getKey())) {
                count++;
                log.warn(key.getKey());
            }
        }
        log.info("---------------------------------------有效密钥数：{}", count);
        return availableKeys;
    }

    /**
     * 获取所有失效的API密钥
     *
     * @return 所有失效的API密钥列表
     */
    public synchronized static List<String> getAllInvalidApiKeys() {
        int count = 0;
        List<String> invalidKeys = new ArrayList<>();
        log.info("所有失效的API密钥：");
        for (ApiKey key : blacklistedKeys) {
            log.warn(key.getKey());
            count++;
        }
        log.info("---------------------------------------无效密钥数：{}", count);
        return invalidKeys;
    }

    /**
     * 打印所有的api key
     */
    public void printAllApiKeys() {
        int count = 0;
        log.info("---------------------------------------有效密钥列表：");
        for (ApiKey apiKey : this.apiKeys) {
            if (apiKey.isAvailable()) {
                log.info(apiKey.getKey());
                count++;
            }
        }
        log.info("---------------------------------------有效密钥数：{}", count);
    }
}
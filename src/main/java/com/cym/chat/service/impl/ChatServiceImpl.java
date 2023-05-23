package com.cym.chat.service.impl;

import com.cym.chat.api.chat.ChatApi;
import com.cym.chat.api.edit.EditApi;
import com.cym.chat.api.image.ImageApi;
import com.cym.chat.dto.ChatDTO;
import com.cym.chat.params.chat.ChatMessage;
import com.cym.chat.params.chat.ChatParams;
import com.cym.chat.params.chat.ChatResult;
import com.cym.chat.params.chat.model.ChoiceModel;
import com.cym.chat.params.constant.ChatRoleConst;
import com.cym.chat.params.edit.EditParams;
import com.cym.chat.params.edit.EditResult;
import com.cym.chat.params.edit.model.EditChoiceModel;
import com.cym.chat.params.image.ImageParams;
import com.cym.chat.params.image.ImageResult;
import com.cym.chat.params.image.model.ImageChoiceModel;
import com.cym.chat.service.ChatAuthService;
import com.cym.chat.service.ChatService;
import com.cym.chat.utils.ApiKeyPoolUtil;
import com.cym.chat.utils.ChatApiUtil;
import com.cym.chat.utils.ChatCacheUtil;
import com.dtflys.forest.exceptions.ForestRuntimeException;
import com.dtflys.forest.http.ForestResponse;
import kotlin.jvm.Throws;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Yummy
 * OpenAI相关服务实现
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private final String jiZaiToken;
    private final String ideaPluginToken;
    private final String roleSystemJiZai;
    private final ChatApiUtil chatApiUtil;
    private final ChatApi chatApi;
    private final EditApi editApi;
    private final ImageApi imageApi;
    private final ChatAuthService authService;
    private final ApiKeyPoolUtil apiKeyPoolUtil;

    @Autowired
    public ChatServiceImpl(
            @Value("${token.nopsw.jizai}") String jiZaiToken,
            @Value("${token.nopsw.ideaplugin}") String ideaPluginToken,
            @Value("${role.system.jizai}") String roleSystemJiZai,
            ChatApiUtil chatApiUtil,
            ChatApi chatApi,
            EditApi editApi,
            ImageApi imageApi,
            ChatAuthService authService,
            ApiKeyPoolUtil apiKeyPoolUtil
    ) {
        this.jiZaiToken = jiZaiToken;
        this.ideaPluginToken = ideaPluginToken;
        this.roleSystemJiZai = roleSystemJiZai;
        this.chatApiUtil = chatApiUtil;
        this.chatApi = chatApi;
        this.editApi = editApi;
        this.imageApi = imageApi;
        this.authService = authService;
        this.apiKeyPoolUtil = apiKeyPoolUtil;
    }
    
    
    /**
     * 从缓存中提取消息
     *
     * @param sessionId
     * @return
     */
    public List<ChatMessage> getChatMessagesBySessionId(String sessionId) {
        List<ChatMessage> messages = ChatCacheUtil.get(sessionId);
        return messages;
    }
    
    /**
     * 推送数据接口，数据更新到提示集中
     *
     * @param dto
     * @return
     * @see ChatCacheUtil.promptMap
     */
    public String handlePush(ChatDTO dto) {
        if (jiZaiToken.equals(dto.getChatId())) {
            // 写入缓存提示集
            ChatCacheUtil.promptMap.put(jiZaiToken, buildSystenMessage(dto.getContent()));
            System.out.println(ChatCacheUtil.promptMap.get(dto.getChatId()));
            return "注入数据成功";
        }
        return "注入失败，请重新注入·";
    }

    /**
     * 设置请求头
     * 格式：Authorization: Bearer OPENAI_API_KEY
     * 所有 API 请求都应在AuthorizationHTTP 标头中包含 API 密钥
     *
     * @return
     */
    @Override
    public Map<String, Object> headers() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + apiKeyPoolUtil.getAvailableApiKeyByRoundRobin());
        return headers;
    }

    /**
     * 构建UserMessage角色对话
     *
     * @param content
     * @return
     */
    @Override
    public ChatMessage buildUserMessage(String content) {
        ChatMessage message = new ChatMessage();
        message.setRole(ChatRoleConst.USER);
        message.setContent(content);
        return message;
    }

    /**
     * 构建SystenMessage角色对话
     *
     * @param content
     * @return
     */
    @Override
    public ChatMessage buildSystenMessage(String content) {
        ChatMessage message = new ChatMessage();
        message.setRole(ChatRoleConst.SYSTEM);
        message.setContent(content);
        return message;
    }

    /**
     * 构建AssistantMessage角色对话
     *
     * @param content
     * @return
     */
    @Override
    public ChatMessage buildAssistantMessage(String content) {
        ChatMessage message = new ChatMessage();
        message.setRole(ChatRoleConst.ASSISTANT);
        message.setContent(content);
        return message;
    }

    @Override
    public ChatResult doChat(ChatParams params) {
        // 写入用户ID
        params.setUser(authService.getUserId());
        ForestResponse<ChatResult> response = chatApi.ChatCompletions(headers(), params);
        return response.getResult();
    }

    @Override
    @Throws(exceptionClasses = ForestRuntimeException.class)
    public ChatResult doChat(ChatParams params, String chatId) {
        // 写入用户ID
        params.setUser(authService.getUserId());

        // 发送请求
        ChatResult result = chatApiUtil.doChat(params);

        // 获取返回结果
        List<ChoiceModel> choices = result.getChoices();

        // 所有有效的API密钥列表
        ApiKeyPoolUtil.getAllAvailableApiKeys();

        // 获取所有失效的API密钥
        ApiKeyPoolUtil.getAllInvalidApiKeys();

        // 获取第一条结果
        ChoiceModel choice = choices.get(0);

        // 获取消息
        ChatMessage message = choice.getMessage();

        // 写入缓存
        List<ChatMessage> messages = ChatCacheUtil.get(chatId);
        messages.add(message);
        ChatCacheUtil.put(chatId, messages);

        // 返回结果
        return result;
    }


    @Override
    public String doChatStr(ChatParams params, String chatId) {
        params.setUser(authService.getUserId());
        return chatApi.ChatCompletionsStr(headers(), params);
    }

    @Override
    public EditResult doEdit(EditParams params) {
        ForestResponse<EditResult> response = editApi.ChatEdits(headers(), params);
        return response.getResult();
    }

    @Override
    public ImageResult doDraw(ImageParams params) {
        // 写入用户
        params.setUser(authService.getUserId());
        ForestResponse<ImageResult> response = imageApi.ChatDraw(headers(), params);
        return response.getResult();
    }

    @Override
    public List<ChatMessage> getContext(String chatId) {
        return ChatCacheUtil.get(chatId);
    }


    @Override
    public List<ChatMessage> getContext(String chatId, Integer num) {
        List<ChatMessage> messages = getContext(chatId);
        int msgSize = messages.size();

        if (msgSize > num) {
            return messages.subList(msgSize - num, msgSize);
        } else {
            return messages;
        }
    }

    @Override
    public String simpleResult(ChatResult result) {
        // 获取返回结果
        List<ChoiceModel> choices = result.getChoices();
        // 获取第一条结果
        ChoiceModel choice = choices.get(0);
        // 获取消息
        ChatMessage message = choice.getMessage();
        // 返回内容
        return message.getContent();
    }

    @Override
    public String simpleResult(EditResult result) {
        List<EditChoiceModel> choices = result.getChoices();
        // 获取结果
        EditChoiceModel choice = choices.get(0);
        // 获取消息
        return choice.getText();
    }

    @Override
    public List<String> simpleResult(ImageResult result) {
        List<ImageChoiceModel> choices = result.getData();
        return choices.stream().map(ImageChoiceModel::getUrl).collect(Collectors.toList());
    }
}

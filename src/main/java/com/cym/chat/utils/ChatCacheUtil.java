package com.cym.chat.utils;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ObjectUtil;
import com.cym.chat.params.chat.ChatMessage;
import com.cym.chat.params.constant.ChatRoleConst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 携带信息。聊天消息缓存实现，用于存储和获取聊天消息。它使用了一个基于先进先出 (FIFO) 策略的本地缓存，缓存最大容量为 100。
 */
public class ChatCacheUtil {
    public static final Cache<String, List<ChatMessage>> chats = CacheUtil.newFIFOCache(100);
    
    /**
     * 限定最大语境字符数量
     * 基于gpt-3.5-turbo模型可以接收4096个Token（经测试为2700个中文字符）
     */
    private static final Integer MAX_CHAR = 2500;
    
    /**
     * 不过期（不溢出）的上下文提示，仅由数据推送接口更新
     */
    public static Map<String, ChatMessage> promptMap = new HashMap<>();
    
    
    /**
     * * 将聊天消息列表存储到缓存中。此方法接收一个键（key）和一个 ChatMessage 对象列表作为值（value）。如果值的大小超过 100，它会截取列表的最后 100 个元素并将其存储到缓存中。
     * ******************************************************************************************************************************************
     * 修复BUG：int fromIndex = Math.max(listSize - 10, 0);确保value.subList(fromIndex, listSize);的fromIndex不是负数
     * date 2023/4/26
     * ******************************************************************************************************************************************
     *
     * @param key
     * @param value
     */
    public static void put(String key, List<ChatMessage> value) {
        int listSize = value.size();
        // 创建一个新列表，默认情况下等于输入列表
        List<ChatMessage> newMessages = value;
        // 判断输入列表的大小是否大于 100
        if (listSize > 100) {
            int fromIndex = Math.max(listSize - 100, 0);
            // 使用 subList 方法截取输入列表中的最后 10 个元素
            newMessages = value.subList(fromIndex, listSize);
        }
        // 将截取后的列表（或原始列表，如果其大小不超过 100）与给定的键一起存储到缓存中
        chats.put(key, newMessages);
    }

    /**
     * 根据键（key）从缓存中获取聊天消息列表。
     *
     * @param key
     * @return
     */
    public static List<ChatMessage> get(String key) {
        List<ChatMessage> messages = chats.get(key);
        if (Validator.isEmpty(messages)) {
            return new ArrayList<>();
        } else {
            return messages;
        }
    }
    
    /**
     * 根据键（key）和所需最大字符数量从缓存中动态获取聊天消息列表长度
     *
     * @param key
     * @param maxChar
     */
    public static List<ChatMessage> getCacheListByMaxChar(String key, Integer maxChar) {
        List<ChatMessage> chatMessages = chats.get(key);
        if (ObjectUtil.isNull(chatMessages)) {
            // 新建对象需要再写入缓存覆盖原对象（否则对象地址不同，不能持久化）
            chats.put(key, new ArrayList<>());
            return chats.get(key);
        }
        
        // 当前累计字符数量
        int charNum = 0;
        int size = chatMessages.size();
        for (int i = size - 1; i >= 0; i--) {
            charNum += chatMessages.get(i).getContent().length();
            if (charNum >= maxChar) {
                // 深克隆对象需要再写入缓存覆盖原对象（否则对象地址不同，不能持久化）
                List<ChatMessage> cutChatMessageList = chatMessages.subList(i + 1, size);
                cutChatMessageList.add(0, chatMessages.get(0));
                chats.put(key, cutChatMessageList);
                return chats.get(key);
            }
        }
        
        // 全列字符数不满足MaxChar
        return chatMessages;
    }
    
    /**
     * 根据键（key）从缓存中动态获取聊天消息列表长度，拼接上提示消息
     *
     * @param key
     * @return
     */
    public static List<ChatMessage> getCacheListByMaxChar(String key) {
        ChatMessage promptChatMessage = promptMap.get(key);
        if (ObjectUtil.isEmpty(promptChatMessage)) {
            return getCacheListByMaxChar(key, MAX_CHAR);
        }
        
        int length = promptChatMessage.getContent().length();
        List<ChatMessage> cacheListByMaxChar = getCacheListByMaxChar(key, MAX_CHAR - length);
        // 空则插入
        if (ObjectUtil.isEmpty(cacheListByMaxChar)) {
            cacheListByMaxChar.add(0, promptChatMessage);
        }
        // 无则清除旧语境并插入
        else if (!ChatRoleConst.SYSTEM.equals(cacheListByMaxChar.get(0).getRole())) {
            cacheListByMaxChar.clear();
            cacheListByMaxChar.add(0, promptChatMessage);
        }
        // 有则判断更新
        else if (!promptChatMessage.getContent().equals(cacheListByMaxChar.get(0).getContent())) {
            cacheListByMaxChar.set(0, promptChatMessage);
        }
        return cacheListByMaxChar;
    }
    
}
package com.cym.chat.utils;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.lang.Validator;
import com.cym.chat.params.chat.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 携带信息。聊天消息缓存实现，用于存储和获取聊天消息。它使用了一个基于先进先出 (FIFO) 策略的本地缓存，缓存最大容量为 100。
 */
public class ChatCacheUtil {
    public static final Cache<String, List<ChatMessage>> chats = CacheUtil.newFIFOCache(100);

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
}
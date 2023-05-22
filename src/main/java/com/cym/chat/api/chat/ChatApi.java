package com.cym.chat.api.chat;

import com.cym.chat.params.chat.ChatParams;
import com.cym.chat.params.chat.ChatResult;
import com.dtflys.forest.annotation.*;
import com.dtflys.forest.http.ForestResponse;

import java.util.Map;

/**
 * @author Yummy
 * 聊天相关API
 */

public interface ChatApi {
    // 新建聊天接口
    @Post("#{openai.chat.host}/v1/chat/completions")
    ForestResponse<ChatResult> ChatCompletions(@Header Map<String, Object> headers, @JSONBody ChatParams params);

    @Post("#{openai.chat.host}/v1/chat/completions")
    String ChatCompletionsStr(@Header Map<String, Object> headers, @JSONBody ChatParams params);
}
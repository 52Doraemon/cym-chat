package com.cym.chat.service;

import com.cym.chat.params.chat.ChatParams;
import com.cym.chat.params.chat.ChatResultStream;
import reactor.core.publisher.Flux;

public interface ChatApiService {
    Flux<ChatResultStream> doChat(ChatParams params);
}

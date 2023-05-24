package com.cym.chat.service;

import com.cym.chat.common.R;

import java.util.List;

/**
 * 秘钥工具业务层接口
 *
 * @author XuChenghe
 * @date 2023/5/24 14:36
 */
public interface ChatGptService {
    
    /**
     * 获取批量key的详细信息(需要翻墙)
     *
     * @param keyList
     * @return
     */
    R getDetails(List<String> keyList);
    
}

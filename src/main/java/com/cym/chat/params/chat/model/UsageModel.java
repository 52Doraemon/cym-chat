package com.cym.chat.params.chat.model;

import lombok.Data;

/**
 * "prompt_tokens": 30,
 * 		"completion_tokens": 282,
 * 		"total_tokens": 312
 */
@Data
public class UsageModel {
    private int prompt_tokens;
    private int completion_tokens;
    private int total_tokens;
}

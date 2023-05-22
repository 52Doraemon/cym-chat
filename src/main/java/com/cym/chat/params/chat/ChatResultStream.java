package com.cym.chat.params.chat;

import com.cym.chat.params.chat.model.ChoiceStreamModel;
import com.cym.chat.params.chat.model.UsageModel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "聊天返回结果")
public class ChatResultStream {
    @ApiModelProperty("chat-id")
    private String id;

    @ApiModelProperty("调用对象")
    private String object;

    @ApiModelProperty("创建ID")
    private Long created;

    @ApiModelProperty("使用模型")
    private String model;

    @ApiModelProperty("token消耗")
    private UsageModel usage;

    // 可选结果集合
    @ApiModelProperty("可选结果集合")
    List<ChoiceStreamModel> choices;
}
package com.cym.chat.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(value = "编辑传参")
public class EditDTO {
    @NotBlank(message = "编辑内容不可为空")
    @ApiModelProperty("编辑内容")
    String content;

    @ApiModelProperty("提示")
    String tips;
}

package com.cym.chat.controller;

import com.cym.chat.common.R;
import com.cym.chat.service.ChatGptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 秘钥工具接口层实现类
 *
 * @author XuChenghe
 * @date 2023/5/24 14:31
 */
@RestController
@CrossOrigin
@RequestMapping("/keyUtil")
@Api(value = "秘钥工具接口", tags = "OpenAI")
public class ChatGptController {
    
    @Autowired
    private ChatGptService chatGptService;
    
    /**
     * 获取批量key的详细信息
     *
     * @param keyList
     * @return
     */
    @GetMapping("/details")
    @ApiOperation("获取批量key的详细信息")
    @ApiImplicitParam(dataType = "list", name = "keyList", value = "秘钥集合", required = true)
    public R getDetails(@RequestParam("keyList") List<String> keyList) {
        return chatGptService.getDetails(keyList);
    }
    
}

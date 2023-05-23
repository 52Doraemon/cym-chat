package com.cym.chat.controller;

import cn.hutool.core.util.IdUtil;
import com.cym.chat.params.web.AssistantParams;
import com.cym.chat.params.web.WebChatParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import springfox.documentation.annotations.ApiIgnore;

@Slf4j
@Controller
@RequestMapping("chat")
public class WebChatController {

    @ApiIgnore
    @GetMapping("web")

    @ApiOperation(value = "在线聊天室", notes = "")
    public ModelAndView webChatRoom() {
        //创建一个ModelAndView对象。这个对象用于将数据与视图关联起来
        ModelAndView modelAndView = new ModelAndView();
        // 构建助理详细信息
        AssistantParams params = new AssistantParams();
        params.setName("智慧小助手");
        params.setAvatar("https://s1.ax1x.com/2023/03/20/ppNV4Fs.png");
        params.setIntro("随时为您解答");
        // 构建聊天初始化信息
        // 创建一个WebChatParams对象，并设置聊天ID和初始内容。
        // 聊天ID是一个随机生成的UUID，用于唯一标识此次聊天。初始内容是助手向用户发送的第一条消息。.
        WebChatParams initParams = new WebChatParams();
        initParams.setChatId(IdUtil.fastSimpleUUID());
        initParams.setInitContent("芜湖小子再次来袭[dog]，快来开启我们的聊天之旅吧，我将会与你无所不谈~");
        modelAndView.setViewName("chat");
        modelAndView.addObject("assistant", params);
        modelAndView.addObject("init", initParams);
        //方法创建并返回一个ModelAndView对象，用于显示一个包含助手详细信息和初始化聊天信息的聊天页面。
        //页面将使用src/main/resources/templates/chat.html模板呈现。
        return modelAndView;
    }

}

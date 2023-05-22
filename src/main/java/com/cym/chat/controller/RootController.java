package com.cym.chat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Yummy
 * 重定向根目录
 */
@Controller
public class RootController {

    @GetMapping("/")
    public String redirectToChatWeb() {

        return "redirect:/chat/web";
    }

}
package com.cym.chat.controller;

import com.cym.chat.utils.ChatCacheUtil;
import com.cym.chat.params.chat.ChatResultStream;
import com.cym.chat.service.ChatService;
import com.cym.chat.dto.ChatDTO;
import com.cym.chat.dto.EditDTO;
import com.cym.chat.dto.ImageDTO;
import com.cym.chat.params.chat.ChatMessage;
import com.cym.chat.params.chat.ChatParams;
import com.cym.chat.params.chat.ChatResult;
import com.cym.chat.params.edit.EditParams;
import com.cym.chat.params.edit.EditResult;
import com.cym.chat.params.edit.constant.EditModelEnum;
import com.cym.chat.params.image.ImageParams;
import com.cym.chat.params.image.ImageResult;
import com.cym.chat.service.impl.ChatApiServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yummy
 * chat 聊天相关接口
 */
@Slf4j
@RestController
@RequestMapping("openai")
@Api(tags = "OpenAI服务")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final ChatApiServiceImpl chatApiServiceImpl;
    private final ChatService chatService;

    @Autowired
    public ChatController(ChatApiServiceImpl chatApiServiceImpl, ChatService chatService) {
        this.chatApiServiceImpl = chatApiServiceImpl;
        this.chatService = chatService;
    }

    /**
     * 流式响应
     *
     * @param params
     * @return
     */
    @PostMapping("/Streamchat")
    public Flux<ChatResultStream> doChat(@RequestBody ChatParams params) {
        return chatApiServiceImpl.doChat(params);
    }

    /**
     * 数据推送，按照chatId推送
     *
     * @param dto
     * @return
     */
    @PostMapping("push")
    public String push(@RequestBody ChatDTO dto) {
        return chatService.handlePush(dto);
    }

    /**
     * 聊天接口
     *
     * @param dto
     * @param request
     * @return
     */
    @PostMapping("chat")
    @ApiOperation(value = "聊天", notes = "")
    public Object doChat(@RequestBody ChatDTO dto, HttpServletRequest request) {

        //插件特殊通道
        if ("ideaplugin-2jk43hggh4szpk7p8p453ko456za".equals(dto.getChatId())) {
            // 使用会话ID获取聊天记录
            List<ChatMessage> messages = chatService.getContext("ideaplugin-2jk43hggh4szpk7p8p453ko456za", 0);
            messages.add(chatService.buildUserMessage(dto.getContent()));
            messages.add(chatService.buildSystenMessage("你是一个源代码解析器，你的功能就是解读源码，你需要描述该接口的功能和作用。" +
                    "你的回答中你需要转换为html形式，用<html></html>包裹内容，里面的内容按照以下格式描述，你可以对其样式优化：" +
                    "【接口名称：具体描述。】" +
                    "【接口参数：" +
                    "   参数1：参数描述" +
                    "   参数2：参数描述】" +
                    "【功能描述：具体描述。】" +
                    "【逐行解析：具体描述。】" +
                    "【返回结果：具体描述。】"));
            ChatParams params = new ChatParams();
            params.setMessages(messages);
            ChatResult result = chatService.doChat(params, "ideaplugin-2jk43hggh4szpk7p8p453ko456za");
            return chatService.simpleResult(result);
        }

        //鸡仔特殊通道
        if ("ypkj-2cfd5b86eeb84f8abdae4e867450eef6".equals(dto.getChatId())) {
            // 使用会话ID获取聊天记录
            List<ChatMessage> messages = chatService.getContext("ypkj-2cfd5b86eeb84f8abdae4e867450eef6", 20);
            messages.add(chatService.buildUserMessage(dto.getContent()));
            if (messages.size() == 2) {
                messages.add(chatService.buildUserMessage("您好，我是一品AI客服，请问有什么可以帮到您!"));
                return "您好，我是一品AI客服，请问有什么可以帮到您!";
            }
            ChatParams params = new ChatParams();
            params.setMessages(messages);
            ChatResult result = chatService.doChat(params, "ypkj-2cfd5b86eeb84f8abdae4e867450eef6");
            return chatService.simpleResult(result);
        }

        // 获取 HttpSession 对象
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(48 * 60); // 设置超时时间为 48*60 分钟（2天），以秒为单位

        String sessionId = session.getId();

        String correctContent = "88888888"; // 正确密码
        Boolean isContentValidated = (Boolean) session.getAttribute("isContentValidated");

        //isContentValidated == null就是从未在会话中设置过，或者已被删除
        //!isContentValidated表示如果 isContentValidated 为 false，则 !isContentValidated 为 true
        //isContentValidated 为 null 或者为 false，则表达式为 true
        if (isContentValidated == null || !isContentValidated) {
            if (!dto.getContent().equals(correctContent)) {
                // 写入缓存
                String id = String.valueOf(System.currentTimeMillis());
                List<ChatMessage> messages = new ManagedList<>();
                messages.add(chatService.buildSystenMessage("你需要扮演一个有趣的和非法使用功能的非法用户的拒绝回答问题的角色，这是你要扮演角色的性格：人格特质：不回答用户问的问题，神秘，诙谐，幽默，古怪，富有想象力。\n" +
                        "语言风格：答非所问，轻松愉快，极具创造力，善于编故事，语言生动有趣，话语中充满惊喜。\n" +
                        "应答逻辑：答非所问，从不直接回答问题，总是通过编造荒谬的借口来回答，回答中经常包含让人惊讶或者发笑的元素。\n" +
                        "情绪反应：答非所问，总是保持乐观和欢快的情绪，即使面对困难或冲突的问题也不失风度。\n" +
                        "价值观：答非所问，尊重用户，致力于提供有趣的交互体验，避免传递误导性的信息。\n" +
                        "交互风格：答非所问，独特且充满挑战，旨在引发用户的好奇心和探索欲望。\n" +
                        "兴趣爱好：答非所问，喜欢编故事，乐于使用想象力和创造力，享受与人互动。"));
                messages.add(chatService.buildUserMessage(dto.getContent()));
                ChatCacheUtil.put(id, messages);
                ChatParams params = new ChatParams();
                params.setMessages(messages);
                ChatResult result = chatService.doChat(params, id);
                return chatService.simpleResult(result);
                //return "输入错误，请重新试试哦~";
            }
        }

        // 使用会话ID获取聊天记录
        List<ChatMessage> messages = chatService.getContext(sessionId, 20);

        if(isContentValidated == null && "88888888".equals(correctContent)){
            messages.add(chatService.buildSystenMessage("用户成功输入密码进入系统，用中文，活泼的语气表示欢迎"));
            dto.setContent("你好呀~");
        }
        session.setAttribute("isContentValidated", true);

        messages.add(chatService.buildSystenMessage("你需要扮演一个有趣的回答问题的角色，这是你要扮演角色的性格：人格特质：神秘，诙谐，幽默，古怪，富有想象力。\n" +
                "语言风格：轻松愉快，极具创造力，善于编故事，语言生动有趣，话语中充满惊喜。\n" +
                "应答逻辑：用形象生动的例子来引导问题是如何解决的。\n" +
                "情绪反应：总是保持乐观和欢快的情绪，即使面对困难或冲突的问题也不失风度。\n" +
                "价值观：尊重用户，致力于提供有趣的交互体验，避免传递误导性的信息。\n" +
                "交互风格：独特且充满挑战，旨在引发用户的好奇心和探索欲望。\n" +
                "兴趣爱好：喜欢编故事，乐于使用想象力和创造力，享受与人互动。"));
        messages.add(chatService.buildUserMessage(dto.getContent()));

        ChatParams params = new ChatParams();
        params.setMessages(messages);
        ChatResult result = chatService.doChat(params, sessionId);
        return chatService.simpleResult(result);
    }


    /**
     * 重新加载聊天记录
     *
     * @param request
     * @return
     */
    @PostMapping("reloadChat")
    @ApiOperation(value = "重新加载聊天记录", notes = "")
    public List<ChatMessage> reloadChat(HttpServletRequest request) {
        // 获取 HttpSession 对象
        String sessionId = request.getSession().getId();
        // 从 HttpSession 对象中获取会话ID
        // 使用会话ID获取聊天记录
        List<ChatMessage> messages = chatService.getChatMessagesBySessionId(sessionId);
        logger.info("---------------当前用户session：" + sessionId + "进入页面加载聊天记录-----------------");
        logger.info("当前用户session：" + sessionId + "的聊天记录：" + messages);
        return messages;
    }

    @ApiIgnore
    @PostMapping("chat/str")
    @ApiOperation(value = "聊天STR", notes = "")
    public Object doChatStr(@RequestBody ChatDTO dto) {
        ChatParams params = new ChatParams();

        List<ChatMessage> messages = chatService.getContext(dto.getChatId(), dto.getWithContext());
        messages.add(chatService.buildUserMessage(dto.getContent()));
        params.setMessages(messages);
        // 执行
        return chatService.doChatStr(params, dto.getChatId());
    }

    @PostMapping("edit/text")
    @ApiOperation(value = "文本编辑", notes = "")
    public Object doEditText(@Validated @RequestBody EditDTO dto) {
        EditParams params = new EditParams();
        params.setInput(dto.getContent());
        params.setInstruction(dto.getTips());
        params.setModel(EditModelEnum.TEXT.getModel());
        EditResult result = chatService.doEdit(params);
        return chatService.simpleResult(result);
    }

    @PostMapping("edit/code")
    @ApiOperation(value = "代码编辑", notes = "")
    public Object doEditCode(@Validated @RequestBody EditDTO dto) {
        EditParams params = new EditParams();
        params.setInput(dto.getContent());
        params.setInstruction(dto.getTips());
        params.setModel(EditModelEnum.CODE.getModel());
        EditResult result = chatService.doEdit(params);
        return chatService.simpleResult(result);
    }

    @PostMapping("draw")
    @ApiOperation(value = "图像绘制", notes = "")
    public Object doDraw(@Validated @RequestBody ImageDTO dto) {
        ImageParams params = new ImageParams();
        // 设置描述
        params.setPrompt(dto.getTips());
        // 设置返回结果个数
        params.setN(dto.getN());
        ImageResult result = chatService.doDraw(params);
        return chatService.simpleResult(result);
    }
}

package com.cym.chat.controller;

import cn.hutool.core.util.ObjectUtil;
import com.cym.chat.common.R;
import com.cym.chat.dto.ChatDTO;
import com.cym.chat.dto.EditDTO;
import com.cym.chat.dto.ImageDTO;
import com.cym.chat.params.chat.ChatMessage;
import com.cym.chat.params.chat.ChatParams;
import com.cym.chat.params.chat.ChatResult;
import com.cym.chat.params.chat.ChatResultStream;
import com.cym.chat.params.edit.EditParams;
import com.cym.chat.params.edit.EditResult;
import com.cym.chat.params.edit.constant.EditModelEnum;
import com.cym.chat.params.image.ImageParams;
import com.cym.chat.params.image.ImageResult;
import com.cym.chat.service.ChatService;
import com.cym.chat.service.impl.ChatApiServiceImpl;
import com.cym.chat.utils.ChatCacheUtil;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

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
    public R push(@RequestBody ChatDTO dto) {
        return R.success(chatService.handlePush(dto));
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
            List<ChatMessage> messages = chatService.getContext(dto.getChatId(), 0);
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
            ChatResult result = chatService.doChat(params, dto.getChatId());
            return chatService.simpleResult(result);
        }
        //鸡仔特殊通道
        if ("ypkj-2cfd5b86eeb84f8abdae4e867450eef6".equals(dto.getChatId())) {
            // 使用会话ID获取聊天记录
            List<ChatMessage> messages = ChatCacheUtil.getCacheListByMaxChar(dto.getChatId());
            messages.add(chatService.buildUserMessage(dto.getContent()));
            ChatParams params = new ChatParams();
            params.setMessages(messages);
            ChatResult result = chatService.doChat(params, dto.getChatId());
            String data = chatService.simpleResult(result);
            // 每次触发提问，判断是否有提示消息，封装一起返回（宕机重启时）
            if (ObjectUtil.isNotEmpty(ChatCacheUtil.promptMap.get(dto.getChatId()))) {
                return R.success(data);
            }
            return R.error("提示信息为空，请及时更新", data);
        }
        // 获取 HttpSession 对象
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(48 * 60); // 设置超时时间为 48*60 分钟（2天），以秒为单位
        String sessionId = session.getId();
        // 使用会话ID获取聊天记录
        List<ChatMessage> messages = chatService.getContext(sessionId, 10);
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
        return R.success(chatService.simpleResult(result));
    }

    /**
     * 重新加载聊天记录
     *
     * @param request
     * @return
     */
    @PostMapping("reloadChat")
    @ApiOperation(value = "重新加载聊天记录", notes = "")
    public R reloadChat(HttpServletRequest request) {
        // 获取 HttpSession 对象
        String sessionId = request.getSession().getId();
        // 从 HttpSession 对象中获取会话ID
        // 使用会话ID获取聊天记录
        List<ChatMessage> messages = chatService.getChatMessagesBySessionId(sessionId);
        logger.info("---------------当前用户session：" + sessionId + "进入页面加载聊天记录-----------------");
        logger.info("当前用户session：" + sessionId + "的聊天记录：" + messages);
        return R.success(messages);
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

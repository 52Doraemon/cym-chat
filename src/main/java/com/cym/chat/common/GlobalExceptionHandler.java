package com.cym.chat.common;

import com.dtflys.forest.exceptions.ForestRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 三次重发失败，接口限速，429
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseEntity<String> handleException(Exception e) {
        // 将错误信息记录到日志
        logger.error("Exception: {}", e.getMessage());
        e.printStackTrace();
        // 返回简单提示信息
        String simpleMessage = e.getMessage();
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(simpleMessage);
    }

    /**
     * 接口异常，已被封禁，401
     * @param ex
     * @return
     */
    @ExceptionHandler(ForestRuntimeException.class)
    public ResponseEntity<String> handleForestRuntimeException(ForestRuntimeException ex) {
        // 将错误信息记录到日志
        logger.error("ForestRuntimeException: {}", ex.getMessage());
        ex.printStackTrace();
        // 返回简单提示信息
        String simpleMessage = ex.getMessage();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(simpleMessage);
    }

    /**
     * 捕获空指针异常，返回 500 错误
     * @param ex
     * @return
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<String> handleNullPointerException(NullPointerException ex) {
        // 将错误信息记录到日志
        logger.error("NullPointerException: {}", ex.getMessage());
        ex.printStackTrace();
        // 返回简单提示信息
        String simpleMessage = ex.getMessage() + "，发生空指针异常，请检查您的输入。";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(simpleMessage);
    }

    /**
     * 捕获没有 Key的异常，返回 500 错误
     * @param e
     * @return
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException e) {
        // 在这里进行异常处理逻辑
        e.printStackTrace();
        String errorMessage = "服务器已经没有可用的Key！请联系管理员~";
        log.warn(errorMessage);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
    }

    /**
     * 捕获没有 Key的异常，返回 400 错误
     * @param e
     * @return
     */
    @ExceptionHandler(IllegalCallerException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalCallerException e) {
        // 在这里进行异常处理逻辑
        e.printStackTrace();
        String errorMessage = "单次输入字符串过多，请重新输入！";
        log.warn(errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
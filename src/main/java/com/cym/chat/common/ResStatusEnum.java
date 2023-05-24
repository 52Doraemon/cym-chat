package com.cym.chat.common;

/**
 * 结果状态枚举类
 *
 * @author XuChenghe
 * @date 2023/5/23 8:12
 */
public enum ResStatusEnum {
    
    SUCCESS(1),
    ERROR(0),
    
    /**
     * 代理网站成功响应码
     */
    PROXY_SUCCESS(200);
    
    private final Integer code;
    
    ResStatusEnum(Integer code) {
        this.code = code;
    }
    
    public Integer getCode() {
        return code;
    }
    
}

package com.cym.chat.common;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用返回结果，服务端响应的数据最终都会封装成此对象
 * 这个的目的是用ajax返回的时候可以返回更多的数据封装到一个对象里头
 * @param <T>
 */
@Data
public class R<T> implements Serializable {

    private Integer code; //编码：1成功，0和其它数字为失败

    private String msg; //错误信息

    private T data; //数据

    private Map map = new HashMap(); //动态数据

    public static <T> R<T> success(T object) {
        R<T> r = new R<T>();
        r.data = object;
        r.msg = "";
        r.code = ResStatusEnum.SUCCESS.getCode();
        return r;
    }

    public static <T> R<T> error(String msg) {
        R r = new R();
        r.msg = msg;
        r.data = "";
        r.code = ResStatusEnum.ERROR.getCode();
        return r;
    }
    
    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }
    
    public static <T> R<T> error(String msg, T object) {
        R r = new R();
        r.msg = msg;
        r.code = ResStatusEnum.ERROR.getCode();
        r.data = object;
        return r;
    }
    
}

package com.cym.chat.api.image;

import com.cym.chat.params.image.ImageParams;
import com.cym.chat.params.image.ImageResult;
import com.dtflys.forest.annotation.Header;
import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.http.ForestResponse;

import java.util.Map;

/**
 * @author Yummy
 * 图像生成相关API
 */
public interface ImageApi {
    // 图像生成接口
    @Post("#{openai.chat.host}/v1/images/generations")
    ForestResponse<ImageResult> ChatDraw(@Header Map<String, Object> headers, @JSONBody ImageParams params);
}

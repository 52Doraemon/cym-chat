package com.cym.chat.api.edit;

import com.cym.chat.params.edit.EditParams;
import com.dtflys.forest.annotation.Header;
import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.http.ForestResponse;
import com.cym.chat.params.edit.EditResult;

import java.util.Map;

/**
 * @author Yummy
 * 文本或者代码编辑相关API
 */
public interface EditApi {
    // 文本或者代码编辑接口
    @Post("#{openai.chat.host}/v1/edits")
    ForestResponse<EditResult> ChatEdits(@Header Map<String, Object> headers, @JSONBody EditParams params);
}

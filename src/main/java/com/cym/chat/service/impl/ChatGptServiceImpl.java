package com.cym.chat.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cym.chat.common.R;
import com.cym.chat.common.ResStatusEnum;
import com.cym.chat.service.ChatGptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 秘钥工具业务层实现类
 *
 * @author XuChenghe
 * @date 2023/5/24 14:37
 */
@Service
public class ChatGptServiceImpl implements ChatGptService {
    
    /**
     * Spring配置文件的解析类
     */
    @Autowired
    private Environment environment;
    
    /**
     * 日志记录对象
     */
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
    /**
     * 请求对象
     */
    private RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 日期格式 FIXME: 可以放到全局工具类中，作为Spring的单例Bean
     */
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    /**
     * {@inheritDoc}
     */
    @Override
    public R getDetails(List<String> keyList) {
        // 结果集
        Map<String, Map<String, Object>> resMap = new HashMap<>();
        
        // 获取查询接口地址
        String subscriptionUrl = environment.getProperty("openAiProxy_XM.subscriptionUrl");
        String usageUrl = environment.getProperty("openAiProxy_XM.usageUrl");
        
        // 循环执行查询
        for (String key : keyList) {
            // 结果
            Map<String, Object> res = new HashMap<>();
            
            // 设置请求头
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Authorization", "Bearer " + key);
            HttpEntity httpEntity = new HttpEntity(httpHeaders);
            try {
                // 查询总额度接口
                ResponseEntity<String> subscriptionEntity = restTemplate.exchange(subscriptionUrl, HttpMethod.GET, httpEntity, String.class);
                // 成功响应
                if (subscriptionEntity.getStatusCodeValue() == ResStatusEnum.PROXY_SUCCESS.getCode()) {
                    // 解析结果回填
                    String body = subscriptionEntity.getBody();
                    JSONObject jsonObject = JSON.parseObject(body);
                    res.put("名称：", jsonObject.get("account_name"));
                    res.put("截止：", dateTimeFormat.format(new Date(Long.parseLong(jsonObject.get("access_until") + "000"))));
                    res.put("总量：", jsonObject.get("hard_limit_usd"));
                }
                // FIXME: 失败响应
                else {
                }
            } catch (Exception e) {
                // LOGGER
                e.printStackTrace();
                res.put("处理结果", "查询总额度请求异常！");
                resMap.put(key, res);
                continue;
            }
            
            try {
                // 设置请求参数
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MONTH, -3);
                String start_date = dateFormat.format(calendar.getTime());
                // 恢复当前时间
                calendar.setTime(new Date());
                calendar.add(Calendar.DATE, 1);
                String end_date = dateFormat.format(calendar.getTime());
                String urlParams = "?start_date=" + start_date + "&end_date=" + end_date;
                
                // 查询三个月内已用额度接口
                ResponseEntity<String> usageEntity = restTemplate.exchange(usageUrl + urlParams, HttpMethod.GET, httpEntity, String.class);
                // 成功响应
                if (usageEntity.getStatusCodeValue() == ResStatusEnum.PROXY_SUCCESS.getCode()) {
                    // 解析结果回填
                    String body = usageEntity.getBody();
                    JSONObject jsonObject = JSON.parseObject(body);
                    // 浮点数使用BigDecimal处理
                    BigDecimal usage = new BigDecimal(jsonObject.get("total_usage").toString()).divide(new BigDecimal(100));
                    BigDecimal balance = new BigDecimal(res.get("总量：").toString()).subtract(usage);
                    res.put("已用：", usage.doubleValue());
                    res.put("剩余：", balance.doubleValue());
                }
                // FIXME: 失败响应
                else {
                }
            } catch (RestClientException e) {
                // LOGGER
                e.printStackTrace();
                res.put("处理结果", " 查询三个月内已用额度请求异常！");
                resMap.put(key, res);
                continue;
            }
            
            resMap.put(key, res);
        }
        
        // 约定格式处理返回
        return R.success(resMap);
    }
    
}

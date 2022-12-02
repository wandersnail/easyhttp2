package cn.wandersnail.http;

import cn.wandersnail.http.converter.JsonParserType;

/**
 * date: 2021/10/29 13:21
 * author: zengfansheng
 */
public class GlobalConfiguration extends BaseConfiguration {
    /**
     * JSON解析器类型
     */
    public JsonParserType jsonParserType = JsonParserType.GSON;
}

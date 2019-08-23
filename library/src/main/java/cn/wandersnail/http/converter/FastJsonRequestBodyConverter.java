package cn.wandersnail.http.converter;

import com.alibaba.fastjson.JSON;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

/**
 * 使用阿里巴巴的FastJson转换请求体
 * 
 * date: 2019/8/23 14:59
 * author: zengfansheng
 */
public class FastJsonRequestBodyConverter implements Converter<Object, RequestBody> {

    @Override
    public RequestBody convert(Object value) throws IOException {
        MediaType mediaType = MediaType.parse("application/json; charset=UTF-8");
        return RequestBody.create(mediaType, JSON.toJSONBytes(value));
    }
}

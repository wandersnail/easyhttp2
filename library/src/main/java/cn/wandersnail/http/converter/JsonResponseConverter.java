package cn.wandersnail.http.converter;

import com.alibaba.fastjson.JSON;

import cn.wandersnail.http.exception.ConvertException;
import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * 响应体为json字符串
 * 
 * date: 2019/8/23 15:13
 * author: zengfansheng
 */
public class JsonResponseConverter<T> implements Converter<ResponseBody, T> {
    private final Class<T> cls;

    public JsonResponseConverter(Class<T> cls) {
        this.cls = cls;
    }

    @Override
    public T convert(ResponseBody value) throws ConvertException {
        if (value == null) {
            throw new ConvertException("ResponseBody is null");
        }
        try {
            return JSON.parseObject(value.string(), cls);
        } catch (Throwable e) {
            throw new ConvertException(e.getMessage(), e);
        }
    }
}

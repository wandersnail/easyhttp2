package cn.wandersnail.http.converter;

import java.io.IOException;

import cn.wandersnail.http.exception.ConvertException;
import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * 响应体为字符串
 * 
 * date: 2019/8/23 15:18
 * author: zengfansheng
 */
public class StringResponseConverter implements Converter<ResponseBody, String> {
    @Override
    public String convert(ResponseBody value) throws IOException {
        if (value == null) {
            throw new ConvertException("ResponseBody is null");
        } else {
            return value.string();
        }
    }
}

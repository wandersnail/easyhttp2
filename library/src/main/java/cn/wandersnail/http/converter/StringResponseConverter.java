package cn.wandersnail.http.converter;

import androidx.annotation.NonNull;

import java.io.IOException;

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
    public String convert(@NonNull ResponseBody value) throws IOException {
        return value.string();
    }
}

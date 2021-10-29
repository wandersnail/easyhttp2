package cn.wandersnail.http.converter;

import androidx.annotation.NonNull;

import java.io.IOException;

import cn.wandersnail.http.EasyHttp;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

/**
 * 使用阿里巴巴的FastJson转换请求体
 * 
 * date: 2019/8/23 14:59
 * author: zengfansheng
 */
public class GsonRequestBodyConverter implements Converter<Object, RequestBody> {

    @Override
    public RequestBody convert(@NonNull Object value) throws IOException {
        MediaType mediaType = MediaType.parse("application/json; charset=UTF-8");
        return RequestBody.create(mediaType, EasyHttp.getInstance().getGson().toJson(value).getBytes());
    }
}

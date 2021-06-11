package cn.wandersnail.http.converter;

import java.io.IOException;
import java.lang.reflect.Type;

import cn.wandersnail.http.EasyHttp;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Okio;
import retrofit2.Converter;

/**
 * 使用阿里巴巴的FastJson转换响应体
 * <p>
 * date: 2019/8/23 15:04
 * author: zengfansheng
 */
public class GsonResponseBodyConverter implements Converter<ResponseBody, Object> {
    private final Type type;

    public GsonResponseBodyConverter(Type type) {
        this.type = type;
    }

    @Override
    public Object convert(ResponseBody value) throws IOException {
        BufferedSource bufferedSource = Okio.buffer(value.source());
        String tempStr = bufferedSource.readUtf8();
        bufferedSource.close();
        return EasyHttp.getGson().fromJson(tempStr, type);
    }
}

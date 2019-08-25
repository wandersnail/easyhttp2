package cn.wandersnail.http.converter;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * date: 2019/8/25 14:19
 * author: zengfansheng
 */
public class NothingConverter implements Converter<ResponseBody, ResponseBody> {
    @Override
    public ResponseBody convert(ResponseBody value) {
        return value;
    }
}

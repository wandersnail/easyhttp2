package cn.wandersnail.http.factory;


import androidx.annotation.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import cn.wandersnail.http.converter.FastJson2ResponseBodyConverter;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * date: 2019/8/23 14:35
 * author: zengfansheng
 */
public class FastJson2ConverterFactory extends Converter.Factory {
    private FastJson2ConverterFactory() {
    }

    public static FastJson2ConverterFactory create() {
        return new FastJson2ConverterFactory();
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(@NonNull Type type, @NonNull Annotation[] annotations, @NonNull Retrofit retrofit) {
        return new FastJson2ResponseBodyConverter(type);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(@NonNull Type type, @NonNull Annotation[] parameterAnnotations, @NonNull Annotation[] methodAnnotations, @NonNull Retrofit retrofit) {
        return super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
    }
}

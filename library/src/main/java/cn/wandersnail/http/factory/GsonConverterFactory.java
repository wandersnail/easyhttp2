package cn.wandersnail.http.factory;


import androidx.annotation.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import cn.wandersnail.http.converter.GsonResponseBodyConverter;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * date: 2019/8/23 14:35
 * author: zengfansheng
 */
public class GsonConverterFactory extends Converter.Factory {
    private GsonConverterFactory() {}
    
    public static GsonConverterFactory create() {
        return new GsonConverterFactory();
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(@NonNull Type type, @NonNull Annotation[] annotations, @NonNull Retrofit retrofit) {
        return new GsonResponseBodyConverter(type);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(@NonNull Type type, @NonNull Annotation[] parameterAnnotations, @NonNull Annotation[] methodAnnotations, @NonNull Retrofit retrofit) {
        return super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
    }
}

package cn.wandersnail.http.exception;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

/**
 * date: 2019/8/23 14:43
 * author: zengfansheng
 */
public class RetryWhenException implements Function<Observable<Throwable>, Observable<?>> {
    //重试次数
    private int count;
    //延时
    private int delay;
    //叠加延迟
    private int increaseDelay;

    public RetryWhenException() {
        this(3, 3000, 3000);
    }

    public RetryWhenException(int count) {
        this(count, 3000, 3000);
    }

    public RetryWhenException(int count, int delay) {
        this(count, delay, 3000);
    }

    public RetryWhenException(int count, int delay, int increaseDelay) {
        this.count = count;
        this.delay = delay;
        this.increaseDelay = increaseDelay;
    }

    private class Wrapper {
        Throwable throwable;
        int index;

        Wrapper(Throwable throwable, int index) {
            this.throwable = throwable;
            this.index = index;
        }
    }

    @Override
    public Observable<?> apply(Observable<Throwable> observable) throws Exception {
        return observable.zipWith(Observable.range(1, count + 1), new BiFunction<Throwable, Integer, RetryWhenException.Wrapper>() {
            @Override
            public Wrapper apply(Throwable throwable, Integer index) throws Exception {
                return new Wrapper(throwable, index);
            }
        }).flatMap(new Function<Wrapper, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Wrapper wrapper) throws Exception {
                if (wrapper.throwable instanceof ConnectException ||
                        wrapper.throwable instanceof SocketTimeoutException ||
                        wrapper.throwable instanceof TimeoutException) {
                    //如果超出重试次数也抛出错误，否则默认是会进入onCompleted
                    return Observable.timer(delay + (wrapper.index - 1) * increaseDelay, TimeUnit.MILLISECONDS);
                }
                return Observable.error(wrapper.throwable);
            }
        });
    }
}

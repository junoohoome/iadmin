package me.fjq.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 限流注解
 * @author jeff
 * <p>Date: 2019-12-16 10:31:00</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Limiter {

    // 限制次数
    double limit() default 5;

    //超时时长
    int timeout() default 1000;

    //超时时间单位
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}

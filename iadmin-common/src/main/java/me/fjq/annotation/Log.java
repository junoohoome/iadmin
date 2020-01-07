package me.fjq.annotation;


import java.lang.annotation.*;

/**
 * 自定义操作日志记录注解
 *
 * @author fjq
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /**
     * 模块
     */
    String title() default "";

    /**
     * 功能
     */
    String function() default "";

    /**
     * 操作人类别
     */
    String operator() default "";

    /**
     * 是否保存请求的参数
     */
    boolean isSaveRequestData() default true;
}

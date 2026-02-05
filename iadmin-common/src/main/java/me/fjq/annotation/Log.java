package me.fjq.annotation;


import java.lang.annotation.*;

/**
 * 自定义操作日志记录注解
 *
 * <p>用于标注需要记录操作日志的 Controller 方法
 *
 * @author fjq
 * @since 2025-02-05 (增强版)
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
    /**
     * 模块名称
     */
    String title() default "";

    /**
     * 功能名称
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

    /**
     * 是否保存响应结果
     */
    boolean isSaveResponseData() default false;

    /**
     * 是否保存请求参数到数据库
     * 为 true 时将请求参数序列化后存储到 request_param 字段
     */
    boolean saveRequestParamToDb() default true;

    /**
     * 是否保存响应结果到数据库
     * 为 true 时将响应结果序列化后存储到 response_result 字段
     */
    boolean saveResponseResultToDb() default false;

    /**
     * 业务类型（0其它 1新增 2修改 3删除 4授权 5导出 6导入）
     */
    int businessType() default 0;

}

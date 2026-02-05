package me.fjq.annotation;

import java.lang.annotation.*;

/**
 * 数据权限过滤注解
 *
 * <p>用于标注需要数据权限过滤的 Mapper 方法
 *
 * <p>使用示例：
 * <pre>
 * &#64;DataScope(deptAlias = "d", userAlias = "u")
 * List&lt;SysUser&gt; selectUserList(SysUserQuery query);
 * </pre>
 *
 * @author fjq
 * @since 2025-02-05
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 部门表别名，默认为 "d"
     * <p>用于 SQL 中关联部门表时的别名
     *
     * @return 部门表别名
     */
    String deptAlias() default "d";

    /**
     * 用户表别名，默认为 "u"
     * <p>用于 SQL 中关联用户表时的别名
     *
     * @return 用户表别名
     */
    String userAlias() default "u";

    /**
     * 是否启用数据权限过滤，默认为 true
     * <p>设置为 false 可以临时禁用数据权限过滤
     *
     * @return 是否启用
     */
    boolean enabled() default true;

}

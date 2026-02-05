package me.fjq.aspect;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import me.fjq.annotation.DataScope;
import me.fjq.constant.DataScopeConstants;
import me.fjq.enums.DataScopeType;
import me.fjq.security.JwtUserDetails;
import me.fjq.system.entity.SysRole;
import me.fjq.utils.SystemSecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据权限过滤切面
 *
 * <p>拦截标注了 @DataScope 注解的方法，根据当前用户的角色数据权限配置，
 * 自动构造 SQL 过滤条件并注入到查询参数中
 *
 * @author fjq
 * @since 2025-02-05
 */
@Slf4j
@Aspect
@Component
public class DataScopeAspect {

    /**
     * 全部数据权限
     */
    public static final String DATA_SCOPE_ALL = "1";

    /**
     * 自定义数据权限
     */
    public static final String DATA_SCOPE_CUSTOM = "2";

    /**
     * 本部门数据权限
     */
    public static final String DATA_SCOPE_DEPT = "3";

    /**
     * 本部门及以下数据权限
     */
    public static final String DATA_SCOPE_DEPT_AND_CHILD = "4";

    /**
     * 仅本人数据权限
     */
    public static final String DATA_SCOPE_SELF = "5";

    /**
     * 数据权限SQL存储
     * 使用 ThreadLocal 存储当前线程的数据权限SQL
     */
    private static final ThreadLocal<Map<String, String>> DATA_SCOPE_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 数据权限参数存储
     * 使用 ThreadLocal 存储当前线程的数据权限参数
     */
    private static final ThreadLocal<Map<String, Object>> DATA_SCOPE_PARAMS = new ThreadLocal<>();

    /**
     * 管理员角色标识
     */
    private static final String ROLE_ADMIN = "admin";

    /**
     * 前置通知：在方法执行前构造数据权限SQL
     *
     * @param point     切入点
     * @param dataScope 数据权限注解
     */
    @Before("@annotation(dataScope)")
    public void doBefore(JoinPoint point, DataScope dataScope) {
        // 清除 ThreadLocal
        clearThreadLocal();

        // 检查是否启用数据权限
        if (!dataScope.enabled()) {
            return;
        }

        try {
            // 获取当前用户
            JwtUserDetails currentUser = SystemSecurityUtils.getCurrentUser();
            if (currentUser == null) {
                log.warn("当前未登录用户，跳过数据权限过滤");
                return;
            }

            // 判断是否为管理员
            if (isAdmin(currentUser)) {
                log.debug("当前用户为管理员，跳过数据权限过滤");
                return;
            }

            // 获取用户角色列表
            List<SysRole> roles = currentUser.getRoles();
            if (CollectionUtil.isEmpty(roles)) {
                log.warn("当前用户没有角色，跳过数据权限过滤");
                return;
            }

            // 构造数据权限SQL
            String sql = constructDataScopeSql(roles, dataScope.deptAlias(), dataScope.userAlias(), currentUser);

            // 存储到 ThreadLocal
            if (StrUtil.isNotBlank(sql)) {
                Map<String, String> sqlMap = new ConcurrentHashMap<>();
                // 使用完整方法签名作为 key
                String key = point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName();
                sqlMap.put(DataScopeConstants.DATA_SCOPE_PREFIX + key, sql);
                DATA_SCOPE_THREAD_LOCAL.set(sqlMap);
                log.debug("数据权限SQL for {}: {}", key, sql);
            }

        } catch (Exception e) {
            log.error("数据权限过滤异常", e);
            clearThreadLocal();
        }
    }

    /**
     * 数据权限SQL示例（Java 21 文本块）
     *
     * <p>全部数据权限：无条件过滤
     * <p>自定义数据权限：
     * <pre>
     * SELECT * FROM sys_user u
     * WHERE u.dept_id IN (
     *     SELECT dept_id FROM sys_role_dept
     *     WHERE role_id = ?
     * )
     * </pre>
     *
     * <p>本部门数据权限：
     * <pre>
     * SELECT * FROM sys_user u
     * WHERE u.dept_id = ?
     * </pre>
     *
     * <p>本部门及以下数据权限：
     * <pre>
     * SELECT * FROM sys_user u
     * WHERE FIND_IN_SET(u.dept_id, ?)
     * </pre>
     *
     * <p>仅本人数据权限：
     * <pre>
     * SELECT * FROM sys_user u
     * WHERE u.user_id = ?
     * </pre>
     *
     * @param roles     用户角色列表
     * @param deptAlias 部门表别名
     * @param userAlias 用户表别名
     * @param user      当前用户
     * @return SQL条件
     */
    private String constructDataScopeSql(List<SysRole> roles, String deptAlias, String userAlias, JwtUserDetails user) {
        List<String> sqlList = new ArrayList<>();

        for (SysRole role : roles) {
            String dataScope = role.getDataScope();
            if (StrUtil.isBlank(dataScope)) {
                continue;
            }

            String sql = switch (DataScopeType.fromCode(dataScope)) {
                case ALL -> {
                    // 全部数据权限，不添加条件
                    yield "";
                }
                case CUSTOM -> {
                    // 自定义数据权限，需要查询 sys_role_dept 表
                    // 这里暂时返回空，需要在 Mapper 中实现
                    yield String.format("%s.dept_id IN (SELECT dept_id FROM sys_role_dept WHERE role_id = %d)",
                            deptAlias, role.getRoleId());
                }
                case DEPT -> {
                    // 本部门数据权限
                    if (user.getDeptId() != null) {
                        yield String.format("%s.dept_id = %d", deptAlias, user.getDeptId());
                    }
                    yield "";
                }
                case DEPT_AND_CHILD -> {
                    // 本部门及以下数据权限
                    if (StrUtil.isNotBlank(user.getAncestors())) {
                        String ancestors = user.getAncestors() + "," + user.getDeptId();
                        yield String.format("FIND_IN_SET(%s.dept_id, '%s')", deptAlias, ancestors);
                    }
                    yield "";
                }
                case SELF -> {
                    // 仅本人数据权限
                    yield String.format("%s.user_id = %d", userAlias, user.getId());
                }
            };

            if (StrUtil.isNotBlank(sql)) {
                sqlList.add(sql);
            }
        }

        // 合并多个角色的SQL条件（使用 OR 连接）
        if (CollectionUtil.isNotEmpty(sqlList)) {
            return "(" + String.join(" OR ", sqlList) + ")";
        }

        return "";
    }

    /**
     * 判断是否为管理员
     *
     * @param user 用户信息
     * @return 是否为管理员
     */
    private boolean isAdmin(JwtUserDetails user) {
        return user.getAuthorities().stream()
                .anyMatch(a -> ((SimpleGrantedAuthority) a).getAuthority().equals(ROLE_ADMIN));
    }

    /**
     * 获取数据权限SQL
     *
     * @param mappedStatementId MyBatis MappedStatement ID (格式: package.ClassName.methodName)
     * @return SQL条件
     */
    public static String getDataScopeSql(String mappedStatementId) {
        Map<String, String> sqlMap = DATA_SCOPE_THREAD_LOCAL.get();
        if (sqlMap == null) {
            return "";
        }
        // 尝试完整匹配
        String sql = sqlMap.get(DataScopeConstants.DATA_SCOPE_PREFIX + mappedStatementId);
        if (StrUtil.isNotBlank(sql)) {
            return sql;
        }
        // 尝试只使用方法名匹配
        String methodName = mappedStatementId.substring(mappedStatementId.lastIndexOf('.') + 1);
        return sqlMap.get(DataScopeConstants.DATA_SCOPE_PREFIX + methodName);
    }

    /**
     * 清除 ThreadLocal
     */
    public static void clearThreadLocal() {
        DATA_SCOPE_THREAD_LOCAL.remove();
        DATA_SCOPE_PARAMS.remove();
    }

}

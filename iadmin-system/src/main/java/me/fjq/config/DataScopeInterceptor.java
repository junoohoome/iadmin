package me.fjq.config;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import me.fjq.aspect.DataScopeAspect;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * MyBatis 数据权限拦截器
 *
 * <p>自动将数据权限 SQL 注入到查询语句中
 *
 * @author fjq
 * @since 2025-02-05
 */
@Slf4j
@Component
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class DataScopeInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        RowBounds rowBounds = (RowBounds) args[2];
        ResultHandler<?> resultHandler = (ResultHandler) args[3];

        // 获取当前方法的数据权限SQL
        String dataScopeSql = DataScopeAspect.getDataScopeSql(ms.getId());

        if (StrUtil.isNotBlank(dataScopeSql)) {
            BoundSql boundSql = ms.getBoundSql(parameter);
            String originalSql = boundSql.getSql();

            // 注入数据权限SQL
            String newSql = injectDataScopeSql(originalSql, dataScopeSql);

            log.debug("原始SQL: {}", originalSql);
            log.debug("数据权限SQL: {}", dataScopeSql);
            log.debug("注入后SQL: {}", newSql);

            // 创建新的 BoundSql
            BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), newSql, boundSql.getParameterMappings(), boundSql.getParameterObject());
            for (String key : boundSql.getAdditionalParameters().keySet()) {
                newBoundSql.setAdditionalParameter(key, boundSql.getAdditionalParameters().get(key));
            }

            // 创建新的 MappedStatement
            MappedStatement newMs = copyMappedStatement(ms, new BoundSqlSource(newBoundSql));
            args[0] = newMs;
        }

        return invocation.proceed();
    }

    /**
     * 注入数据权限SQL
     *
     * @param originalSql 原始SQL
     * @param dataScopeSql 数据权限SQL
     * @return 注入后的SQL
     */
    private String injectDataScopeSql(String originalSql, String dataScopeSql) {
        String sql = originalSql.toLowerCase();

        // 查找 WHERE 子句位置
        int whereIndex = sql.indexOf(" where ");
        int andIndex = sql.indexOf(" and ");

        if (whereIndex == -1) {
            // 没有 WHERE 子句，直接添加
            return originalSql + " WHERE " + dataScopeSql;
        }

        // 找到最后一个 WHERE 或 AND 的位置
        int insertPos = andIndex > whereIndex ? andIndex : whereIndex;

        // 插入 AND 条件
        String prefix = originalSql.substring(0, insertPos);
        String suffix = originalSql.substring(insertPos);

        // 判断需要添加 AND 还是直接添加
        if (insertPos == whereIndex) {
            // WHERE 后面没有其他条件，直接添加
            return prefix + " WHERE " + dataScopeSql + " AND " + suffix.substring(6);
        } else {
            // 已有 AND 条件，添加新的 AND
            return prefix + " AND " + dataScopeSql + " " + suffix;
        }
    }

    /**
     * 复制 MappedStatement
     */
    private MappedStatement copyMappedStatement(MappedStatement ms, BoundSqlSource boundSqlSource) {
        return ms.getConfiguration().getMappedStatement(ms.getId());
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // Do nothing
    }

    /**
     * 自定义 BoundSqlSource
     */
    private static class BoundSqlSource implements org.apache.ibatis.mapping.SqlSource {
        private final BoundSql boundSql;

        public BoundSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }

}

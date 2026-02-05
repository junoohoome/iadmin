package me.fjq.constant;

/**
 * 数据权限常量
 *
 * @author fjq
 * @since 2025-02-05
 */
public class DataScopeConstants {

    /**
     * 数据权限过滤SQL前缀
     */
    public static final String DATA_SCOPE_PREFIX = "dataScope_";

    /**
     * 部门表别名
     */
    public static final String DEPT_ALIAS = "d";

    /**
     * 用户表别名
     */
    public static final String USER_ALIAS = "u";

    /**
     * 数据权限全部
     */
    public static final String DATA_SCOPE_ALL = "1";

    /**
     * 数据权限自定义
     */
    public static final String DATA_SCOPE_CUSTOM = "2";

    /**
     * 数据权限本部门
     */
    public static final String DATA_SCOPE_DEPT = "3";

    /**
     * 数据权限本部门及以下
     */
    public static final String DATA_SCOPE_DEPT_AND_CHILD = "4";

    /**
     * 数据权限仅本人
     */
    public static final String DATA_SCOPE_SELF = "5";

    /**
     * SQL 模板
     */
    public static final class SQL {

        /**
         * 全部数据权限 SQL (不过滤)
         */
        public static final String SQL_ALL = "1=1";

        /**
         * 自定义数据权限 SQL
         * 格式: #{deptAlias}.dept_id IN (选中的部门ID列表)
         */
        public static final String SQL_CUSTOM = "%s.dept_id IN (%s)";

        /**
         * 本部门数据权限 SQL
         * 格式: #{deptAlias}.dept_id = #{用户部门ID}
         */
        public static final String SQL_DEPT = "%s.dept_id = %d";

        /**
         * 本部门及以下数据权限 SQL
         * 格式: #{deptAlias}.dept_id IN (用户部门及其子部门ID列表)
         * 使用 FIND_IN_SET 函数
         */
        public static final String SQL_DEPT_AND_CHILD = "FIND_IN_SET(%s.dept_id, %s)";

        /**
         * 仅本人数据权限 SQL
         * 格式: #{userAlias}.user_id = #{用户ID}
         */
        public static final String SQL_SELF = "%s.user_id = %d";

    }

    private DataScopeConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}

package me.fjq.generator.domain;

import java.io.Serializable;
import java.util.List;

/**
 * 代码生成业务表信息
 *
 * @author fjq
 * @since 2025-02-05
 */
public record GenTable(
        /**
         * 表ID
         */
        Long tableId,

        /**
         * 表名称
         */
        String tableName,

        /**
         * 表描述
         */
        String tableComment,

        /**
         * 类名称
         */
        String className,

        /**
         * 模块名
         */
        String moduleName,

        /**
         * 业务名
         */
        String businessName,

        /**
         * 功能名称
         */
        String functionName,

        /**
         * 功能作者
         */
        String functionAuthor,

        /**
         * 生成路径
         */
        String genPath,

        /**
         * 表列信息
         */
        List<GenTableColumn> columns,

        /**
         * 主键列信息
         */
        GenTableColumn pkColumn,

        /**
         * 主键类型
         * AUTO 自增
         * NONE 用户输入
         * UUID 全局唯一
         */
        String pkType
) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 简化构造器
     */
    public static GenTable of(String tableName, String tableComment) {
        return new GenTable(0L, tableName, tableComment, "", "", "", "", "", "", null, null, "");
    }

    /**
     * 设置类名
     */
    public GenTable withClassName(String className) {
        return new GenTable(tableId, tableName, tableComment, className, moduleName, businessName,
                functionName, functionAuthor, genPath, columns, pkColumn, pkType);
    }

    /**
     * 设置业务名
     */
    public GenTable withBusinessName(String businessName) {
        return new GenTable(tableId, tableName, tableComment, className, moduleName, businessName,
                functionName, functionAuthor, genPath, columns, pkColumn, pkType);
    }

    /**
     * 设置模块名
     */
    public GenTable withModuleName(String moduleName) {
        return new GenTable(tableId, tableName, tableComment, className, moduleName, businessName,
                functionName, functionAuthor, genPath, columns, pkColumn, pkType);
    }

    /**
     * 设置列信息
     */
    public GenTable withColumns(List<GenTableColumn> columns) {
        return new GenTable(tableId, tableName, tableComment, className, moduleName, businessName,
                functionName, functionAuthor, genPath, columns, pkColumn, pkType);
    }
}

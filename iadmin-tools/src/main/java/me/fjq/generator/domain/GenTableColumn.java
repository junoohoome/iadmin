package me.fjq.generator.domain;

import java.io.Serializable;

/**
 * 代码生成表字段信息
 *
 * @author fjq
 * @since 2025-02-05
 */
public record GenTableColumn(
        /**
         * 列ID
         */
        Long columnId,

        /**
         * 主键ID
         */
        Long tableId,

        /**
         * 列名
         */
        String columnName,

        /**
         * 列描述
         */
        String columnComment,

        /**
         * 列类型
         */
        String columnType,

        /**
         * Java类型
         */
        String javaType,

        /**
         * Java字段名
         */
        String javaField,

        /**
         * 是否主键
         */
        Boolean isPk,

        /**
         * 是否自增
         */
        Boolean isIncrement,

        /**
         * 是否必填
         */
        Boolean isRequired,

        /**
         * 是否为插入字段
         */
        Boolean isInsert,

        /**
         * 是否为编辑字段
         */
        Boolean isEdit,

        /**
         * 是否为列表字段
         */
        Boolean isList,

        /**
         * 是否为查询字段
         */
        Boolean isQuery,

        /**
         * 查询方式
         * EQ 等于
         * NE 不等于
         * GT 大于
         * GTE 大于等于
         * LT 小于
         * LTE 小于等于
         * LIKE 模糊查询
         * BETWEEN 范围查询
         */
        String queryType,

        /**
         * 显示类型
         * input 输入框
         * textarea 文本框
         * select 下拉框
         * checkbox 复选框
         * radio 单选框
         * datetime 日期时间控件
         * imageUpload 图片上传
         * fileUpload 文件上传
         * editor 富文本控件
         */
        String htmlType,

        /**
         * 字典类型
         */
        String dictType
) implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 简化构造器
     */
    public GenTableColumn {
        if (columnId == null) columnId = 0L;
        if (tableId == null) tableId = 0L;
        if (javaType == null) javaType = "String";
        if (isPk == null) isPk = false;
        if (isIncrement == null) isIncrement = false;
        if (isRequired == null) isRequired = false;
        if (isInsert == null) isInsert = false;
        if (isEdit == null) isEdit = false;
        if (isList == null) isList = false;
        if (isQuery == null) isQuery = false;
        if (queryType == null) queryType = "";
        if (htmlType == null) htmlType = "";
        if (dictType == null) dictType = "";
    }
}

package me.fjq.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据权限类型枚举
 *
 * @author fjq
 * @since 2025-02-05
 */
@Getter
@AllArgsConstructor
public enum DataScopeType {

    /**
     * 全部数据权限
     */
    ALL("1", "全部数据权限"),

    /**
     * 自定义数据权限
     */
    CUSTOM("2", "自定义数据权限"),

    /**
     * 本部门数据权限
     */
    DEPT("3", "本部门数据权限"),

    /**
     * 本部门及以下数据权限
     */
    DEPT_AND_CHILD("4", "本部门及以下数据权限"),

    /**
     * 仅本人数据权限
     */
    SELF("5", "仅本人数据权限");

    /**
     * 数据权限编码
     */
    private final String code;

    /**
     * 数据权限描述
     */
    private final String description;

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 数据权限类型
     */
    public static DataScopeType fromCode(String code) {
        for (DataScopeType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown data scope code: " + code);
    }

    /**
     * 判断是否为全部数据权限
     */
    public boolean isAll() {
        return this == ALL;
    }

    /**
     * 判断是否为自定义数据权限
     */
    public boolean isCustom() {
        return this == CUSTOM;
    }

    /**
     * 判断是否为本部门数据权限
     */
    public boolean isDept() {
        return this == DEPT;
    }

    /**
     * 判断是否为本部门及以下数据权限
     */
    public boolean isDeptAndChild() {
        return this == DEPT_AND_CHILD;
    }

    /**
     * 判断是否为仅本人数据权限
     */
    public boolean isSelf() {
        return this == SELF;
    }

}

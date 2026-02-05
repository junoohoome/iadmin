package me.fjq.system.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户和角色关联表(SysUserRole)表实体类
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@Getter
@Setter
public class SysUserRole {

    /**用户ID*/
    private Long userId;
    /**角色ID*/
    private Long roleId;

}
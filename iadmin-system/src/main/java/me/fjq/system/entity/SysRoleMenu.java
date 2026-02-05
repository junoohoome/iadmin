package me.fjq.system.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * 角色和菜单关联表(SysRoleMenu)表实体类
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@Getter
@Setter
public class SysRoleMenu {

    /**角色ID*/
    private Long roleId;
    /**菜单ID*/
    private Long menuId;

}
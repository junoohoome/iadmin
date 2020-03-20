package me.fjq.system.domain;


import lombok.Getter;
import lombok.Setter;

/**
 * 角色和菜单关联 sys_role_menu
 *
 * @author fjq
 * @date 2020/1/7 14:46
 */
@Getter
@Setter
public class SysRoleMenu {

    private Long id;

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 菜单ID
     */
    private Long menuId;

}

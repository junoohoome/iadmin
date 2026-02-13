package me.fjq.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色和菜单关联表(SysRoleMenu)表实体类
 *
 * 联合主键: role_id + menu_id
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@Getter
@Setter
@TableName("sys_role_menu")
public class SysRoleMenu {

    /**角色ID (联合主键之一)*/
    @TableId(value = "role_id", type = IdType.INPUT)
    @TableField("role_id")
    private Long roleId;

    /**菜单ID (联合主键之一)*/
    @TableField("menu_id")
    private Long menuId;

}
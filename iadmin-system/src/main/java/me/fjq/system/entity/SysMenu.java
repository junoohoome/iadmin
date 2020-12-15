package me.fjq.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import me.fjq.Domain.BaseEntity;

import java.util.ArrayList;
import java.util.List;


/**
 * 菜单权限表(SysMenu)表实体类
 *
 * @author fjq
 * @since 2020-03-23 22:43:48
 */
@Getter
@Setter
public class SysMenu extends BaseEntity {

    /**菜单ID*/
    @TableId(value = "menu_id", type = IdType.AUTO)
    private Long menuId;
    /**菜单名称*/
    private String menuName;
    /**父菜单ID*/
    private Long parentId;
    /**显示顺序*/
    private Integer sort;
    /**路由地址*/
    private String path;
    /**组件路径*/
    private String component;
    /**是否为外链（0是 1否）*/
    private Integer isFrame;
    /**菜单类型（M目录 C菜单 F按钮）*/
    private String menuType;
    /**菜单状态（0显示 1隐藏）*/
    private String visible;
    /**权限标识*/
    private String perms;
    /**菜单图标*/
    private String icon;
    /** 子菜单 */
    @TableField(exist = false)
    private List<SysMenu> children = new ArrayList<>();

}
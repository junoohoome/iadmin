package me.fjq.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;
import me.fjq.Domain.BaseEntity;

/**
 * 角色信息表(SysRole)表实体类
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@Getter
@Setter
public class SysRole extends BaseEntity {

    /**角色ID*/
    @TableId(value = "role_id", type = IdType.AUTO)
    private Long roleId;
    /**角色名称*/
    private String roleName;
    /**角色权限字符串*/
    private String roleKey;
    /**显示顺序*/
    private Integer sort;
    /**数据范围（1：全部数据权限 2：自定义数据权限 3：本部门数据权限 4：本部门及以下数据权限 5：仅本人数据权限）*/
    private String dataScope;
    /**角色状态（0正常 1停用）*/
    private String status;
    /**删除标志（0代表存在 2代表删除）*/
    private String delFlag;

}
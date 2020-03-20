package me.fjq.system.domain;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;
import me.fjq.Domain.BaseEntity;


/**
 * 角色表 sys_role
 *
 * @author fjq
 * @date 2020/1/7 14:46
 */
@Getter
@Setter
public class SysRole extends BaseEntity {

    /**
     * 角色ID
     */
    @TableId(value = "role_id", type = IdType.AUTO)
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色权限
     */
    private String roleKey;

    /**
     * 角色排序
     */
    private Integer sort;

    /**
     * 数据范围（1：所有数据权限；2：自定义数据权限)
     */
    private String dataScope;

    /**
     * 角色状态（0正常 1停用）
     */
    private Integer status;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private Integer delFlag;

    /**
     * 用户是否存在此角色标识 默认不存在
     */
    private Boolean flag = false;

}

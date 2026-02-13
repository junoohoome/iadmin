package me.fjq.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色与部门关联表(SysRoleDept)表实体类
 * 用于数据权限控制
 *
 * 联合主键: role_id + dept_id
 *
 * @author fjq
 * @since 2025-02-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_role_dept")
public class SysRoleDept {

    /**
     * 角色ID (联合主键之一)
     */
    @TableId(value = "role_id", type = IdType.INPUT)
    @TableField("role_id")
    private Long roleId;

    /**
     * 部门ID (联合主键之一)
     */
    @TableField("dept_id")
    private Long deptId;

}

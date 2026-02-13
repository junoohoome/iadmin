package me.fjq.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户和角色关联表(SysUserRole)表实体类
 *
 * 联合主键: user_id + role_id
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@Getter
@Setter
@TableName("sys_user_role")
public class SysUserRole {

    /**用户ID (联合主键之一)*/
    @TableId(value = "user_id", type = IdType.INPUT)
    @TableField("user_id")
    private Long userId;

    /**角色ID (联合主键之一)*/
    @TableField("role_id")
    private Long roleId;

}
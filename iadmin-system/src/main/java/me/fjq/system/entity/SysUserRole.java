package me.fjq.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
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
    @TableId(value = "user_id")
    private Long userId;
    /**角色ID*/
    @TableId(value = "role_id")
    private Long roleId;

}
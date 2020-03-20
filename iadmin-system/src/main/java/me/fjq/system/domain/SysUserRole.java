package me.fjq.system.domain;


import lombok.Getter;
import lombok.Setter;

/**
 * 用户和角色关联 sys_user_role
 *
 * @author fjq
 * @date 2020/1/7 14:46
 */
@Getter
@Setter
public class SysUserRole {

    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色ID
     */
    private Long roleId;

}

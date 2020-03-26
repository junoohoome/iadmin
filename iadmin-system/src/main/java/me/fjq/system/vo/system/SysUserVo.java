package me.fjq.system.vo.system;

import lombok.Getter;
import lombok.Setter;
import me.fjq.system.entity.SysUser;

import java.util.List;


/**
 * 用户信息表(SysUser)表实体类
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@Getter
@Setter
public class SysUserVo extends SysUser {

    /** 角色 **/
    private List roles;

}
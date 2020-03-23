package me.fjq.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import me.fjq.system.entity.SysUser;

/**
 * 用户信息表(SysUser)表服务接口
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    SysUser selectUserByUserName(String userName);

}
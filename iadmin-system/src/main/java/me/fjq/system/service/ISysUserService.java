package me.fjq.system.service;


import com.baomidou.mybatisplus.extension.service.IService;
import me.fjq.system.domain.SysUser;

/**
 * 用户业务层
 * @author fjq
 * @date 2020-1-8
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    SysUser selectUserByUserName(String userName);

}

package me.fjq.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.fjq.system.domain.SysUser;

/**
 * 用户表 数据层
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    SysUser selectUserByUserName(String userName);

}

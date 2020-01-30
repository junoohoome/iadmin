package me.fjq.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import me.fjq.system.domain.SysUser;
import me.fjq.system.mapper.SysRoleMapper;
import me.fjq.system.mapper.SysUserMapper;
import me.fjq.system.service.ISysUserService;
import org.springframework.stereotype.Service;

/**
 * @author fjq
 * @date 2020-1-8
 */
@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;

    public SysUserServiceImpl(SysUserMapper sysUserMapper, SysRoleMapper sysRoleMapper) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
    }


    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    @Override
    public SysUser selectUserByUserName(String userName) {
//        return sysUserMapper.selectOne(new QueryWrapper<SysUser>().lambda().eq(SysUser::getUserName, userName));
        return sysUserMapper.selectUserByUserName(userName);
    }

}

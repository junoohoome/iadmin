package me.fjq.system.service.impl;


import cn.hutool.core.util.ObjectUtil;
import me.fjq.system.domain.SysRole;
import me.fjq.system.domain.SysUser;
import me.fjq.system.mapper.SysMenuMapper;
import me.fjq.system.mapper.SysRoleMapper;
import me.fjq.system.mapper.SysRoleMenuMapper;
import me.fjq.system.service.ISysRoleService;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 角色 业务层处理
 */
@Service
public class SysRoleServiceImpl implements ISysRoleService {

    private final SysRoleMapper roleMapper;

    public SysRoleServiceImpl(SysRoleMapper roleMapper) {
        this.roleMapper = roleMapper;
    }

    @Override
    public Set<String> selectRolePermissionByUserId(Long userId) {
        List<SysRole> perms = roleMapper.selectRolePermissionByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (SysRole perm : perms) {
            if (ObjectUtil.isNotNull(perm)) {
                permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return permsSet;
    }

}

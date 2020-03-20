package me.fjq.system.service.impl;


import cn.hutool.core.util.ObjectUtil;
import me.fjq.system.domain.SysRole;
import me.fjq.system.mapper.SysRoleMapper;
import me.fjq.system.service.ISysRoleService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        List<SysRole> sysRoles = roleMapper.selectRolePermissionByUserId(userId);
        Set<String> perms = new HashSet<>();
        for (SysRole perm : sysRoles) {
            if (ObjectUtil.isNotNull(perm)) {
                perms.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return perms;
    }

}

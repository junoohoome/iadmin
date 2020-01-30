package me.fjq.system.service.impl;


import cn.hutool.core.util.ObjectUtil;
import me.fjq.system.domain.SysRole;
import me.fjq.system.domain.SysUser;
import me.fjq.system.mapper.SysMenuMapper;
import me.fjq.system.mapper.SysRoleMapper;
import me.fjq.system.mapper.SysRoleMenuMapper;
import me.fjq.system.service.ISysRoleService;
import me.fjq.utils.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色 业务层处理
 */
@Service
public class SysRoleServiceImpl implements ISysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysMenuMapper menuMapper;

    public SysRoleServiceImpl(SysRoleMapper roleMapper, SysRoleMenuMapper roleMenuMapper, SysMenuMapper menuMapper) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.menuMapper = menuMapper;
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
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

    @Override
    public Collection<GrantedAuthority> mapToGrantedAuthorities(SysUser user) {
        List<String> perms = menuMapper.selectMenuPermsByUserId(user.getUserId());
        Set<String> permissions = new HashSet<>();
        for (String perm : perms) {
            if (StringUtils.isNotEmpty(perm)) {
                permissions.addAll(Arrays.asList(perm.trim().split(",")));
            }
        }
        return permissions.stream().map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}

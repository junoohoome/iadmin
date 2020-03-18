package me.fjq.system.service.impl;


import me.fjq.system.mapper.SysMenuMapper;
import me.fjq.system.service.ISysMenuService;
import me.fjq.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * 菜单 业务层处理
 */
@Service
public class SysMenuServiceImpl implements ISysMenuService {

    private final SysMenuMapper menuMapper;

    public SysMenuServiceImpl(SysMenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectMenuPermsByUserId(Long userId) {
        List<String> perms = menuMapper.selectMenuPermsByUserId(userId);
        Set<String> permissions = new HashSet<>();
        for (String perm : perms) {
            if (StringUtils.isNotEmpty(perm)) {
                permissions.addAll(Arrays.asList(perm.trim().split(",")));
            }
        }
        return permissions;
    }

}

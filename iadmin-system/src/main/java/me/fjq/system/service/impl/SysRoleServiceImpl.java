package me.fjq.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.cache.MultiLevelCacheService;
import me.fjq.system.entity.SysRole;
import me.fjq.system.entity.SysRoleMenu;
import me.fjq.system.entity.SysUserRole;
import me.fjq.system.mapper.SysRoleMapper;
import me.fjq.system.mapper.SysRoleMenuMapper;
import me.fjq.system.mapper.SysUserRoleMapper;
import me.fjq.system.service.SysMenuService;
import me.fjq.system.service.SysRoleService;
import me.fjq.system.vo.SelectOptions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色信息表(SysRole)表服务实现类
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@Slf4j
@RequiredArgsConstructor
@Service("sysRoleService")
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysMenuService menuService;
    private final MultiLevelCacheService cacheService;

    /**
     * 查询角色下拉选项（带缓存）
     *
     * @return 角色下拉选项列表
     */
    public List<SelectOptions> selectRoleOptions() {
        return cacheService.getRoleList(() ->
                list().stream().map(sysRole -> {
                    SelectOptions options = new SelectOptions();
                    options.setText(sysRole.getRoleName());
                    options.setValue(sysRole.getRoleId());
                    return options;
                }).collect(Collectors.toList())
        );
    }

    @Override
    public Set<String> selectRolePermsByUserId(Long userId) {
        List<SysRole> sysRoles = roleMapper.selectRolePermissionByUserId(userId);
        Set<String> perms = new HashSet<>();
        for (SysRole perm : sysRoles) {
            if (ObjectUtil.isNotNull(perm)) {
                perms.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return perms;
    }

    @Override
    public List<Long> selectRoleMenuListByRoleId(Long roleId) {
        return roleMenuMapper.selectList(new QueryWrapper<SysRoleMenu>().lambda().eq(SysRoleMenu::getRoleId, roleId))
                .stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
    }

    /**
     * 保存角色并清除缓存
     */
    @Override
    public boolean save(SysRole entity) {
        boolean result = super.save(entity);
        if (result) {
            cacheService.evictRoleList();
        }
        return result;
    }

    /**
     * 更新角色并清除缓存
     */
    @Override
    public boolean updateById(SysRole entity) {
        boolean result = super.updateById(entity);
        if (result) {
            cacheService.evictRoleList();
        }
        return result;
    }

    /**
     * 删除角色并清除缓存
     */
    @Override
    public boolean removeById(SysRole entity) {
        boolean result = super.removeById(entity);
        if (result) {
            cacheService.evictRoleList();
        }
        return result;
    }

    /**
     * 更新角色权限
     * <p>使用事务保证数据一致性，使用批量插入优化性能
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表（逗号分隔）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePermissions(Long roleId, String menuIds) {
        String[] split = StringUtils.split(menuIds, ",");
        // 先删除旧权限
        roleMenuMapper.delete(new QueryWrapper<SysRoleMenu>().lambda().eq(SysRoleMenu::getRoleId, roleId));

        // 批量插入新权限（优化：使用 Db.saveBatch 替代循环单条插入）
        if (split != null && split.length > 0) {
            List<SysRoleMenu> roleMenuList = Arrays.stream(split)
                    .map(menuId -> {
                        SysRoleMenu roleMenu = new SysRoleMenu();
                        roleMenu.setMenuId(Long.parseLong(menuId));
                        roleMenu.setRoleId(roleId);
                        return roleMenu;
                    })
                    .collect(Collectors.toList());
            // 使用 MyBatis-Plus 批量插入（一条 SQL 插入多条数据）
            Db.saveBatch(roleMenuList);
        }

        // 清除该角色下所有用户的权限缓存
        clearPermissionCacheByRoleId(roleId);
    }

    /**
     * 清除角色下所有用户的权限缓存
     *
     * @param roleId 角色ID
     */
    private void clearPermissionCacheByRoleId(Long roleId) {
        try {
            // 查询该角色下的所有用户ID
            List<SysUserRole> userRoles = userRoleMapper.selectList(
                    new QueryWrapper<SysUserRole>().lambda().eq(SysUserRole::getRoleId, roleId));
            List<Long> userIds = userRoles.stream()
                    .map(SysUserRole::getUserId)
                    .collect(Collectors.toList());

            // 批量清除缓存
            menuService.clearUserPermissionCacheBatch(userIds);
            log.info("角色权限变更，已清除用户缓存, roleId: {}, userCount: {}", roleId, userIds.size());
        } catch (Exception e) {
            log.warn("清除角色用户缓存失败, roleId: {}, error: {}", roleId, e.getMessage());
        }
    }
}

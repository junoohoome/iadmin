package me.fjq.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import me.fjq.system.entity.SysRole;
import me.fjq.system.entity.SysRoleMenu;
import me.fjq.system.mapper.SysRoleMapper;
import me.fjq.system.mapper.SysRoleMenuMapper;
import me.fjq.system.service.SysRoleService;
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
@AllArgsConstructor
@Service("sysRoleService")
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysRoleMapper roleMapper;
    private final SysRoleMenuMapper roleMenuMapper;

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

        // 批量插入新权限（优化性能）
        if (split != null && split.length > 0) {
            List<SysRoleMenu> roleMenuList = new ArrayList<>(split.length);
            for (String menuId : split) {
                SysRoleMenu roleMenu = new SysRoleMenu();
                roleMenu.setMenuId(Long.parseLong(menuId));
                roleMenu.setRoleId(roleId);
                roleMenuList.add(roleMenu);
            }
            // 使用 MyBatis-Plus 批量插入
            for (SysRoleMenu roleMenu : roleMenuList) {
                roleMenuMapper.insert(roleMenu);
            }
        }
    }
}
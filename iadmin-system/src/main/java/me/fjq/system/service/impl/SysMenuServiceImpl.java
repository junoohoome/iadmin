package me.fjq.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.constant.Constants;
import me.fjq.constant.MenuConstants;
import me.fjq.system.entity.SysMenu;
import me.fjq.system.mapper.SysMenuMapper;
import me.fjq.system.service.SysMenuService;
import me.fjq.system.vo.MetaVo;
import me.fjq.system.vo.RouterVo;
import me.fjq.system.vo.TreeSelect;
import me.fjq.utils.RedisUtils;
import me.fjq.utils.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜单权限表(SysMenu)表服务实现类
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@Slf4j
@AllArgsConstructor
@Service("sysMenuService")
@Transactional(rollbackFor = Exception.class)
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private final SysMenuMapper menuMapper;
    private final RedisUtils redisUtils;

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> selectMenuPermsByUserId(Long userId) {
        String cacheKey = Constants.USER_PERMISSIONS_KEY + userId;

        // 1. 先查缓存
        try {
            Object cached = redisUtils.get(cacheKey);
            if (cached instanceof Set) {
                Set<String> cachedPermissions = (Set<String>) cached;
                if (CollectionUtil.isNotEmpty(cachedPermissions)) {
                    log.debug("用户权限缓存命中, userId: {}", userId);
                    return cachedPermissions;
                }
            }
        } catch (Exception e) {
            log.warn("读取权限缓存失败,降级到数据库查询, userId: {}, error: {}", userId, e.getMessage());
        }

        // 2. 缓存未命中，查询数据库
        List<String> perms = menuMapper.selectMenuPermsByUserId(userId);
        Set<String> permissions = new HashSet<>();
        for (String perm : perms) {
            if (StringUtils.isNotEmpty(perm)) {
                permissions.addAll(Arrays.asList(perm.trim().split(",")));
            }
        }

        // 3. 存入缓存（30分钟过期，加随机偏移防止雪崩）
        try {
            long expireTime = Constants.USER_PERMISSIONS_EXPIRE_TIME + (long) (Math.random() * 5);
            redisUtils.set(cacheKey, permissions, expireTime, TimeUnit.MINUTES);
            log.debug("用户权限已缓存, userId: {}, expireTime: {}min", userId, expireTime);
        } catch (Exception e) {
            log.warn("缓存用户权限失败, userId: {}, error: {}", userId, e.getMessage());
        }

        return permissions;
    }

    /**
     * 清除用户权限缓存
     * @param userId 用户ID
     */
    public void clearUserPermissionCache(Long userId) {
        try {
            String cacheKey = Constants.USER_PERMISSIONS_KEY + userId;
            redisUtils.del(cacheKey);
            log.info("已清除用户权限缓存, userId: {}", userId);
        } catch (Exception e) {
            log.warn("清除用户权限缓存失败, userId: {}, error: {}", userId, e.getMessage());
        }
    }

    /**
     * 批量清除用户权限缓存
     * @param userIds 用户ID列表
     */
    public void clearUserPermissionCacheBatch(List<Long> userIds) {
        if (CollectionUtil.isEmpty(userIds)) {
            return;
        }
        for (Long userId : userIds) {
            clearUserPermissionCache(userId);
        }
        log.info("已批量清除用户权限缓存, count: {}", userIds.size());
    }

    @Override
    public List<SysMenu> selectMenuTreeByUserId(Long userId, String menuName, Boolean isRouterSelect) {
        List<SysMenu> menus;
        if (SecurityUtils.isAdmin(userId)) {
            menus = menuMapper.selectMenuTreeAll(menuName, isRouterSelect);
        } else {
            menus = menuMapper.selectMenuTreeByUserId(userId, menuName, isRouterSelect);
        }
        return getChildMenuTree(menus, MenuConstants.ROOT_PARENT_ID.intValue());
    }

    @Override
    public List<RouterVo> buildMenus(List<SysMenu> menus) {
        List<RouterVo> routers = new LinkedList<>();
        for (SysMenu menu : menus) {
            RouterVo router = new RouterVo();
            router.setId(menu.getMenuId());
            router.setParentId(menu.getParentId());
            router.setName(StringUtils.capitalize(menu.getPath()));
            router.setPath(getRouterPath(menu));
            router.setComponent(StringUtils.isEmpty(menu.getComponent()) ? MenuConstants.LAYOUT_COMPONENT : menu.getComponent());
            router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon()));
            // visible="0" 表示隐藏, "1" 表示可见，需要转换为 hidden（含义相反）
            router.setHidden(MenuConstants.VISIBLE_SHOW.equals(menu.getVisible()) ? "1" : "0");
            router.setVisible(menu.getVisible());
            router.setType(menu.getMenuType());
            router.setSort(menu.getSort());
            List<SysMenu> cMenus = menu.getChildren();
            if (CollectionUtil.isNotEmpty(cMenus) && MenuConstants.MENU_TYPE_DIR.equals(menu.getMenuType())) {
                router.setAlwaysShow(true);
                router.setRedirect(MenuConstants.NO_REDIRECT);
                router.setChildren(buildMenus(cMenus));
            }
            routers.add(router);
        }
        return routers;
    }

    /**
     * 构建前端路由（带完整字段）
     */
    public List<RouterVo> buildMenusForFront(List<SysMenu> menus) {
        return buildMenus(menus);
    }

    @Override
    public List<TreeSelect> buildMenuTreeSelect(List<SysMenu> menus) {
        return menus.stream().map(TreeSelect::new).collect(Collectors.toList());
    }

    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    private String getRouterPath(SysMenu menu) {
        String routerPath = menu.getPath();
        // 非外链并且是一级目录
        if (MenuConstants.ROOT_PARENT_ID.equals(menu.getParentId()) && MenuConstants.IS_FRAME_NO.equals(menu.getIsFrame())) {
            routerPath = "/" + menu.getPath();
        }
        return routerPath;
    }

    /**
     * 根据父节点的ID获取所有子节点
     *
     * @param list     分类表
     * @param parentId 传入的父节点ID
     * @return String
     */
    private List<SysMenu> getChildMenuTree(List<SysMenu> list, int parentId) {
        List<SysMenu> returnList = new ArrayList<>();
        for (Iterator<SysMenu> iterator = list.iterator(); iterator.hasNext(); ) {
            SysMenu t = iterator.next();
            // 根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (t.getParentId() == parentId) {
                recursionFn(list, t);
                returnList.add(t);
            }
        }
        // 如果不是从根结点开始获取，那么就直接返回原数据
        if (returnList.isEmpty()) {
            returnList = list;
        }
        return returnList;
    }

    /**
     * 递归列表
     *
     * @param list
     * @param t
     */
    private void recursionFn(List<SysMenu> list, SysMenu t) {
        // 得到子节点列表
        List<SysMenu> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysMenu tChild : childList) {
            if (hasChild(list, tChild)) {
                // 判断是否有子节点
                Iterator<SysMenu> it = childList.iterator();
                while (it.hasNext()) {
                    SysMenu n = it.next();
                    recursionFn(list, n);
                }
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<SysMenu> getChildList(List<SysMenu> list, SysMenu t) {
        List<SysMenu> tlist = new ArrayList<>();
        Iterator<SysMenu> it = list.iterator();
        while (it.hasNext()) {
            SysMenu n = it.next();
            if (n.getParentId().longValue() == t.getMenuId().longValue()) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<SysMenu> list, SysMenu t) {
        return getChildList(list, t).size() > 0;
    }

}
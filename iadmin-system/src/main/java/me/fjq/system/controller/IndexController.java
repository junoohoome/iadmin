package me.fjq.system.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.core.HttpResult;
import me.fjq.security.JwtUserDetails;
import me.fjq.security.utils.SecurityUtils;
import me.fjq.system.entity.SysMenu;
import me.fjq.system.service.SysMenuService;
import me.fjq.system.service.SysRoleService;
import me.fjq.system.vo.RouterVo;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author fjq
 */
@Slf4j
@RestController
@RequestMapping
@AllArgsConstructor
public class IndexController {

    private final UserDetailsService userDetailsService;
    private final SysRoleService roleService;
    private final SysMenuService menuService;

    @GetMapping(value = "user/info")
    public HttpResult getUserInfo() {
        JwtUserDetails user = (JwtUserDetails) userDetailsService.loadUserByUsername(SecurityUtils.getUsername());
        Set<String> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();
        // 管理员拥有所有权限
        boolean isAdmin = SecurityUtils.isAdmin(user.getId());
        if (isAdmin) {
            roles.add("admin");
            permissions.add("*:*:*");
        } else {
            roles.addAll(roleService.selectRolePermissionByUserId(user.getId()));
            permissions.addAll(menuService.selectMenuPermsByUserId(user.getId()));
        }
        Map<String, Object> map = new HashMap<>(3);
        map.put("user", user);
        map.put("roles", roles);
        map.put("permissions", permissions);
        return HttpResult.ok(map);
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public HttpResult getRouters() {
        JwtUserDetails user = (JwtUserDetails) userDetailsService.loadUserByUsername(SecurityUtils.getUsername());
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(user.getId());
        List<RouterVo> routerVos = menuService.buildMenus(menus);
        return HttpResult.ok(routerVos);
    }
}

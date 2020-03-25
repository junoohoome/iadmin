package me.fjq.system.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.core.HttpResult;
import me.fjq.security.JwtTokenService;
import me.fjq.security.JwtUserDetails;
import me.fjq.utils.SecurityUtils;
import me.fjq.system.entity.SysMenu;
import me.fjq.system.service.SysMenuService;
import me.fjq.system.service.SysRoleService;
import me.fjq.system.vo.RouterVo;
import me.fjq.utils.ServletUtils;
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

    private final SysRoleService roleService;
    private final SysMenuService menuService;
    private final JwtTokenService jwtTokenService;

    @GetMapping(value = "user/info")
    public HttpResult getUserInfo() {
        JwtUserDetails user = jwtTokenService.getJwtUserDetails(ServletUtils.getRequest());
        Set<String> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();
        // 管理员拥有所有权限
        boolean isAdmin = SecurityUtils.isAdmin(user.getId());
        if (isAdmin) {
            roles.add("superadmin");
            permissions.add("superadmin");
        } else {
            roles.addAll(roleService.selectRolePermsByUserId(user.getId()));
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
        JwtUserDetails user = jwtTokenService.getJwtUserDetails(ServletUtils.getRequest());
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(user.getId());
        List<RouterVo> routerVos = menuService.buildMenus(menus);
        return HttpResult.ok(routerVos);
    }
}

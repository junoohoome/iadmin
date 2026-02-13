package me.fjq.system.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.core.HttpResult;
import me.fjq.constant.Constants;
import me.fjq.security.JwtTokenService;
import me.fjq.security.JwtUserDetails;
import me.fjq.system.entity.SysMenu;
import me.fjq.system.service.SysMenuService;
import me.fjq.system.service.SysRoleService;
import me.fjq.utils.ServletUtils;
import me.fjq.system.vo.MetaVo;
import me.fjq.system.vo.RouterVo;
import me.fjq.utils.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户信息表(SysUser)表控制层
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@RestController
@RequestMapping("user/info")
@AllArgsConstructor
public class IndexController {

    private final SysRoleService roleService;
    private final SysMenuService menuService;
    private final JwtTokenService jwtTokenService;

    /**
     * 获取用户信息
     * @param request 用户信息
     * @return 用户信息
     */
    @GetMapping
    public HttpResult getUserInfo() {
        JwtUserDetails user = jwtTokenService.getJwtUserDetails(ServletUtils.getRequest());
        Set<String> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();

        // 管理员拥有所有权限
        boolean isAdmin = SecurityUtils.isAdmin(user.getId());
        if (isAdmin) {
            roles.add(Constants.SYS_ADMIN_ROLE);
            permissions.add(Constants.SYS_ADMIN_PERMISSION);
        } else {
            roles.addAll(roleService.selectRolePermsByUserId(user.getId()));
            permissions.addAll(menuService.selectMenuPermsByUserId(user.getId()));
        }

        Map<String, Object> map = new HashMap<>();
        map.put("user", user);
        map.put("roles", roles);
        map.put("permissions", permissions);

        return HttpResult.ok(map);
    }

    /**
     * 获取路由信息
     * @return 路由配置
     */
    @GetMapping("getRouters")
    public HttpResult getRouters() {
        JwtUserDetails user = jwtTokenService.getJwtUserDetails(ServletUtils.getRequest());
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(user.getId(), "", true);
        List<RouterVo> routerVos = new ArrayList<>();

        for (SysMenu menu : menus) {
            routerVos.add(buildRouter(menu));
        }

        return HttpResult.ok(routerVos);
    }

    /**
     * 构建路由对象（递归处理子菜单）
     */
    private RouterVo buildRouter(SysMenu menu) {
        RouterVo router = new RouterVo();
        router.setId(menu.getMenuId());
        router.setParentId(menu.getParentId());
        router.setName(menu.getMenuName());

        // 路径处理：如果已有前导斜杠则不再添加
        String path = menu.getPath();
        if (path != null && !path.startsWith("/")) {
            path = "/" + path;
        }
        router.setPath(path);

        // 当 component 为空且是根目录时，设置为 Layout 字符串，确保前端正确识别
        router.setComponent(menu.getComponent() != null ? menu.getComponent() :
                          (menu.getParentId() == 0 ? "Layout" : null));
        router.setType(menu.getMenuType());
        router.setSort(menu.getSort());

        // visible: 0=显示(hidden=false), 1=隐藏(hidden=true)
        // 注意：后端 visible 字段含义与前端 hidden 相反
        String visible = menu.getVisible();
        router.setHidden("1".equals(visible) ? "1" : "0");
        router.setVisible(visible);

        // redirect 和 alwaysShow 暂时为 null（可后续扩展）
        router.setRedirect(null);
        router.setAlwaysShow(null);

        // 构建 meta 信息
        MetaVo meta = new MetaVo(menu.getMenuName(), menu.getIcon());
        router.setMeta(meta);

        // 递归处理子菜单
        if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
            List<RouterVo> children = new ArrayList<>();
            for (SysMenu child : menu.getChildren()) {
                // 只处理类型为菜单或目录的，按钮类型不加入路由
                if ("M".equals(child.getMenuType()) || "C".equals(child.getMenuType())) {
                    children.add(buildRouter(child));
                }
            }
            router.setChildren(children);
        }

        return router;
    }
}
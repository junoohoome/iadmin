package me.fjq.system.controller;


import me.fjq.annotation.Log;
import me.fjq.core.HttpResult;
import me.fjq.security.JwtTokenService;
import me.fjq.security.JwtUserDetails;
import me.fjq.system.entity.SysMenu;
import me.fjq.system.service.SysMenuService;
import me.fjq.utils.ServletUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 菜单权限表(SysMenu)表控制层
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@RestController
@RequestMapping("sysMenu")
public class SysMenuController {

    private final SysMenuService sysMenuService;
    private final JwtTokenService jwtTokenService;

    public SysMenuController(SysMenuService sysMenuService, JwtTokenService jwtTokenService) {
        this.sysMenuService = sysMenuService;
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * 获取菜单列表
     */
    @PreAuthorize("@ss.hasPerms('system:menu:list')")
    @GetMapping("list")
    public HttpResult list(String menuName) {
        JwtUserDetails user = jwtTokenService.getJwtUserDetails(ServletUtils.getRequest());
        List<SysMenu> menus = sysMenuService.selectMenuTreeByUserId(user.getId(), menuName, false);
        return HttpResult.ok(menus);
    }

    /**
     * 获取菜单下拉树列表
     * isRouterSelect: true 因为不需要查询增删查改操作菜单
     */
    @GetMapping("treeSelect")
    public HttpResult treeSelect(String menuName) {
        JwtUserDetails user = jwtTokenService.getJwtUserDetails(ServletUtils.getRequest());
        List<SysMenu> menus = sysMenuService.selectMenuTreeByUserId(user.getId(), menuName, true);
        return HttpResult.ok(sysMenuService.buildMenuTreeSelect(menus));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @PreAuthorize("@ss.hasPerms('system:menu:list')")
    @GetMapping("{id}")
    public HttpResult selectOne(@PathVariable Serializable id) {
        return HttpResult.ok(this.sysMenuService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param sysMenu 实体对象
     * @return 新增结果
     */
    @PreAuthorize("@ss.hasPerms('system:menu:add')")
    @Log(title = "菜单管理", businessType = 1)
    @PostMapping
    public HttpResult insert(@RequestBody SysMenu sysMenu) {
        return HttpResult.ok(this.sysMenuService.save(sysMenu));
    }

    /**
     * 修改数据
     *
     * @param sysMenu 实体对象
     * @return 修改结果
     */
    @PreAuthorize("@ss.hasPerms('system:menu:edit')")
    @Log(title = "菜单管理", businessType = 2)
    @PutMapping
    public HttpResult update(@RequestBody SysMenu sysMenu) {
        return HttpResult.ok(this.sysMenuService.updateById(sysMenu));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合 (comma-separated string)
     * @return 删除结果
     */
    @PreAuthorize("@ss.hasPerms('system:menu:del')")
    @Log(title = "菜单管理", businessType = 3)
    @DeleteMapping("{idList}")
    public HttpResult delete(@PathVariable String idList) {
        List<Long> ids = Arrays.stream(idList.split(","))
            .map(Long::valueOf)
            .collect(Collectors.toList());
        return HttpResult.ok(this.sysMenuService.removeByIds(ids));
    }

    /**
     * 获取路由菜单（前端动态路由使用）
     */
    @GetMapping("getRouters")
    public HttpResult getRouters() {
        JwtUserDetails user = jwtTokenService.getJwtUserDetails(ServletUtils.getRequest());
        List<SysMenu> menus = sysMenuService.selectMenuTreeByUserId(user.getId(), null, false);
        return HttpResult.ok(sysMenuService.buildMenusForFront(menus));
    }
}
package me.fjq.system.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.fjq.annotation.Log;
import me.fjq.constant.Constants;
import me.fjq.core.HttpResult;
import me.fjq.system.entity.SysRole;
import me.fjq.system.service.impl.SysRoleServiceImpl;
import me.fjq.system.vo.SelectOptions;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 角色信息表(SysRole)表控制层
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@RestController
@RequestMapping("sysRole")
public class SysRoleController {

    private final SysRoleServiceImpl sysRoleService;

    public SysRoleController(SysRoleServiceImpl sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    /**
     * 分页查询所有数据
     *
     * @param page    分页对象
     * @param sysRole 查询实体
     * @return 所有数据
     */
    @PreAuthorize("@ss.hasPerms('system:role:list')")
    @GetMapping
    public HttpResult selectAll(Page<SysRole> page, SysRole sysRole) {
        return HttpResult.ok(sysRoleService.page(page, new QueryWrapper<>(sysRole)));
    }

    /**
     * 查询下拉框所有数据（带缓存）
     *
     * @return 所有数据
     */
    @GetMapping("selectOptions")
    public HttpResult selectOptions() {
        return HttpResult.ok(sysRoleService.selectRoleOptions());
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @PreAuthorize("@ss.hasPerms('system:role:list')")
    @GetMapping("{id}")
    public HttpResult selectOne(@PathVariable Serializable id) {
        return HttpResult.ok(sysRoleService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param sysRole 实体对象
     * @return 新增结果
     */
    @PreAuthorize("@ss.hasPerms('system:role:add')")
    @Log(title = "角色管理", businessType = 1)
    @PostMapping
    public HttpResult insert(@RequestBody SysRole sysRole) {
        return HttpResult.ok(sysRoleService.save(sysRole));
    }

    /**
     * 修改数据
     *
     * @param sysRole 实体对象
     * @return 修改结果
     */
    @PreAuthorize("@ss.hasPerms('system:role:edit')")
    @Log(title = "角色管理", businessType = 2)
    @PutMapping
    public HttpResult update(@RequestBody SysRole sysRole) {
        return HttpResult.ok(sysRoleService.updateById(sysRole));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合 (comma-separated string)
     * @return 删除结果
     */
    @PreAuthorize("@ss.hasPerms('system:role:del')")
    @Log(title = "角色管理", businessType = 3)
    @DeleteMapping("{idList}")
    public HttpResult delete(@PathVariable String idList) {
        List<Long> ids = Arrays.stream(idList.split(","))
            .map(Long::valueOf)
            .collect(Collectors.toList());
        return HttpResult.ok(sysRoleService.removeByIds(ids));
    }

    @PreAuthorize("@ss.hasPerms('system:role:list')")
    @GetMapping("selectMenuIds")
    public HttpResult<List<Long>> findRoleMenuListByRoleId(@RequestParam("roleId") String roleId) {
        return HttpResult.ok(sysRoleService.selectRoleMenuListByRoleId(Long.valueOf(roleId)));
    }

    @PreAuthorize("@ss.hasPerms('system:role:edit')")
    @Log(title = "角色管理", businessType = 4)
    @PutMapping("update/permissions")
    public HttpResult updatePermissions(@RequestParam("roleId") String roleId, @RequestParam("menuIds") String menuIds) {
        if (ObjectUtils.isNull(roleId, menuIds)) {
            return HttpResult.error(Constants.INVALID_PARAMS);
        }
        sysRoleService.updatePermissions(Long.valueOf(roleId), menuIds);
        return HttpResult.ok();
    }
}
package me.fjq.system.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import me.fjq.core.HttpResult;
import me.fjq.system.entity.SysUser;
import me.fjq.system.query.SysUserQuery;
import me.fjq.system.service.SysUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;


/**
 * 用户信息表(SysUser)表控制层
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@AllArgsConstructor
@RestController
@RequestMapping("sysUser")
public class SysUserController {
    /**
     * 服务对象
     */
    private final SysUserService sysUserService;

    /**
     * 分页查询所有数据
     *
     * @param page  分页对象
     * @param query 查询实体
     * @return 所有数据
     */
    @PreAuthorize("@ss.hasPerms('admin,system:user:list')")
    @GetMapping
    public HttpResult selectAll(Page page, SysUserQuery query) {
        return HttpResult.ok(this.sysUserService.selectPage(page, query));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public HttpResult selectOne(@PathVariable Serializable id) {
        return HttpResult.ok(this.sysUserService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param sysUser 实体对象
     * @return 新增结果
     */
    @PreAuthorize("@ss.hasPerms('admin,system:user:add')")
    @PostMapping
    public HttpResult insert(@RequestBody SysUser sysUser) {
        return HttpResult.ok(this.sysUserService.save(sysUser));
    }

    /**
     * 修改数据
     *
     * @param sysUser 实体对象
     * @return 修改结果
     */
    @PreAuthorize("@ss.hasPerms('admin,system:user:edit')")
    @PutMapping
    public HttpResult update(@RequestBody SysUser sysUser) {
        return HttpResult.ok(this.sysUserService.updateById(sysUser));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @PreAuthorize("@ss.hasPerms('admin,system:user:del')")
    @DeleteMapping
    public HttpResult delete(@RequestParam("idList") List<Long> idList) {
        return HttpResult.ok(this.sysUserService.removeByIds(idList));
    }
}
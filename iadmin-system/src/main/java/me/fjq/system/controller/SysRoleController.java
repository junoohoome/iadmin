package me.fjq.system.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.fjq.core.HttpResult;
import me.fjq.system.entity.SysRole;
import me.fjq.system.service.SysRoleService;
import me.fjq.system.vo.SelectOptions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.Serializable;
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
    /**
     * 服务对象
     */
    @Resource
    private SysRoleService sysRoleService;

    /**
     * 分页查询所有数据
     *
     * @param page    分页对象
     * @param sysRole 查询实体
     * @return 所有数据
     */
    @GetMapping
    public HttpResult selectAll(Page<SysRole> page, SysRole sysRole) {
        return HttpResult.ok(this.sysRoleService.page(page, new QueryWrapper<>(sysRole)));
    }

    /**
     * 查询下拉框所有数据
     *
     * @return 所有数据
     */
    @GetMapping("selectOptions")
    public HttpResult selectOptions() {
        List<SelectOptions> list = this.sysRoleService.list().stream().map(sysRole -> {
            SelectOptions options = new SelectOptions();
            options.setText(sysRole.getRoleName());
            options.setValue(sysRole.getRoleId());
            return options;
        }).collect(Collectors.toList());

        return HttpResult.ok(list);
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public HttpResult selectOne(@PathVariable Serializable id) {
        return HttpResult.ok(this.sysRoleService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param sysRole 实体对象
     * @return 新增结果
     */
    @PostMapping
    public HttpResult insert(@RequestBody SysRole sysRole) {
        return HttpResult.ok(this.sysRoleService.save(sysRole));
    }

    /**
     * 修改数据
     *
     * @param sysRole 实体对象
     * @return 修改结果
     */
    @PutMapping
    public HttpResult update(@RequestBody SysRole sysRole) {
        return HttpResult.ok(this.sysRoleService.updateById(sysRole));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping
    public HttpResult delete(@RequestParam("idList") List<Long> idList) {
        return HttpResult.ok(this.sysRoleService.removeByIds(idList));
    }

    @GetMapping("selectMenuIds")
    public HttpResult<List<Long>> findRoleMenuListByRoleId(@RequestParam("roleId") String roleId) {
        return HttpResult.ok(this.sysRoleService.selectRoleMenuListByRoleId(Long.valueOf(roleId)));
    }

}
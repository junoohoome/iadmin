package me.fjq.system.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.fjq.annotation.Log;
import me.fjq.core.HttpResult;
import me.fjq.system.entity.SysDept;
import me.fjq.system.service.impl.SysDeptServiceImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门信息表(SysDept)表控制层
 *
 * @author fjq
 * @since 2025-02-13
 */
@RestController
@RequestMapping("sysDept")
public class SysDeptController {

    private final SysDeptServiceImpl sysDeptService;

    public SysDeptController(SysDeptServiceImpl sysDeptService) {
        this.sysDeptService = sysDeptService;
    }

    /**
     * 分页查询所有数据
     *
     * @param page    分页对象
     * @param sysDept 查询实体
     * @return 所有数据
     */
    @PreAuthorize("@ss.hasPerms('system:dept:list')")
    @GetMapping
    public HttpResult selectAll(Page<SysDept> page, SysDept sysDept) {
        return HttpResult.ok(sysDeptService.page(page,
                Wrappers.<SysDept>lambdaQuery()
                        .eq(sysDept.getDeptId() != null, SysDept::getDeptId, sysDept.getDeptId())
                        .eq(sysDept.getParentId() != null, SysDept::getParentId, sysDept.getParentId())
                        .like(sysDept.getDeptName() != null, SysDept::getDeptName, sysDept.getDeptName())
        ));
    }

    /**
     * 查询树形列表（带缓存）
     *
     * @return 树形数据
     */
    @PreAuthorize("@ss.hasPerms('system:dept:list')")
    @GetMapping("list")
    public HttpResult list() {
        return HttpResult.ok(sysDeptService.selectDeptTree());
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @PreAuthorize("@ss.hasPerms('system:dept:list')")
    @GetMapping("{id}")
    public HttpResult selectOne(@PathVariable Serializable id) {
        return HttpResult.ok(sysDeptService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param sysDept 实体对象
     * @return 新增结果
     */
    @PreAuthorize("@ss.hasPerms('system:dept:add')")
    @Log(title = "部门管理", businessType = 1)
    @PostMapping
    public HttpResult insert(@RequestBody SysDept sysDept) {
        return HttpResult.ok(sysDeptService.save(sysDept));
    }

    /**
     * 修改数据
     *
     * @param sysDept 实体对象
     * @return 修改结果
     */
    @PreAuthorize("@ss.hasPerms('system:dept:edit')")
    @Log(title = "部门管理", businessType = 2)
    @PutMapping
    public HttpResult update(@RequestBody SysDept sysDept) {
        return HttpResult.ok(sysDeptService.updateById(sysDept));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合 (comma-separated string)
     * @return 删除结果
     */
    @PreAuthorize("@ss.hasPerms('system:dept:del')")
    @Log(title = "部门管理", businessType = 3)
    @DeleteMapping("{idList}")
    public HttpResult delete(@PathVariable String idList) {
        List<Long> ids = Arrays.stream(idList.split(","))
            .map(Long::valueOf)
            .collect(Collectors.toList());
        return HttpResult.ok(sysDeptService.removeByIds(ids));
    }
}

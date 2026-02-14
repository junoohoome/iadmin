package me.fjq.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.fjq.core.HttpResult;
import me.fjq.system.entity.SysDept;
import me.fjq.system.service.SysDeptService;
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

    private final SysDeptService sysDeptService;

    public SysDeptController(SysDeptService sysDeptService) {
        this.sysDeptService = sysDeptService;
    }

    /**
     * 分页查询所有数据
     *
     * @param page    分页对象
     * @param sysDept 查询实体
     * @return 所有数据
     */
    @GetMapping
    public HttpResult selectAll(Page<SysDept> page, SysDept sysDept) {
        return HttpResult.ok(this.sysDeptService.page(page,
                Wrappers.<SysDept>lambdaQuery()
                        .eq(sysDept.getDeptId() != null, SysDept::getDeptId, sysDept.getDeptId())
                        .eq(sysDept.getParentId() != null, SysDept::getParentId, sysDept.getParentId())
                        .like(sysDept.getDeptName() != null, SysDept::getDeptName, sysDept.getDeptName())
        ));
    }

    /**
     * 查询树形列表
     *
     * @return 树形数据
     */
    @GetMapping("list")
    public HttpResult list() {
        List<SysDept> list = this.sysDeptService.list(
                Wrappers.<SysDept>lambdaQuery()
                        .orderByAsc(SysDept::getOrderNum)
        );
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
        return HttpResult.ok(this.sysDeptService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param sysDept 实体对象
     * @return 新增结果
     */
    @PostMapping
    public HttpResult insert(@RequestBody SysDept sysDept) {
        return HttpResult.ok(this.sysDeptService.save(sysDept));
    }

    /**
     * 修改数据
     *
     * @param sysDept 实体对象
     * @return 修改结果
     */
    @PutMapping
    public HttpResult update(@RequestBody SysDept sysDept) {
        return HttpResult.ok(this.sysDeptService.updateById(sysDept));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合 (comma-separated string)
     * @return 删除结果
     */
    @DeleteMapping("{idList}")
    public HttpResult delete(@PathVariable String idList) {
        List<Long> ids = Arrays.stream(idList.split(","))
            .map(Long::valueOf)
            .collect(Collectors.toList());
        return HttpResult.ok(this.sysDeptService.removeByIds(ids));
    }
}

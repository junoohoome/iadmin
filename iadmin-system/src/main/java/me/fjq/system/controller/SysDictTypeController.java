package me.fjq.system.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.fjq.core.HttpResult;
import me.fjq.system.entity.SysDictType;
import me.fjq.system.service.SysDictTypeService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 字典类型表(SysDictType)表控制层
 *
 * @author fjq
 * @since 2020-03-23 22:43:48
 */
@RestController
@RequestMapping("sysDictType")
public class SysDictTypeController {

    private final SysDictTypeService sysDictTypeService;

    public SysDictTypeController(SysDictTypeService sysDictTypeService) {
        this.sysDictTypeService = sysDictTypeService;
    }

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param sysDictType 查询实体
     * @return 所有数据
     */
    @GetMapping
    public HttpResult selectAll(Page<SysDictType> page, SysDictType sysDictType) {
        return HttpResult.ok(this.sysDictTypeService.page(page,
                Wrappers.<SysDictType>lambdaQuery().like(
                        StrUtil.isNotBlank(sysDictType.getDictName()),
                        SysDictType::getDictName, sysDictType.getDictName())));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public HttpResult selectOne(@PathVariable Serializable id) {
        return HttpResult.ok(this.sysDictTypeService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param sysDictType 实体对象
     * @return 新增结果
     */
    @PostMapping
    public HttpResult insert(@RequestBody SysDictType sysDictType) {
        return HttpResult.ok(this.sysDictTypeService.save(sysDictType));
    }

    /**
     * 修改数据
     *
     * @param sysDictType 实体对象
     * @return 修改结果
     */
    @PutMapping
    public HttpResult update(@RequestBody SysDictType sysDictType) {

        return HttpResult.ok(this.sysDictTypeService.updateById(sysDictType));
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
        return HttpResult.ok(this.sysDictTypeService.removeByIds(ids));
    }
}
package me.fjq.system.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.fjq.core.HttpResult;
import me.fjq.system.entity.SysOperLog;
import me.fjq.system.service.SysOperLogService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 操作日志记录(SysOperLog)表控制层
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@RestController
@RequestMapping("sysOperLog")
public class SysOperLogController {
    /**
     * 服务对象
     */
    @Resource
    private SysOperLogService sysOperLogService;

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param sysOperLog 查询实体
     * @return 所有数据
     */
    @GetMapping
    public HttpResult selectAll(Page<SysOperLog> page, SysOperLog sysOperLog) {
        return HttpResult.ok(this.sysOperLogService.page(page, new QueryWrapper<>(sysOperLog)));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public HttpResult selectOne(@PathVariable Serializable id) {
        return HttpResult.ok(this.sysOperLogService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param sysOperLog 实体对象
     * @return 新增结果
     */
    @PostMapping
    public HttpResult insert(@RequestBody SysOperLog sysOperLog) {
        return HttpResult.ok(this.sysOperLogService.save(sysOperLog));
    }

    /**
     * 修改数据
     *
     * @param sysOperLog 实体对象
     * @return 修改结果
     */
    @PutMapping
    public HttpResult update(@RequestBody SysOperLog sysOperLog) {
        return HttpResult.ok(this.sysOperLogService.updateById(sysOperLog));
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
        return HttpResult.ok(this.sysOperLogService.removeByIds(ids));
    }
}

package me.fjq.monitor.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.fjq.core.HttpResult;
import me.fjq.monitor.entity.OperLog;
import me.fjq.monitor.service.OperLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 操作日志记录(OperLog)表控制层
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@RestController
@RequestMapping("/monitor/operlog")
public class OperLogController {

    private final OperLogService operLogService;

    public OperLogController(OperLogService operLogService) {
        this.operLogService = operLogService;
    }

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param operLog 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPerms('monitor:operlog:list')")
    public HttpResult selectAll(Page<OperLog> page, OperLog operLog) {
        return HttpResult.ok(this.operLogService.page(page, new QueryWrapper<>(operLog)));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPerms('monitor:operlog:query')")
    public HttpResult selectOne(@PathVariable Serializable id) {
        return HttpResult.ok(this.operLogService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param operLog 实体对象
     * @return 新增结果
     */
    @PostMapping
    @PreAuthorize("@ss.hasPerms('monitor:operlog:add')")
    public HttpResult insert(@RequestBody OperLog operLog) {
        return HttpResult.ok(this.operLogService.save(operLog));
    }

    /**
     * 修改数据
     *
     * @param operLog 实体对象
     * @return 修改结果
     */
    @PutMapping
    @PreAuthorize("@ss.hasPerms('monitor:operlog:edit')")
    public HttpResult update(@RequestBody OperLog operLog) {
        return HttpResult.ok(this.operLogService.updateById(operLog));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合 (comma-separated string)
     * @return 删除结果
     */
    @DeleteMapping("/{idList}")
    @PreAuthorize("@ss.hasPerms('monitor:operlog:remove')")
    public HttpResult delete(@PathVariable String idList) {
        List<Long> ids = Arrays.stream(idList.split(","))
            .map(Long::valueOf)
            .collect(Collectors.toList());
        return HttpResult.ok(this.operLogService.removeByIds(ids));
    }
}

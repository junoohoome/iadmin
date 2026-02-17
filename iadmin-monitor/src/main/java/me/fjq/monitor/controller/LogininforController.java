package me.fjq.monitor.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.fjq.core.HttpResult;
import me.fjq.monitor.entity.Logininfor;
import me.fjq.monitor.service.LogininforService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统访问记录(Logininfor)表控制层
 *
 * @author fjq
 * @since 2020-03-23 22:43:48
 */
@RestController
@RequestMapping("/monitor/logininfor")
public class LogininforController {

    private final LogininforService logininforService;

    public LogininforController(LogininforService logininforService) {
        this.logininforService = logininforService;
    }

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param logininfor 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPerms('monitor:logininfor:list')")
    public HttpResult selectAll(Page<Logininfor> page, Logininfor logininfor) {
        return HttpResult.ok(this.logininforService.page(page, new QueryWrapper<>(logininfor)));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPerms('monitor:logininfor:query')")
    public HttpResult selectOne(@PathVariable Serializable id) {
        return HttpResult.ok(this.logininforService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param logininfor 实体对象
     * @return 新增结果
     */
    @PostMapping
    @PreAuthorize("@ss.hasPerms('monitor:logininfor:add')")
    public HttpResult insert(@RequestBody Logininfor logininfor) {
        return HttpResult.ok(this.logininforService.save(logininfor));
    }

    /**
     * 修改数据
     *
     * @param logininfor 实体对象
     * @return 修改结果
     */
    @PutMapping
    @PreAuthorize("@ss.hasPerms('monitor:logininfor:edit')")
    public HttpResult update(@RequestBody Logininfor logininfor) {
        return HttpResult.ok(this.logininforService.updateById(logininfor));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合 (comma-separated string)
     * @return 删除结果
     */
    @DeleteMapping("/{idList}")
    @PreAuthorize("@ss.hasPerms('monitor:logininfor:remove')")
    public HttpResult delete(@PathVariable String idList) {
        List<Long> ids = Arrays.stream(idList.split(","))
            .map(Long::valueOf)
            .collect(Collectors.toList());
        return HttpResult.ok(this.logininforService.removeByIds(ids));
    }
}

package me.fjq.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.fjq.core.HttpResult;
import me.fjq.system.entity.SysLogininfor;
import me.fjq.system.service.SysLogininforService;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统访问记录(SysLogininfor)表控制层
 *
 * @author fjq
 * @since 2020-03-23 22:43:48
 */
@RestController
@RequestMapping("sysLogininfor")
public class SysLogininforController {

    private final SysLogininforService sysLogininforService;

    public SysLogininforController(SysLogininforService sysLogininforService) {
        this.sysLogininforService = sysLogininforService;
    }

    /**
     * 分页查询所有数据
     *
     * @param page 分页对象
     * @param sysLogininfor 查询实体
     * @return 所有数据
     */
    @GetMapping("/list")
    public HttpResult selectAll(Page<SysLogininfor> page, SysLogininfor sysLogininfor) {
        return HttpResult.ok(this.sysLogininforService.page(page, new QueryWrapper<>(sysLogininfor)));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public HttpResult selectOne(@PathVariable Serializable id) {
        return HttpResult.ok(this.sysLogininforService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param sysLogininfor 实体对象
     * @return 新增结果
     */
    @PostMapping
    public HttpResult insert(@RequestBody SysLogininfor sysLogininfor) {
        return HttpResult.ok(this.sysLogininforService.save(sysLogininfor));
    }

    /**
     * 修改数据
     *
     * @param sysLogininfor 实体对象
     * @return 修改结果
     */
    @PutMapping
    public HttpResult update(@RequestBody SysLogininfor sysLogininfor) {
        return HttpResult.ok(this.sysLogininforService.updateById(sysLogininfor));
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
        return HttpResult.ok(this.sysLogininforService.removeByIds(ids));
    }
}

package me.fjq.system.controller;


import cn.hutool.core.util.StrUtil;
import me.fjq.annotation.Log;
import me.fjq.core.HttpResult;
import me.fjq.system.entity.SysDictData;
import me.fjq.system.service.SysDictDataService;
import me.fjq.system.service.impl.SysDictDataServiceImpl;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 字典数据表(SysDictData)表控制层
 *
 * @author fjq
 * @since 2020-03-23 22:43:48
 */
@RestController
@RequestMapping("sysDictData")
public class SysDictDataController {

    private final SysDictDataServiceImpl sysDictDataService;

    public SysDictDataController(SysDictDataServiceImpl sysDictDataService) {
        this.sysDictDataService = sysDictDataService;
    }

    /**
     * 通过字典类型查询数据（带缓存）
     *
     * @param dictType 字典类型
     * @return 查询字典类型数据
     */
    @GetMapping("{dictType}")
    public HttpResult selectListByDictType(@PathVariable String dictType) {
        if (StrUtil.isBlank(dictType)) {
            return HttpResult.ok();
        }

        return HttpResult.ok(sysDictDataService.selectListByDictType(dictType));
    }

    /**
     * 新增数据
     *
     * @param sysDictData 实体对象
     * @return 新增结果
     */
    @PreAuthorize("@ss.hasPerms('system:dict:add')")
    @Log(title = "字典数据", businessType = 1)
    @PostMapping
    public HttpResult insert(@RequestBody SysDictData sysDictData) {
        return HttpResult.ok(sysDictDataService.save(sysDictData));
    }

    /**
     * 修改数据
     *
     * @param sysDictData 实体对象
     * @return 修改结果
     */
    @PreAuthorize("@ss.hasPerms('system:dict:edit')")
    @Log(title = "字典数据", businessType = 2)
    @PutMapping
    public HttpResult update(@RequestBody SysDictData sysDictData) {
        return HttpResult.ok(sysDictDataService.updateById(sysDictData));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合 (comma-separated string)
     * @return 删除结果
     */
    @PreAuthorize("@ss.hasPerms('system:dict:del')")
    @Log(title = "字典数据", businessType = 3)
    @DeleteMapping("{idList}")
    public HttpResult delete(@PathVariable String idList) {
        List<Long> ids = Arrays.stream(idList.split(","))
            .map(Long::valueOf)
            .collect(Collectors.toList());
        return HttpResult.ok(sysDictDataService.removeByIds(ids));
    }
}
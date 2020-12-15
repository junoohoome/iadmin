package me.fjq.system.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import me.fjq.core.HttpResult;
import me.fjq.system.entity.SysDictData;
import me.fjq.system.service.SysDictDataService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


/**
 * 字典数据表(SysDictData)表控制层
 *
 * @author fjq
 * @since 2020-03-23 22:43:48
 */
@RestController
@RequestMapping("sysDictData")
public class SysDictDataController {
    /**
     * 服务对象
     */
    @Resource
    private SysDictDataService sysDictDataService;

    /**
     * 通过字典类型查询数据
     *
     * @param dictType 字典类型
     * @return 查询字典类型数据
     */
    @GetMapping("{dictType}")
    public HttpResult selectListByDictType(@PathVariable String dictType) {
        if (StrUtil.isBlank(dictType)) {
            return HttpResult.ok();
        }

        return HttpResult.ok(this.sysDictDataService.list(
                Wrappers.<SysDictData>lambdaQuery().eq(SysDictData::getDictType, dictType)
        ));
    }

    /**
     * 新增数据
     *
     * @param sysDictData 实体对象
     * @return 新增结果
     */
    @PostMapping
    public HttpResult insert(@RequestBody SysDictData sysDictData) {
        return HttpResult.ok(this.sysDictDataService.save(sysDictData));
    }

    /**
     * 修改数据
     *
     * @param sysDictData 实体对象
     * @return 修改结果
     */
    @PutMapping
    public HttpResult update(@RequestBody SysDictData sysDictData) {
        return HttpResult.ok(this.sysDictDataService.updateById(sysDictData));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @DeleteMapping
    public HttpResult delete(@RequestParam("idList") List<Long> idList) {
        return HttpResult.ok(this.sysDictDataService.removeByIds(idList));
    }

}
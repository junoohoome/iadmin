package me.fjq.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import me.fjq.cache.MultiLevelCacheService;
import me.fjq.system.entity.SysDictData;
import me.fjq.system.mapper.SysDictDataMapper;
import me.fjq.system.service.SysDictDataService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典数据表(SysDictData)表服务实现类
 *
 * @author fjq
 * @since 2020-03-23 22:43:48
 */
@RequiredArgsConstructor
@Service("sysDictDataService")
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

    private final MultiLevelCacheService cacheService;

    /**
     * 根据字典类型查询字典数据（带缓存）
     *
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    public List<SysDictData> selectListByDictType(String dictType) {
        return cacheService.getDictByType(dictType, () ->
                list(Wrappers.<SysDictData>lambdaQuery().eq(SysDictData::getDictType, dictType))
        );
    }

    /**
     * 保存字典数据并清除缓存
     */
    @Override
    public boolean save(SysDictData entity) {
        boolean result = super.save(entity);
        if (result) {
            cacheService.evictDict(entity.getDictType());
        }
        return result;
    }

    /**
     * 更新字典数据并清除缓存
     */
    @Override
    public boolean updateById(SysDictData entity) {
        boolean result = super.updateById(entity);
        if (result) {
            cacheService.evictDict(entity.getDictType());
        }
        return result;
    }

    /**
     * 删除字典数据并清除缓存
     */
    @Override
    public boolean removeById(SysDictData entity) {
        boolean result = super.removeById(entity);
        if (result) {
            cacheService.evictDict(entity.getDictType());
        }
        return result;
    }
}

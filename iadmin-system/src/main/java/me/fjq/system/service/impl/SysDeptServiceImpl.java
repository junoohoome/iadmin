package me.fjq.system.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import me.fjq.cache.MultiLevelCacheService;
import me.fjq.system.entity.SysDept;
import me.fjq.system.mapper.SysDeptMapper;
import me.fjq.system.service.SysDeptService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 部门表(SysDept)表服务实现类
 *
 * @author fjq
 * @since 2025-02-13
 */
@RequiredArgsConstructor
@Service("sysDeptService")
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    private final MultiLevelCacheService cacheService;

    /**
     * 查询部门树（带缓存）
     *
     * @return 部门列表
     */
    public List<SysDept> selectDeptTree() {
        return cacheService.getDeptTree(() ->
                list(Wrappers.<SysDept>lambdaQuery().orderByAsc(SysDept::getOrderNum))
        );
    }

    /**
     * 保存部门并清除缓存
     */
    @Override
    public boolean save(SysDept entity) {
        boolean result = super.save(entity);
        if (result) {
            cacheService.evictDeptTree();
        }
        return result;
    }

    /**
     * 更新部门并清除缓存
     */
    @Override
    public boolean updateById(SysDept entity) {
        boolean result = super.updateById(entity);
        if (result) {
            cacheService.evictDeptTree();
        }
        return result;
    }

    /**
     * 删除部门并清除缓存
     */
    @Override
    public boolean removeById(SysDept entity) {
        boolean result = super.removeById(entity);
        if (result) {
            cacheService.evictDeptTree();
        }
        return result;
    }
}

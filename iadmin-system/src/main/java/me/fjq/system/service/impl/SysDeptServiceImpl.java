package me.fjq.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import me.fjq.system.entity.SysDept;
import me.fjq.system.mapper.SysDeptMapper;
import me.fjq.system.service.SysDeptService;
import org.springframework.stereotype.Service;

/**
 * 部门表(SysDept)表服务实现类
 *
 * @author fjq
 * @since 2025-02-13
 */
@AllArgsConstructor
@Service("sysDeptService")
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    private final SysDeptMapper sysDeptMapper;
}

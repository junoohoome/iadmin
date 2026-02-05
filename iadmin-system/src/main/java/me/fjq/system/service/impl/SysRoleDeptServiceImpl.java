package me.fjq.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import me.fjq.system.entity.SysRoleDept;
import me.fjq.system.mapper.SysRoleDeptMapper;
import me.fjq.system.service.SysRoleDeptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色与部门关联表 服务实现类
 *
 * @author fjq
 * @since 2025-02-05
 */
@Service
@AllArgsConstructor
public class SysRoleDeptServiceImpl extends ServiceImpl<SysRoleDeptMapper, SysRoleDept> implements SysRoleDeptService {

    private final SysRoleDeptMapper roleDeptMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRoleDataScope(Long roleId, List<Long> deptIds) {
        // 先删除旧的关联关系
        roleDeptMapper.deleteByRoleId(roleId);

        // 如果部门ID列表为空，直接返回
        if (CollectionUtil.isEmpty(deptIds)) {
            return true;
        }

        // 构建新的关联关系
        List<SysRoleDept> roleDeptList = new ArrayList<>();
        for (Long deptId : deptIds) {
            SysRoleDept roleDept = SysRoleDept.builder()
                    .roleId(roleId)
                    .deptId(deptId)
                    .build();
            roleDeptList.add(roleDept);
        }

        // 批量插入
        return roleDeptMapper.batchInsert(roleDeptList) > 0;
    }

    @Override
    public List<Long> selectDeptIdsByRoleId(Long roleId) {
        return roleDeptMapper.selectDeptIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByRoleId(Long roleId) {
        return roleDeptMapper.deleteByRoleId(roleId) > 0;
    }

}

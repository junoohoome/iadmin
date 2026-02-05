package me.fjq.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import me.fjq.system.entity.SysRoleDept;

import java.util.List;

/**
 * 角色与部门关联表 服务接口
 *
 * @author fjq
 * @since 2025-02-05
 */
public interface SysRoleDeptService extends IService<SysRoleDept> {

    /**
     * 保存角色数据权限配置
     *
     * @param roleId  角色ID
     * @param deptIds 部门ID列表
     * @return 是否成功
     */
    boolean saveRoleDataScope(Long roleId, List<Long> deptIds);

    /**
     * 根据角色ID查询部门ID列表
     *
     * @param roleId 角色ID
     * @return 部门ID列表
     */
    List<Long> selectDeptIdsByRoleId(Long roleId);

    /**
     * 根据角色ID删除关联
     *
     * @param roleId 角色ID
     * @return 是否成功
     */
    boolean deleteByRoleId(Long roleId);

}

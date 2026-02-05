package me.fjq.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.fjq.system.entity.SysRoleDept;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色与部门关联表 Mapper
 *
 * @author fjq
 * @since 2025-02-05
 */
public interface SysRoleDeptMapper extends BaseMapper<SysRoleDept> {

    /**
     * 根据角色ID查询部门ID列表
     *
     * @param roleId 角色ID
     * @return 部门ID列表
     */
    @Select("SELECT dept_id FROM sys_role_dept WHERE role_id = #{roleId}")
    List<Long> selectDeptIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色ID删除关联
     *
     * @param roleId 角色ID
     * @return 删除行数
     */
    @Delete("DELETE FROM sys_role_dept WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量插入角色部门关联
     *
     * @param roleDeptList 角色部门关联列表
     * @return 插入行数
     */
    int batchInsert(@Param("list") List<SysRoleDept> roleDeptList);

}

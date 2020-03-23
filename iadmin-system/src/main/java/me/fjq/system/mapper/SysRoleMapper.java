package me.fjq.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.fjq.system.entity.SysRole;

import java.util.List;


/**
 * 角色信息表(SysRole)表数据库访问层
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> selectRolePermissionByUserId(Long userId);

}
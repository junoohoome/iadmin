package me.fjq.system.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.fjq.system.domain.SysRole;

import java.util.List;

/**
 * 角色表 数据层
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


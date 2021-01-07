package me.fjq.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import me.fjq.system.entity.SysRole;

import java.util.List;
import java.util.Set;


/**
 * 角色信息表(SysRole)表服务接口
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 根据用户ID查询角色
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    Set<String> selectRolePermsByUserId(Long userId);

    List<Long> selectRoleMenuListByRoleId(Long roleId);

    void updatePermissions(Long roleId, String menuIds);

}
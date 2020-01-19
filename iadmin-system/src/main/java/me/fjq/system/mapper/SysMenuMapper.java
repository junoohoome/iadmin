package me.fjq.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.fjq.system.domain.SysMenu;

import java.util.List;

/**
 * 菜单表 数据层
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<String> selectMenuPermsByUserId(Long userId);


}
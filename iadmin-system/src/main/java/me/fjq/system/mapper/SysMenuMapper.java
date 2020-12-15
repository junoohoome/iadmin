package me.fjq.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.fjq.system.entity.SysMenu;

import java.util.List;

/**
 * 菜单权限表(SysMenu)表数据库访问层
 *
 * @author fjq
 * @since 2020-03-23 22:43:48
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<String> selectMenuPermsByUserId(Long userId);

    /**
     * 根据用户ID查询菜单
     *
     * @param menuName       菜单名称
     * @param isRouterSelect 是否是路由菜单查询
     * @return 菜单列表
     */
    List<SysMenu> selectMenuTreeAll(String menuName, Boolean isRouterSelect);

    /**
     * 根据用户ID查询菜单
     *
     * @param userId         用户ID
     * @param menuName       菜单名称
     * @param isRouterSelect 是否是路由菜单查询
     * @return 菜单列表
     */
    List<SysMenu> selectMenuTreeByUserId(Long userId, String menuName, Boolean isRouterSelect);

}
package me.fjq.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import me.fjq.system.entity.SysMenu;
import me.fjq.system.vo.RouterVo;

import java.util.List;
import java.util.Set;

;


/**
 * 菜单权限表(SysMenu)表服务接口
 *
 * @author fjq
 * @since 2020-03-23 22:43:48
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    Set<String> selectMenuPermsByUserId(Long userId);

    /**
     * 根据用户ID查询菜单树信息
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    List<SysMenu> selectMenuTreeByUserId(Long userId);

    /**
     * 构建前端路由所需要的菜单
     *
     * @param menus 菜单列表
     * @return 路由列表
     */
    List<RouterVo> buildMenus(List<SysMenu> menus);

}
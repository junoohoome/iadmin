package me.fjq.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.fjq.system.mapper.SysRoleMenuMapper;
import me.fjq.system.entity.SysRoleMenu;
import me.fjq.system.service.SysRoleMenuService;
import org.springframework.stereotype.Service;

/**
 * 角色和菜单关联表(SysRoleMenu)表服务实现类
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@Service("sysRoleMenuService")
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements SysRoleMenuService {

}
package me.fjq.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.fjq.system.mapper.SysUserRoleMapper;
import me.fjq.system.entity.SysUserRole;
import me.fjq.system.service.SysUserRoleService;
import org.springframework.stereotype.Service;



/**
 * 用户和角色关联表(SysUserRole)表服务实现类
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@Service("sysUserRoleService")
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

}
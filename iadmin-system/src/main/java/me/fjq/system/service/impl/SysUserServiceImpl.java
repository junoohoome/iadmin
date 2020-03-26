package me.fjq.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import me.fjq.system.entity.SysUser;
import me.fjq.system.mapper.SysUserMapper;
import me.fjq.system.query.SysUserQuery;
import me.fjq.system.service.SysUserService;
import me.fjq.system.vo.system.SysUserVo;
import org.springframework.stereotype.Service;

/**
 * 用户信息表(SysUser)表服务实现类
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@AllArgsConstructor
@Service("sysUserService")
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper sysUserMapper;

    @Override
    public Page<SysUserVo> selectPage(Page page, SysUserQuery query) {
        return page.setRecords(this.sysUserMapper.selectPage(page, query));
    }
}
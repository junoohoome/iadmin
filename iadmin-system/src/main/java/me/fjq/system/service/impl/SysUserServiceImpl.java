package me.fjq.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.cache.MultiLevelCacheService;
import me.fjq.system.entity.SysUser;
import me.fjq.system.mapper.SysUserMapper;
import me.fjq.system.query.SysUserQuery;
import me.fjq.system.service.SysUserService;
import me.fjq.system.vo.system.SysUserVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户信息表(SysUser)表服务实现类
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@Slf4j
@AllArgsConstructor
@Service("sysUserService")
@Transactional(rollbackFor = Exception.class)
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final MultiLevelCacheService cacheService;

    @Override
    public Page<SysUserVo> selectPage(Page page, SysUserQuery query) {
        return this.sysUserMapper.selectPage(page, query);
    }

    /**
     * 更新用户信息
     *
     * @param user 用户实体
     * @return 更新结果
     */
    @Override
    public boolean updateUser(SysUser user) {
        boolean result = this.updateById(user);
        if (result && user.getUserId() != null) {
            // 清除用户缓存
            cacheService.evictUserAll(user.getUserId());
            log.debug("用户信息更新，已清除缓存: userId={}", user.getUserId());
        }
        return result;
    }
}

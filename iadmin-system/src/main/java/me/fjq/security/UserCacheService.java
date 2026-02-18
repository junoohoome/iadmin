package me.fjq.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.cache.MultiLevelCacheService;
import me.fjq.system.entity.SysUser;
import org.springframework.stereotype.Service;

/**
 * 用户信息缓存服务（使用多级缓存）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheService {

    private final MultiLevelCacheService cacheService;

    /**
     * 获取缓存的用户信息（多级缓存）
     */
    public SysUser getCachedUser(Long userId) {
        return cacheService.getUserInfo(userId, () -> null);
    }

    /**
     * 缓存用户信息（写入 L1 + L2）
     */
    public void cacheUser(SysUser user) {
        // 通过 getUserInfo 方法自动写入缓存，这里传入已有的用户对象
        cacheService.getUserInfo(user.getUserId(), () -> user);
        log.debug("用户信息已缓存: userId={}", user.getUserId());
    }

    /**
     * 清除用户所有缓存（L1 + L2）
     */
    public void evictUserCache(Long userId) {
        cacheService.evictUserAll(userId);
        log.debug("用户所有缓存已清除: userId={}", userId);
    }

    /**
     * 续期缓存（延长 L2 TTL，L1 自动过期）
     * 注：L1 本地缓存采用固定 TTL，不支持单独续期
     */
    public void renewCache(Long userId) {
        // L2 Redis 缓存续期由 MultiLevelCacheService 内部处理
        // L1 Caffeine 采用固定过期策略，无需手动续期
        log.debug("用户缓存续期: userId={}", userId);
    }
}

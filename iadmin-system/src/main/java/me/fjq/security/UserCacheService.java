package me.fjq.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.system.entity.SysUser;
import me.fjq.utils.RedisUtils;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 用户信息缓存服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheService {

    private static final String USER_CACHE_PREFIX = "user:cache:";
    private static final String USER_ROLES_PREFIX = "user:roles:";
    private static final String USER_PERMISSIONS_PREFIX = "user:permissions:";
    private static final long CACHE_TTL = 30 * 60; // 30分钟

    private final RedisUtils redisUtils;

    /**
     * 获取缓存的用户信息
     */
    public SysUser getCachedUser(Long userId) {
        try {
            String key = USER_CACHE_PREFIX + userId;
            return (SysUser) redisUtils.get(key);
        } catch (Exception e) {
            log.warn("获取用户缓存失败: userId={}, error={}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 缓存用户信息
     */
    public void cacheUser(SysUser user) {
        try {
            String key = USER_CACHE_PREFIX + user.getUserId();
            redisUtils.set(key, user, CACHE_TTL, TimeUnit.SECONDS);
            log.debug("用户信息已缓存: userId={}", user.getUserId());
        } catch (Exception e) {
            log.warn("缓存用户信息失败: userId={}, error={}", user.getUserId(), e.getMessage());
        }
    }

    /**
     * 获取缓存的权限列表
     */
    @SuppressWarnings("unchecked")
    public Set<String> getCachedPermissions(Long userId) {
        try {
            String key = USER_PERMISSIONS_PREFIX + userId;
            return (Set<String>) redisUtils.get(key);
        } catch (Exception e) {
            log.warn("获取权限缓存失败: userId={}, error={}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 缓存权限列表
     */
    public void cachePermissions(Long userId, Set<String> permissions) {
        try {
            String key = USER_PERMISSIONS_PREFIX + userId;
            redisUtils.set(key, permissions, CACHE_TTL, TimeUnit.SECONDS);
            log.debug("权限信息已缓存: userId={}, count={}", userId, permissions.size());
        } catch (Exception e) {
            log.warn("缓存权限信息失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 清除用户所有缓存
     */
    public void evictUserCache(Long userId) {
        try {
            redisUtils.del(USER_CACHE_PREFIX + userId);
            redisUtils.del(USER_ROLES_PREFIX + userId);
            redisUtils.del(USER_PERMISSIONS_PREFIX + userId);
            log.debug("用户缓存已清除: userId={}", userId);
        } catch (Exception e) {
            log.warn("清除用户缓存失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 续期缓存（延长TTL）
     */
    public void renewCache(Long userId) {
        try {
            redisUtils.expire(USER_CACHE_PREFIX + userId, CACHE_TTL);
            redisUtils.expire(USER_ROLES_PREFIX + userId, CACHE_TTL);
            redisUtils.expire(USER_PERMISSIONS_PREFIX + userId, CACHE_TTL);
            log.debug("用户缓存已续期: userId={}", userId);
        } catch (Exception e) {
            log.warn("续期用户缓存失败: userId={}, error={}", userId, e.getMessage());
        }
    }
}

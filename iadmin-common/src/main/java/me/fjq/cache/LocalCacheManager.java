package me.fjq.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 本地缓存管理器 (L1 Cache)
 * 基于 Caffeine 实现高性能本地缓存
 *
 * @author fjq
 */
@Slf4j
@Component
public class LocalCacheManager {

    /**
     * 用户权限缓存
     * 最大 10000 条，5 分钟过期
     */
    private final Cache<String, Set<String>> permissionsCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()
            .build();

    /**
     * 用户菜单缓存
     * 最大 1000 条，5 分钟过期
     */
    private final Cache<String, Object> menuCache = Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()
            .build();

    /**
     * 用户信息缓存
     * 最大 10000 条，5 分钟过期
     */
    private final Cache<String, Object> userInfoCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()
            .build();

    /**
     * 字典数据缓存
     * 最大 500 条，10 分钟过期
     */
    private final Cache<String, Object> dictCache = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .recordStats()
            .build();

    /**
     * 部门树缓存
     * 最大 100 条，10 分钟过期
     */
    private final Cache<String, Object> deptCache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .recordStats()
            .build();

    /**
     * 角色列表缓存
     * 最大 100 条，10 分钟过期
     */
    private final Cache<String, Object> roleCache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .recordStats()
            .build();

    // ==================== 权限缓存操作 ====================

    /**
     * 获取缓存的权限
     */
    public Set<String> getPermissions(Long userId) {
        return permissionsCache.getIfPresent(buildPermsKey(userId));
    }

    /**
     * 缓存权限
     */
    public void putPermissions(Long userId, Set<String> permissions) {
        permissionsCache.put(buildPermsKey(userId), permissions);
        log.debug("L1缓存权限: userId={}", userId);
    }

    /**
     * 清除权限缓存
     */
    public void evictPermissions(Long userId) {
        permissionsCache.invalidate(buildPermsKey(userId));
        log.debug("L1清除权限缓存: userId={}", userId);
    }

    // ==================== 菜单缓存操作 ====================

    /**
     * 获取缓存的菜单
     */
    @SuppressWarnings("unchecked")
    public <T> T getMenus(Long userId) {
        return (T) menuCache.getIfPresent(buildMenuKey(userId));
    }

    /**
     * 缓存菜单
     */
    public void putMenus(Long userId, Object menus) {
        menuCache.put(buildMenuKey(userId), menus);
        log.debug("L1缓存菜单: userId={}", userId);
    }

    /**
     * 清除菜单缓存
     */
    public void evictMenus(Long userId) {
        menuCache.invalidate(buildMenuKey(userId));
        log.debug("L1清除菜单缓存: userId={}", userId);
    }

    /**
     * 清除所有菜单缓存
     */
    public void evictAllMenus() {
        menuCache.invalidateAll();
        log.info("L1清除所有菜单缓存");
    }

    // ==================== 用户信息缓存操作 ====================

    /**
     * 获取缓存的用户信息
     */
    @SuppressWarnings("unchecked")
    public <T> T getUserInfo(Long userId) {
        return (T) userInfoCache.getIfPresent(buildUserInfoKey(userId));
    }

    /**
     * 缓存用户信息
     */
    public void putUserInfo(Long userId, Object userInfo) {
        userInfoCache.put(buildUserInfoKey(userId), userInfo);
        log.debug("L1缓存用户信息: userId={}", userId);
    }

    /**
     * 清除用户信息缓存
     */
    public void evictUserInfo(Long userId) {
        userInfoCache.invalidate(buildUserInfoKey(userId));
        log.debug("L1清除用户信息缓存: userId={}", userId);
    }

    // ==================== 字典缓存操作 ====================

    /**
     * 获取缓存的字典数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getDict(String dictType) {
        return (T) dictCache.getIfPresent(buildDictKey(dictType));
    }

    /**
     * 缓存字典数据
     */
    public void putDict(String dictType, Object dictData) {
        dictCache.put(buildDictKey(dictType), dictData);
        log.debug("L1缓存字典: dictType={}", dictType);
    }

    /**
     * 清除字典缓存
     */
    public void evictDict(String dictType) {
        dictCache.invalidate(buildDictKey(dictType));
        log.debug("L1清除字典缓存: dictType={}", dictType);
    }

    /**
     * 清除所有字典缓存
     */
    public void evictDictAll() {
        dictCache.invalidateAll();
        log.info("L1清除所有字典缓存");
    }

    // ==================== 部门缓存操作 ====================

    /**
     * 获取缓存的部门树
     */
    @SuppressWarnings("unchecked")
    public <T> T getDeptTree() {
        return (T) deptCache.getIfPresent(CacheKeys.DEPT_TREE);
    }

    /**
     * 缓存部门树
     */
    public void putDeptTree(Object deptTree) {
        deptCache.put(CacheKeys.DEPT_TREE, deptTree);
        log.debug("L1缓存部门树");
    }

    /**
     * 清除部门缓存
     */
    public void evictDeptTree() {
        deptCache.invalidate(CacheKeys.DEPT_TREE);
        log.debug("L1清除部门树缓存");
    }

    // ==================== 角色缓存操作 ====================

    /**
     * 获取缓存的角色列表
     */
    @SuppressWarnings("unchecked")
    public <T> T getRoleList() {
        return (T) roleCache.getIfPresent(CacheKeys.ROLE_LIST);
    }

    /**
     * 缓存角色列表
     */
    public void putRoleList(Object roleList) {
        roleCache.put(CacheKeys.ROLE_LIST, roleList);
        log.debug("L1缓存角色列表");
    }

    /**
     * 清除角色缓存
     */
    public void evictRoleList() {
        roleCache.invalidate(CacheKeys.ROLE_LIST);
        log.debug("L1清除角色列表缓存");
    }

    // ==================== 缓存常量 ====================

    private static class CacheKeys {
        static final String DEPT_TREE = "dept:tree";
        static final String ROLE_LIST = "role:list";
    }

    // ==================== 用户全量缓存清除 ====================

    /**
     * 清除用户所有相关缓存
     */
    public void evictUserAll(Long userId) {
        evictPermissions(userId);
        evictMenus(userId);
        evictUserInfo(userId);
        log.info("L1清除用户所有缓存: userId={}", userId);
    }

    // ==================== 缓存统计 ====================

    /**
     * 获取权限缓存统计
     */
    public String getPermissionsCacheStats() {
        var stats = permissionsCache.stats();
        return String.format("权限缓存 - 命中率: %.2f%%, 请求数: %d, 命中数: %d, 未命中数: %d",
                stats.hitRate() * 100, stats.requestCount(), stats.hitCount(), stats.missCount());
    }

    /**
     * 获取菜单缓存统计
     */
    public String getMenuCacheStats() {
        var stats = menuCache.stats();
        return String.format("菜单缓存 - 命中率: %.2f%%, 请求数: %d, 命中数: %d, 未命中数: %d",
                stats.hitRate() * 100, stats.requestCount(), stats.hitCount(), stats.missCount());
    }

    // ==================== Key 构建方法 ====================

    private String buildPermsKey(Long userId) {
        return "perms:" + userId;
    }

    private String buildMenuKey(Long userId) {
        return "menus:" + userId;
    }

    private String buildUserInfoKey(Long userId) {
        return "userInfo:" + userId;
    }

    private String buildDictKey(String dictType) {
        return "dict:" + dictType;
    }
}

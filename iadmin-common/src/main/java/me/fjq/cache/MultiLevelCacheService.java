package me.fjq.cache;

import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.constant.Constants;
import me.fjq.utils.RedisUtils;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 多级缓存服务 (L1 Caffeine + L2 Redis)
 *
 * @author fjq
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MultiLevelCacheService {

    private final LocalCacheManager localCache;
    private final RedisUtils redisUtils;

    // ==================== 权限缓存 ====================

    /**
     * 获取用户权限（多级缓存）
     *
     * @param userId       用户ID
     * @param dbSupplier   数据库查询函数（缓存未命中时调用）
     * @return 权限集合
     */
    @SuppressWarnings("unchecked")
    public Set<String> getPermissions(Long userId, Supplier<Set<String>> dbSupplier) {
        // 1. 查 L1 本地缓存
        Set<String> perms = localCache.getPermissions(userId);
        if (CollectionUtil.isNotEmpty(perms)) {
            log.debug("L1缓存命中-权限: userId={}", userId);
            return perms;
        }

        // 2. 查 L2 Redis
        String cacheKey = Constants.USER_PERMISSIONS_KEY + userId;
        try {
            Object cached = redisUtils.get(cacheKey);
            if (cached instanceof Set) {
                perms = (Set<String>) cached;
                if (CollectionUtil.isNotEmpty(perms)) {
                    log.debug("L2缓存命中-权限: userId={}", userId);
                    // 回填 L1
                    localCache.putPermissions(userId, perms);
                    return perms;
                }
            }
        } catch (Exception e) {
            log.warn("读取Redis权限缓存失败: userId={}, error={}", userId, e.getMessage());
        }

        // 3. 查数据库
        perms = dbSupplier.get();
        if (CollectionUtil.isEmpty(perms)) {
            return perms;
        }

        // 4. 写入 L2 Redis（带随机偏移防止雪崩）
        try {
            long expireTime = Constants.USER_PERMISSIONS_EXPIRE_TIME + (long) (Math.random() * 5);
            redisUtils.set(cacheKey, perms, expireTime, TimeUnit.MINUTES);
            log.debug("L2缓存写入-权限: userId={}, expireTime={}min", userId, expireTime);
        } catch (Exception e) {
            log.warn("写入Redis权限缓存失败: userId={}, error={}", userId, e.getMessage());
        }

        // 5. 写入 L1 本地缓存
        localCache.putPermissions(userId, perms);

        return perms;
    }

    /**
     * 清除用户权限缓存（L1 + L2）
     */
    public void evictPermissions(Long userId) {
        // 清除 L1
        localCache.evictPermissions(userId);
        // 清除 L2
        try {
            redisUtils.del(Constants.USER_PERMISSIONS_KEY + userId);
            log.debug("清除权限缓存: userId={}", userId);
        } catch (Exception e) {
            log.warn("清除Redis权限缓存失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    // ==================== 菜单缓存 ====================

    /**
     * 获取用户菜单（多级缓存）
     *
     * @param userId     用户ID
     * @param dbSupplier 数据库查询函数
     * @param <T>        返回类型
     * @return 菜单数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getMenus(Long userId, Supplier<T> dbSupplier) {
        // 1. 查 L1 本地缓存
        T menus = localCache.getMenus(userId);
        if (menus != null) {
            log.debug("L1缓存命中-菜单: userId={}", userId);
            return menus;
        }

        // 2. 查 L2 Redis
        String cacheKey = Constants.USER_MENUS_KEY + userId;
        try {
            Object cached = redisUtils.get(cacheKey);
            if (cached != null) {
                menus = (T) cached;
                log.debug("L2缓存命中-菜单: userId={}", userId);
                // 回填 L1
                localCache.putMenus(userId, menus);
                return menus;
            }
        } catch (Exception e) {
            log.warn("读取Redis菜单缓存失败: userId={}, error={}", userId, e.getMessage());
        }

        // 3. 查数据库
        menus = dbSupplier.get();
        if (menus == null) {
            return null;
        }

        // 4. 写入 L2 Redis
        try {
            long expireTime = Constants.CACHE_MENUS_EXPIRE_TIME + (long) (Math.random() * 5);
            redisUtils.set(cacheKey, menus, expireTime, TimeUnit.MINUTES);
            log.debug("L2缓存写入-菜单: userId={}, expireTime={}min", userId, expireTime);
        } catch (Exception e) {
            log.warn("写入Redis菜单缓存失败: userId={}, error={}", userId, e.getMessage());
        }

        // 5. 写入 L1 本地缓存
        localCache.putMenus(userId, menus);

        return menus;
    }

    /**
     * 清除用户菜单缓存（L1 + L2）
     */
    public void evictMenus(Long userId) {
        localCache.evictMenus(userId);
        try {
            redisUtils.del(Constants.USER_MENUS_KEY + userId);
            log.debug("清除菜单缓存: userId={}", userId);
        } catch (Exception e) {
            log.warn("清除Redis菜单缓存失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 清除所有菜单缓存（L1 + L2）
     */
    public void evictAllMenus() {
        // 清除 L1 所有菜单
        localCache.evictAllMenus();
        // 清除 L2 所有菜单（模糊匹配删除）
        try {
            java.util.List<String> keys = redisUtils.scan(Constants.USER_MENUS_KEY + "*");
            if (CollectionUtil.isNotEmpty(keys)) {
                for (String key : keys) {
                    redisUtils.del(key);
                }
            }
            log.info("清除所有菜单缓存: count={}", keys.size());
        } catch (Exception e) {
            log.warn("清除Redis所有菜单缓存失败: error={}", e.getMessage());
        }
    }

    // ==================== 用户信息缓存 ====================

    /**
     * 获取用户信息（多级缓存）
     */
    @SuppressWarnings("unchecked")
    public <T> T getUserInfo(Long userId, Supplier<T> dbSupplier) {
        // 1. 查 L1
        T userInfo = localCache.getUserInfo(userId);
        if (userInfo != null) {
            log.debug("L1缓存命中-用户信息: userId={}", userId);
            return userInfo;
        }

        // 2. 查 L2
        String cacheKey = Constants.USER_INFO_KEY + userId;
        try {
            Object cached = redisUtils.get(cacheKey);
            if (cached != null) {
                userInfo = (T) cached;
                log.debug("L2缓存命中-用户信息: userId={}", userId);
                localCache.putUserInfo(userId, userInfo);
                return userInfo;
            }
        } catch (Exception e) {
            log.warn("读取Redis用户信息缓存失败: userId={}, error={}", userId, e.getMessage());
        }

        // 3. 查数据库
        userInfo = dbSupplier.get();
        if (userInfo == null) {
            return null;
        }

        // 4. 写入 L2
        try {
            long expireTime = Constants.CACHE_USER_INFO_EXPIRE_TIME + (long) (Math.random() * 5);
            redisUtils.set(cacheKey, userInfo, expireTime, TimeUnit.MINUTES);
            log.debug("L2缓存写入-用户信息: userId={}", userId);
        } catch (Exception e) {
            log.warn("写入Redis用户信息缓存失败: userId={}, error={}", userId, e.getMessage());
        }

        // 5. 写入 L1
        localCache.putUserInfo(userId, userInfo);

        return userInfo;
    }

    /**
     * 清除用户信息缓存（L1 + L2）
     */
    public void evictUserInfo(Long userId) {
        localCache.evictUserInfo(userId);
        try {
            redisUtils.del(Constants.USER_INFO_KEY + userId);
            log.debug("清除用户信息缓存: userId={}", userId);
        } catch (Exception e) {
            log.warn("清除Redis用户信息缓存失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    // ==================== 用户全量缓存清除 ====================

    /**
     * 清除用户所有缓存（L1 + L2）
     */
    public void evictUserAll(Long userId) {
        localCache.evictUserAll(userId);
        try {
            redisUtils.del(
                    Constants.USER_PERMISSIONS_KEY + userId,
                    Constants.USER_MENUS_KEY + userId,
                    Constants.USER_INFO_KEY + userId
            );
            log.info("清除用户所有缓存: userId={}", userId);
        } catch (Exception e) {
            log.warn("清除Redis用户所有缓存失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    // ==================== 缓存统计 ====================

    /**
     * 获取缓存统计信息
     */
    public String getCacheStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 本地缓存统计 ===\n");
        sb.append(localCache.getPermissionsCacheStats()).append("\n");
        sb.append(localCache.getMenuCacheStats());
        return sb.toString();
    }

    // ==================== 字典缓存 ====================

    /**
     * 获取字典数据（多级缓存）
     *
     * @param dictType   字典类型
     * @param dbSupplier 数据库查询函数
     * @param <T>        返回类型
     * @return 字典数据列表
     */
    @SuppressWarnings("unchecked")
    public <T> T getDictByType(String dictType, Supplier<T> dbSupplier) {
        // 1. 查 L1 本地缓存
        T dictData = localCache.getDict(dictType);
        if (dictData != null) {
            log.debug("L1缓存命中-字典: dictType={}", dictType);
            return dictData;
        }

        // 2. 查 L2 Redis
        String cacheKey = Constants.DICT_KEY + dictType;
        try {
            Object cached = redisUtils.get(cacheKey);
            if (cached != null) {
                dictData = (T) cached;
                log.debug("L2缓存命中-字典: dictType={}", dictType);
                // 回填 L1
                localCache.putDict(dictType, dictData);
                return dictData;
            }
        } catch (Exception e) {
            log.warn("读取Redis字典缓存失败: dictType={}, error={}", dictType, e.getMessage());
        }

        // 3. 查数据库
        dictData = dbSupplier.get();
        if (dictData == null) {
            return null;
        }

        // 4. 写入 L2 Redis
        try {
            long expireTime = Constants.CACHE_DICT_EXPIRE_TIME + (long) (Math.random() * 5);
            redisUtils.set(cacheKey, dictData, expireTime, TimeUnit.MINUTES);
            log.debug("L2缓存写入-字典: dictType={}, expireTime={}min", dictType, expireTime);
        } catch (Exception e) {
            log.warn("写入Redis字典缓存失败: dictType={}, error={}", dictType, e.getMessage());
        }

        // 5. 写入 L1 本地缓存
        localCache.putDict(dictType, dictData);

        return dictData;
    }

    /**
     * 清除字典缓存（L1 + L2）
     */
    public void evictDict(String dictType) {
        localCache.evictDict(dictType);
        try {
            redisUtils.del(Constants.DICT_KEY + dictType);
            log.debug("清除字典缓存: dictType={}", dictType);
        } catch (Exception e) {
            log.warn("清除Redis字典缓存失败: dictType={}, error={}", dictType, e.getMessage());
        }
    }

    /**
     * 清除所有字典缓存（L1 + L2）
     */
    public void evictDictAll() {
        localCache.evictDictAll();
        try {
            java.util.List<String> keys = redisUtils.scan(Constants.DICT_KEY + "*");
            if (CollectionUtil.isNotEmpty(keys)) {
                for (String key : keys) {
                    redisUtils.del(key);
                }
            }
            log.info("清除所有字典缓存: count={}", keys != null ? keys.size() : 0);
        } catch (Exception e) {
            log.warn("清除Redis所有字典缓存失败: error={}", e.getMessage());
        }
    }

    // ==================== 部门缓存 ====================

    /**
     * 获取部门树（多级缓存）
     *
     * @param dbSupplier 数据库查询函数
     * @param <T>        返回类型
     * @return 部门树
     */
    @SuppressWarnings("unchecked")
    public <T> T getDeptTree(Supplier<T> dbSupplier) {
        // 1. 查 L1 本地缓存
        T deptTree = localCache.getDeptTree();
        if (deptTree != null) {
            log.debug("L1缓存命中-部门树");
            return deptTree;
        }

        // 2. 查 L2 Redis
        String cacheKey = Constants.DEPT_TREE_KEY;
        try {
            Object cached = redisUtils.get(cacheKey);
            if (cached != null) {
                deptTree = (T) cached;
                log.debug("L2缓存命中-部门树");
                // 回填 L1
                localCache.putDeptTree(deptTree);
                return deptTree;
            }
        } catch (Exception e) {
            log.warn("读取Redis部门树缓存失败: error={}", e.getMessage());
        }

        // 3. 查数据库
        deptTree = dbSupplier.get();
        if (deptTree == null) {
            return null;
        }

        // 4. 写入 L2 Redis
        try {
            long expireTime = Constants.CACHE_DEPT_EXPIRE_TIME + (long) (Math.random() * 5);
            redisUtils.set(cacheKey, deptTree, expireTime, TimeUnit.MINUTES);
            log.debug("L2缓存写入-部门树: expireTime={}min", expireTime);
        } catch (Exception e) {
            log.warn("写入Redis部门树缓存失败: error={}", e.getMessage());
        }

        // 5. 写入 L1 本地缓存
        localCache.putDeptTree(deptTree);

        return deptTree;
    }

    /**
     * 清除部门树缓存（L1 + L2）
     */
    public void evictDeptTree() {
        localCache.evictDeptTree();
        try {
            redisUtils.del(Constants.DEPT_TREE_KEY);
            log.debug("清除部门树缓存");
        } catch (Exception e) {
            log.warn("清除Redis部门树缓存失败: error={}", e.getMessage());
        }
    }

    // ==================== 角色缓存 ====================

    /**
     * 获取角色列表（多级缓存）
     *
     * @param dbSupplier 数据库查询函数
     * @param <T>        返回类型
     * @return 角色列表
     */
    @SuppressWarnings("unchecked")
    public <T> T getRoleList(Supplier<T> dbSupplier) {
        // 1. 查 L1 本地缓存
        T roleList = localCache.getRoleList();
        if (roleList != null) {
            log.debug("L1缓存命中-角色列表");
            return roleList;
        }

        // 2. 查 L2 Redis
        String cacheKey = Constants.ROLE_LIST_KEY;
        try {
            Object cached = redisUtils.get(cacheKey);
            if (cached != null) {
                roleList = (T) cached;
                log.debug("L2缓存命中-角色列表");
                // 回填 L1
                localCache.putRoleList(roleList);
                return roleList;
            }
        } catch (Exception e) {
            log.warn("读取Redis角色列表缓存失败: error={}", e.getMessage());
        }

        // 3. 查数据库
        roleList = dbSupplier.get();
        if (roleList == null) {
            return null;
        }

        // 4. 写入 L2 Redis
        try {
            long expireTime = Constants.CACHE_ROLE_EXPIRE_TIME + (long) (Math.random() * 5);
            redisUtils.set(cacheKey, roleList, expireTime, TimeUnit.MINUTES);
            log.debug("L2缓存写入-角色列表: expireTime={}min", expireTime);
        } catch (Exception e) {
            log.warn("写入Redis角色列表缓存失败: error={}", e.getMessage());
        }

        // 5. 写入 L1 本地缓存
        localCache.putRoleList(roleList);

        return roleList;
    }

    /**
     * 清除角色列表缓存（L1 + L2）
     */
    public void evictRoleList() {
        localCache.evictRoleList();
        try {
            redisUtils.del(Constants.ROLE_LIST_KEY);
            log.debug("清除角色列表缓存");
        } catch (Exception e) {
            log.warn("清除Redis角色列表缓存失败: error={}", e.getMessage());
        }
    }
}

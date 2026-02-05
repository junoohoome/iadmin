package me.fjq.system.controller;

import lombok.AllArgsConstructor;
import me.fjq.core.HttpResult;
import me.fjq.utils.RedisUtils;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 缓存监控 Controller
 *
 * @author fjq
 * @since 2025-02-05
 */
@RestController
@RequestMapping("/system/cache")
@AllArgsConstructor
public class CacheController {

    private final RedisTemplate<Object, Object> redisTemplate;
    private final RedisUtils redisUtils;

    /**
     * 获取缓存信息列表
     *
     * @return 缓存信息列表
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPerms('system:cache:list')")
    public HttpResult<List<Map<String, Object>>> list() {
        List<Map<String, Object>> cacheInfos = new ArrayList<>();

        redisTemplate.execute((RedisCallback<Void>) connection -> {
            ScanOptions options = ScanOptions.scanOptions().match("*").count(100).build();
            Cursor<byte[]> cursor = connection.scan(options);
            while (cursor.hasNext()) {
                String key = new String(cursor.next());
                Map<String, Object> cacheInfo = new HashMap<>();
                cacheInfo.put("key", key);
                cacheInfo.put("type", getType(key));
                cacheInfo.put("ttl", redisUtils.getExpire(key));
                cacheInfos.add(cacheInfo);
            }
            return null;
        });

        return HttpResult.ok(cacheInfos);
    }

    /**
     * 获取缓存详细信息
     *
     * @param key 缓存键
     * @return 缓存详细信息
     */
    @GetMapping("/info/{key}")
    @PreAuthorize("@ss.hasPerms('system:cache:query')")
    public HttpResult<Map<String, Object>> getCacheInfo(@PathVariable String key) {
        Map<String, Object> cacheInfo = new HashMap<>();

        Object value = redisUtils.get(key);
        cacheInfo.put("key", key);
        cacheInfo.put("value", value);
        cacheInfo.put("type", getType(key));
        cacheInfo.put("ttl", redisUtils.getExpire(key));
        cacheInfo.put("size", value != null ? value.toString().length() : 0);

        return HttpResult.ok(cacheInfo);
    }

    /**
     * 根据前缀搜索缓存键
     *
     * @param pattern 键名模式（支持通配符 *）
     * @return 匹配的键列表
     */
    @GetMapping("/keys")
    @PreAuthorize("@ss.hasPerms('system:cache:query')")
    public HttpResult<Set<String>> getKeys(@RequestParam(defaultValue = "*") String pattern) {
        Set<String> keys = new HashSet<>();
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();
            Cursor<byte[]> cursor = connection.scan(options);
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next()));
            }
            return null;
        });
        return HttpResult.ok(keys);
    }

    /**
     * 删除单个缓存
     *
     * @param key 缓存键
     * @return 操作结果
     */
    @DeleteMapping("/{key}")
    @PreAuthorize("@ss.hasPerms('system:cache:remove')")
    public HttpResult<Boolean> deleteCache(@PathVariable String key) {
        redisUtils.del(key);
        return HttpResult.ok(true);
    }

    /**
     * 批量删除缓存
     *
     * @param keys 缓存键列表
     * @return 删除的数量
     */
    @DeleteMapping("/batch")
    @PreAuthorize("@ss.hasPerms('system:cache:remove')")
    public HttpResult<Long> deleteBatch(@RequestBody List<String> keys) {
        redisUtils.del(keys.toArray(new String[0]));
        return HttpResult.ok((long) keys.size());
    }

    /**
     * 根据前缀批量删除缓存
     *
     * @param pattern 键名模式（支持通配符 *）
     * @return 删除的数量
     */
    @DeleteMapping("/pattern")
    @PreAuthorize("@ss.hasPerms('system:cache:remove')")
    public HttpResult<Integer> deleteByPattern(@RequestParam String pattern) {
        Set<String> keys = new HashSet<>();
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();
            Cursor<byte[]> cursor = connection.scan(options);
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next()));
            }
            return null;
        });

        if (!keys.isEmpty()) {
            redisUtils.del(keys.toArray(new String[0]));
        }

        return HttpResult.ok(keys.size());
    }

    /**
     * 清空所有缓存
     *
     * @return 操作结果
     */
    @DeleteMapping("/clear")
    @PreAuthorize("@ss.hasPerms('system:cache:remove')")
    public HttpResult<Boolean> clearCache() {
        Set<String> keys = new HashSet<>();
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            ScanOptions options = ScanOptions.scanOptions().match("*").count(1000).build();
            Cursor<byte[]> cursor = connection.scan(options);
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next()));
            }
            return null;
        });

        if (!keys.isEmpty()) {
            redisUtils.del(keys.toArray(new String[0]));
        }
        return HttpResult.ok(true);
    }

    /**
     * 获取缓存统计信息
     *
     * @return 缓存统计信息
     */
    @GetMapping("/stats")
    @PreAuthorize("@ss.hasPerms('system:cache:list')")
    public HttpResult<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 获取所有键的数量
        final Set<String> keys = new HashSet<>();
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            ScanOptions options = ScanOptions.scanOptions().match("*").count(1000).build();
            Cursor<byte[]> cursor = connection.scan(options);
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next()));
            }
            return null;
        });
        stats.put("totalKeys", keys.size());

        // 统计不同类型键的数量
        Map<String, Integer> keyTypes = new HashMap<>();
        for (String key : keys) {
            String type = getType(key);
            keyTypes.put(type, keyTypes.getOrDefault(type, 0) + 1);
        }
        stats.put("keyTypes", keyTypes);

        // 获取内存信息
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            Properties info = connection.info("memory");
            stats.put("memory", info);
            return null;
        });

        return HttpResult.ok(stats);
    }

    /**
     * 根据键获取数据类型
     *
     * @param key 缓存键
     * @return 数据类型
     */
    private String getType(String key) {
        return redisTemplate.execute((RedisCallback<String>) connection -> {
            byte[] keyBytes = key.getBytes();
            String type = connection.type(keyBytes).code();
            return type != null ? type : "none";
        });
    }

}

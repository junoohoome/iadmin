package me.fjq.system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.fjq.constant.RedisConstants;
import me.fjq.system.entity.OnlineUser;
import me.fjq.utils.RedisUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 在线用户服务
 *
 * @author fjq
 * @since 2025-02-05
 */
@Slf4j
@Service
public class OnlineUserService {

    private final RedisUtils redisUtils;
    private final ObjectMapper objectMapper;

    public OnlineUserService(RedisUtils redisUtils, ObjectMapper objectMapper) {
        this.redisUtils = redisUtils;
        this.objectMapper = objectMapper;
    }

    /**
     * 保存在线用户
     *
     * @param onlineUser 在线用户信息
     */
    public void saveOnlineUser(OnlineUser onlineUser) {
        try {
            String tokenKey = RedisConstants.ONLINE_TOKEN_KEY + onlineUser.token();
            String userJson = objectMapper.writeValueAsString(onlineUser);

            // 保存 token 对应的用户信息
            redisUtils.set(tokenKey, userJson, RedisConstants.ONLINE_TOKEN_EXPIRE_SECONDS);

            // 添加到在线用户集合
            redisUtils.sSet(RedisConstants.ONLINE_USERS_KEY, onlineUser.token());

            log.debug("保存在线用户: {}", onlineUser.username());
        } catch (JsonProcessingException e) {
            log.error("保存在线用户失败", e);
        }
    }

    /**
     * 更新最后访问时间
     *
     * @param token 登录令牌
     */
    public void updateLastAccessTime(String token) {
        try {
            String tokenKey = RedisConstants.ONLINE_TOKEN_KEY + token;
            Object userObj = redisUtils.get(tokenKey);

            if (userObj != null) {
                OnlineUser oldUser = objectMapper.readValue(userObj.toString(), OnlineUser.class);

                // 创建更新后的用户信息
                OnlineUser newUser = OnlineUser.builder()
                        .token(oldUser.token())
                        .userId(oldUser.userId())
                        .username(oldUser.username())
                        .nickName(oldUser.nickName())
                        .deptId(oldUser.deptId())
                        .deptName(oldUser.deptName())
                        .ipaddr(oldUser.ipaddr())
                        .loginLocation(oldUser.loginLocation())
                        .browser(oldUser.browser())
                        .os(oldUser.os())
                        .loginTime(oldUser.loginTime())
                        .lastAccessTime(LocalDateTime.now())
                        .build();

                String userJson = objectMapper.writeValueAsString(newUser);
                redisUtils.set(tokenKey, userJson, RedisConstants.ONLINE_TOKEN_EXPIRE_SECONDS);

                log.debug("更新最后访问时间: {}", oldUser.username());
            }
        } catch (JsonProcessingException e) {
            log.error("更新最后访问时间失败", e);
        }
    }

    /**
     * 删除在线用户
     *
     * @param token 登录令牌
     */
    public void deleteOnlineUser(String token) {
        String tokenKey = RedisConstants.ONLINE_TOKEN_KEY + token;

        // 删除 token 对应的用户信息
        redisUtils.del(tokenKey);

        // 从在线用户集合中移除
        redisUtils.setRemove(RedisConstants.ONLINE_USERS_KEY, token);

        log.debug("删除在线用户: {}", token);
    }

    /**
     * 获取所有在线用户（使用批量操作优化性能）
     *
     * @return 在线用户列表
     */
    public List<OnlineUser> getOnlineUsers() {
        List<OnlineUser> onlineUsers = new ArrayList<>();

        try {
            // 获取所有在线用户的 token
            Set<Object> tokens = redisUtils.sGet(RedisConstants.ONLINE_USERS_KEY);

            if (tokens != null && !tokens.isEmpty()) {
                // 使用 Redis multiGet 批量获取（替代原来的 N+1 查询）
                List<String> tokenKeys = tokens.stream()
                        .map(token -> RedisConstants.ONLINE_TOKEN_KEY + token)
                        .collect(java.util.stream.Collectors.toList());

                List<Object> userObjects = redisUtils.multiGet(tokenKeys);

                // 解析返回的用户数据
                for (int i = 0; i < tokenKeys.size() && i < userObjects.size(); i++) {
                    try {
                        Object userObj = userObjects.get(i);
                        if (userObj != null) {
                            OnlineUser user = objectMapper.readValue(userObj.toString(), OnlineUser.class);
                            onlineUsers.add(user);
                        }
                    } catch (JsonProcessingException e) {
                        Object token = tokens.toArray()[i];
                        log.error("解析在线用户数据失败, token: {}", token, e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取在线用户列表失败", e);
        }

        return onlineUsers;
    }

    /**
     * 检查 token 是否在线
     *
     * @param token 登录令牌
     * @return 是否在线
     */
    public boolean isOnline(String token) {
        String tokenKey = RedisConstants.ONLINE_TOKEN_KEY + token;
        return redisUtils.hasKey(tokenKey);
    }

    /**
     * 添加 token 到黑名单
     *
     * @param token 登录令牌
     */
    public void addToBlacklist(String token) {
        String blacklistKey = RedisConstants.BLACKLISTED_TOKEN_KEY + token;
        redisUtils.set(blacklistKey, "1", RedisConstants.BLACKLISTED_TOKEN_EXPIRE_SECONDS);

        // 同时删除在线用户
        deleteOnlineUser(token);

        log.debug("添加 token 到黑名单: {}", token);
    }

    /**
     * 检查 token 是否在黑名单中
     *
     * @param token 登录令牌
     * @return 是否在黑名单中
     */
    public boolean isBlacklisted(String token) {
        String blacklistKey = RedisConstants.BLACKLISTED_TOKEN_KEY + token;
        return redisUtils.hasKey(blacklistKey);
    }

    /**
     * 根据用户名强制下线（使用虚拟线程优化并发操作）
     *
     * @param username 用户名
     * @return 强制下线的用户数量
     */
    public int forceLogout(String username) {
        List<OnlineUser> onlineUsers = getOnlineUsers();
        List<OnlineUser> targetUsers = new ArrayList<>();

        // 筛选出需要下线的用户
        for (OnlineUser user : onlineUsers) {
            if (username.equals(user.username())) {
                targetUsers.add(user);
            }
        }

        if (targetUsers.isEmpty()) {
            return 0;
        }

        // 使用 Java 21 虚拟线程并发执行强制下线
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Boolean>> futures = new ArrayList<>();

            for (OnlineUser user : targetUsers) {
                Future<Boolean> future = executor.submit(() -> {
                    try {
                        addToBlacklist(user.token());
                        return true;
                    } catch (Exception e) {
                        log.error("强制下线失败, token: {}", user.token(), e);
                        return false;
                    }
                });
                futures.add(future);
            }

            // 统计成功数量
            int count = 0;
            for (Future<Boolean> future : futures) {
                try {
                    if (future.get()) {
                        count++;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    log.error("统计强制下线结果失败", e);
                }
            }

            log.info("强制下线用户: {}, 数量: {}", username, count);
            return count;
        }
    }

}

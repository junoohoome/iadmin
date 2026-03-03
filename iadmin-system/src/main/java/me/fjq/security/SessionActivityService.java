package me.fjq.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.utils.RedisUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 会话活动服务
 * <p>
 * 跟踪用户会话活动，实现空闲超时检测
 * </p>
 *
 * @author fjq
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionActivityService {

    private final RedisUtils redisUtils;

    /**
     * 用户最后活动时间缓存 Key 前缀
     */
    private static final String LAST_ACTIVITY_PREFIX = "session:activity:";

    /**
     * 默认空闲超时时间（分钟）
     */
    private static final long DEFAULT_IDLE_TIMEOUT_MINUTES = 30;

    /**
     * 记录用户活动
     *
     * @param userId 用户ID
     */
    public void recordActivity(Long userId) {
        if (userId == null) {
            return;
        }

        String key = LAST_ACTIVITY_PREFIX + userId;
        long now = System.currentTimeMillis();

        // 记录最后活动时间，设置过期时间为 2 倍超时时间
        redisUtils.set(key, now, DEFAULT_IDLE_TIMEOUT_MINUTES * 2, TimeUnit.MINUTES);
    }

    /**
     * 检查会话是否因空闲超时
     *
     * @param userId 用户ID
     * @return true 表示会话已超时
     */
    public boolean isSessionIdleTimeout(Long userId) {
        if (userId == null) {
            return true;
        }

        String key = LAST_ACTIVITY_PREFIX + userId;
        Object lastActivity = redisUtils.get(key);

        if (lastActivity == null) {
            // 没有活动记录，视为首次访问或已超时
            return false;
        }

        long lastActivityTime = Long.parseLong(lastActivity.toString());
        long idleTimeMs = System.currentTimeMillis() - lastActivityTime;
        long timeoutMs = DEFAULT_IDLE_TIMEOUT_MINUTES * 60 * 1000;

        return idleTimeMs > timeoutMs;
    }

    /**
     * 获取会话剩余有效时间（秒）
     *
     * @param userId 用户ID
     * @return 剩余秒数，-1 表示无记录
     */
    public long getRemainingIdleTime(Long userId) {
        if (userId == null) {
            return -1;
        }

        String key = LAST_ACTIVITY_PREFIX + userId;
        Object lastActivity = redisUtils.get(key);

        if (lastActivity == null) {
            return -1;
        }

        long lastActivityTime = Long.parseLong(lastActivity.toString());
        long idleTimeMs = System.currentTimeMillis() - lastActivityTime;
        long timeoutMs = DEFAULT_IDLE_TIMEOUT_MINUTES * 60 * 1000;
        long remainingMs = timeoutMs - idleTimeMs;

        return remainingMs > 0 ? remainingMs / 1000 : 0;
    }

    /**
     * 清除用户活动记录
     *
     * @param userId 用户ID
     */
    public void clearActivity(Long userId) {
        if (userId == null) {
            return;
        }

        String key = LAST_ACTIVITY_PREFIX + userId;
        redisUtils.del(key);
        log.debug("清除用户会话活动记录: userId={}", userId);
    }

    /**
     * 刷新用户活动时间（延长会话）
     *
     * @param userId 用户ID
     */
    public void refreshActivity(Long userId) {
        recordActivity(userId);
        log.debug("刷新用户会话活动时间: userId={}", userId);
    }
}

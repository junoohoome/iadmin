package me.fjq.aspect;

import lombok.extern.slf4j.Slf4j;
import me.fjq.annotation.Limiter;
import me.fjq.exception.BadRequestException;
import me.fjq.utils.IpUtils;
import me.fjq.utils.RedisUtils;
import me.fjq.utils.ServletUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流切面
 * <p>
 * 基于 Redis + Lua 脚本实现的令牌桶限流
 * 限制指定接口在指定时间窗口内的请求次数
 * </p>
 *
 * @author fjq
 */
@Slf4j
@Aspect
@Component
public class LimiterAspect {

    private final RedisUtils redisUtils;

    public LimiterAspect(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    /**
     * Lua 脚本：实现滑动窗口限流
     * KEYS[1]: 限流 key
     * ARGV[1]: 窗口大小（秒）
     * ARGV[2]: 最大请求数
     * ARGV[3]: 当前时间戳
     */
    private static final String LIMIT_SCRIPT =
            "local key = KEYS[1] " +
            "local window = tonumber(ARGV[1]) " +
            "local maxRequests = tonumber(ARGV[2]) " +
            "local now = tonumber(ARGV[3]) " +
            "local windowStart = now - window * 1000 " +
            // 移除窗口外的请求记录
            "redis.call('ZREMRANGEBYSCORE', key, 0, windowStart) " +
            // 获取当前窗口内的请求数
            "local current = redis.call('ZCARD', key) " +
            "if current >= maxRequests then " +
            "  return 0 " +
            "end " +
            // 添加当前请求
            "redis.call('ZADD', key, now, now .. '-' .. math.random()) " +
            // 设置过期时间
            "redis.call('EXPIRE', key, window) " +
            "return 1";

    @Around("@annotation(me.fjq.annotation.Limiter)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Limiter limiter = method.getAnnotation(Limiter.class);

        if (limiter == null) {
            return point.proceed();
        }

        // 构建限流 Key：limiter:类名:方法名:IP
        String className = point.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        String ip = IpUtils.getIp(ServletUtils.getRequest());
        String key = String.format("limiter:%s:%s:%s", className, methodName, ip);

        // 计算时间窗口（秒）
        long windowSeconds = limiter.timeUnit().toSeconds(limiter.timeout());
        if (windowSeconds <= 0) {
            windowSeconds = 1;
        }

        // 执行限流检查
        boolean allowed = checkRateLimit(key, windowSeconds, (long) limiter.limit());

        if (!allowed) {
            log.warn("接口限流触发 - Key: {}, 限制: {}/{}秒, IP: {}",
                    key, (long) limiter.limit(), windowSeconds, ip);
            throw new BadRequestException("请求过于频繁，请稍后再试");
        }

        return point.proceed();
    }

    /**
     * 检查是否允许请求
     *
     * @param key         限流 Key
     * @param windowSeconds 时间窗口（秒）
     * @param maxRequests 最大请求数
     * @return true 表示允许，false 表示被限流
     */
    private boolean checkRateLimit(String key, long windowSeconds, long maxRequests) {
        try {
            long now = System.currentTimeMillis();

            // 使用 Redis 有序集合实现滑动窗口
            String countKey = "limiter:count:" + key;

            // 移除过期的请求记录
            long windowStart = now - windowSeconds * 1000;
            redisUtils.zRemoveRangeByScore(countKey, 0, windowStart);

            // 获取当前窗口内的请求数
            long current = redisUtils.zCard(countKey);

            if (current >= maxRequests) {
                return false;
            }

            // 添加当前请求
            redisUtils.zAdd(countKey, String.valueOf(now), now);

            // 设置过期时间
            redisUtils.expire(countKey, windowSeconds + 1, TimeUnit.SECONDS);

            return true;
        } catch (Exception e) {
            log.error("限流检查失败，默认放行: {}", e.getMessage());
            // 限流失败时默认放行，避免影响正常业务
            return true;
        }
    }
}

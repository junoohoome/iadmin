package me.fjq.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.monitor.service.OnlineService;
import me.fjq.properties.SecurityProperties;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Token刷新服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRefreshService {

    /** 续期阈值：剩余有效期小于1小时时触发 */
    private static final long REFRESH_THRESHOLD_MS = 60 * 60 * 1000;

    private final JwtTokenService jwtTokenService;
    private final OnlineService onlineService;
    private final UserCacheService userCacheService;
    private final SecurityProperties properties;

    /**
     * 检查并刷新Token
     *
     * @param token 当前Token（不含Bearer前缀）
     * @return 新Token（如果需要刷新），否则返回null
     */
    public String refreshTokenIfNeeded(String token) {
        try {
            // 1. 获取Token过期时间
            Claims claims = jwtTokenService.getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            long now = System.currentTimeMillis();
            long remainingTime = expiration.getTime() - now;

            // 2. 检查是否需要续期
            if (remainingTime > REFRESH_THRESHOLD_MS) {
                return null; // 不需要续期
            }

            // 3. 生成新Token
            String newToken = jwtTokenService.refreshToken(token);

            // 4. 旧Token加入黑名单（TTL为剩余有效期）
            long ttlSeconds = remainingTime / 1000;
            if (ttlSeconds > 0) {
                onlineService.addToBlacklist(token, ttlSeconds);
            }

            // 5. 续期用户缓存
            String username = claims.getSubject();
            log.info("Token续期成功: username={}, remainingTime={}ms", username, remainingTime);

            return newToken;

        } catch (Exception e) {
            log.warn("Token续期失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断是否需要刷新Token
     */
    public boolean needsRefresh(String token) {
        try {
            Claims claims = jwtTokenService.getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            long remainingTime = expiration.getTime() - System.currentTimeMillis();
            return remainingTime > 0 && remainingTime <= REFRESH_THRESHOLD_MS;
        } catch (Exception e) {
            return false;
        }
    }
}

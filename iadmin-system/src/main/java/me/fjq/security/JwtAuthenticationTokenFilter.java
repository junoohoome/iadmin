package me.fjq.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.fjq.core.HttpResult;
import me.fjq.properties.SecurityProperties;
import me.fjq.utils.ServletUtils;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * token过滤器 验证token有效性
 * <p>
 * 功能：
 * 1. JWT Token 验证
 * 2. Token 自动续期
 * 3. 会话空闲超时检测
 * </p>
 *
 * @author fjq
 */
@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends GenericFilterBean {

    private final JwtTokenService jwtTokenService;
    private final TokenRefreshService tokenRefreshService;
    private final SessionActivityService sessionActivityService;
    private final SecurityProperties properties;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationTokenFilter(JwtTokenService jwtTokenService,
                                         TokenRefreshService tokenRefreshService,
                                         SessionActivityService sessionActivityService,
                                         SecurityProperties properties,
                                         ObjectMapper objectMapper) {
        this.jwtTokenService = jwtTokenService;
        this.tokenRefreshService = tokenRefreshService;
        this.sessionActivityService = sessionActivityService;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 获取token, 并检查登录状态
        jwtTokenService.checkAuthentication(request);

        // 获取当前认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            // 获取用户ID
            Long userId = getUserIdFromAuthentication(authentication);

            if (userId != null) {
                // 检查会话空闲超时
                if (sessionActivityService.isSessionIdleTimeout(userId)) {
                    log.warn("会话空闲超时，强制登出: userId={}", userId);

                    // 清除认证信息
                    SecurityContextHolder.clearContext();

                    // 清除会话活动记录
                    sessionActivityService.clearActivity(userId);

                    // 返回 440 登录超时响应
                    handleSessionTimeout(response);
                    return;
                }

                // 记录用户活动
                sessionActivityService.recordActivity(userId);
            }
        }

        // 检查是否需要Token续期
        String token = jwtTokenService.getToken(request);
        if (token != null && tokenRefreshService.needsRefresh(token)) {
            String newToken = tokenRefreshService.refreshTokenIfNeeded(token);
            if (newToken != null) {
                // 设置响应头返回新Token
                response.setHeader("X-New-Token", properties.getTokenStartWith() + newToken);
                log.debug("Token已续期，新Token通过响应头返回");
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * 从认证信息中获取用户ID
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserDetails) {
            return ((JwtUserDetails) principal).getId();
        }
        return null;
    }

    /**
     * 处理会话超时响应
     */
    private void handleSessionTimeout(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_REQUEST_TIMEOUT);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        HttpResult<Void> result = HttpResult.error(440, "会话已超时，请重新登录");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}

package me.fjq.security;

import lombok.extern.slf4j.Slf4j;
import me.fjq.properties.SecurityProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * token过滤器 验证token有效性
 *
 * @author fjq
 */
@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends GenericFilterBean {

    private final JwtTokenService jwtTokenService;
    private final TokenRefreshService tokenRefreshService;
    private final SecurityProperties properties;

    public JwtAuthenticationTokenFilter(JwtTokenService jwtTokenService,
                                         TokenRefreshService tokenRefreshService,
                                         SecurityProperties properties) {
        this.jwtTokenService = jwtTokenService;
        this.tokenRefreshService = tokenRefreshService;
        this.properties = properties;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 获取token, 并检查登录状态
        jwtTokenService.checkAuthentication(request);

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
}

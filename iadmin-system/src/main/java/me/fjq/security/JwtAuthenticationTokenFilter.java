package me.fjq.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * token过滤器 验证token有效性
 *
 * @author fjq
 */
@Slf4j
@Component
@AllArgsConstructor
public class JwtAuthenticationTokenFilter extends GenericFilterBean {

    private final JwtTokenService jwtTokenService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        // 获取token, 并检查登录状态
        jwtTokenService.checkAuthentication(request);
        filterChain.doFilter(servletRequest, servletResponse);
    }

}

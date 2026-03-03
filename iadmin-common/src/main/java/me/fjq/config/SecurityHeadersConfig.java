package me.fjq.config;

import me.fjq.filter.XssFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 安全响应头配置
 * <p>
 * 添加以下安全响应头：
 * - Content-Security-Policy (CSP): 防止 XSS 攻击
 * - X-Content-Type-Options: 防止 MIME 类型嗅探
 * - X-Frame-Options: 防止点击劫持
 * - X-XSS-Protection: XSS 过滤器（已弃用但仍有用）
 * - Referrer-Policy: 控制 Referrer 信息
 * - Permissions-Policy: 限制浏览器功能
 * </p>
 *
 * @author fjq
 */
@Configuration
public class SecurityHeadersConfig {

    /**
     * XSS 过滤器
     */
    @Bean
    public FilterRegistrationBean<XssFilter> xssFilter() {
        FilterRegistrationBean<XssFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new XssFilter());
        registration.addUrlPatterns("/*");
        registration.setName("xssFilter");
        registration.setOrder(1);  // XSS 过滤器优先级最高
        return registration;
    }

    /**
     * 安全响应头过滤器
     */
    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        FilterRegistrationBean<SecurityHeadersFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SecurityHeadersFilter());
        registration.addUrlPatterns("/*");
        registration.setName("securityHeadersFilter");
        registration.setOrder(2);
        return registration;
    }

    /**
     * 安全响应头过滤器实现
     */
    public static class SecurityHeadersFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response,
                             FilterChain chain) throws IOException, ServletException {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            // 1. Content-Security-Policy (CSP)
            // 限制资源加载来源，防止 XSS 攻击
            String csp = buildCspHeader(httpRequest);
            httpResponse.setHeader("Content-Security-Policy", csp);

            // 2. X-Content-Type-Options
            // 防止浏览器 MIME 类型嗅探
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");

            // 3. X-Frame-Options
            // 防止点击劫持（已被 CSP frame-ancestors 取代，但仍建议保留）
            httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");

            // 4. X-XSS-Protection
            // 启用浏览器 XSS 过滤器（现代浏览器已弃用，但仍有用）
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

            // 5. Referrer-Policy
            // 控制 Referrer 信息泄露
            httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

            // 6. Permissions-Policy
            // 限制浏览器功能访问
            httpResponse.setHeader("Permissions-Policy",
                    "geolocation=(), microphone=(), camera=(), payment=()");

            // 7. Cache-Control
            // 防止敏感页面被缓存
            String uri = httpRequest.getRequestURI();
            if (uri.contains("/auth/") || uri.contains("/user/")) {
                httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
                httpResponse.setHeader("Pragma", "no-cache");
                httpResponse.setHeader("Expires", "0");
            }

            chain.doFilter(request, response);
        }

        /**
         * 构建 CSP 响应头
         * <p>
         * 根据请求路径动态调整 CSP 策略
         * </p>
         */
        private String buildCspHeader(HttpServletRequest request) {
            String uri = request.getRequestURI();

            // Swagger 页面需要更宽松的 CSP
            if (uri.contains("/swagger-ui") || uri.contains("/api-docs")) {
                return "default-src 'self'; " +
                       "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                       "style-src 'self' 'unsafe-inline'; " +
                       "img-src 'self' data: https:; " +
                       "font-src 'self' data:; " +
                       "connect-src 'self';";
            }

            // Druid 监控页面
            if (uri.contains("/druid")) {
                return "default-src 'self'; " +
                       "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                       "style-src 'self' 'unsafe-inline'; " +
                       "img-src 'self' data:;";
            }

            // 默认 CSP 策略（严格模式）
            return "default-src 'self'; " +
                   "script-src 'self'; " +
                   "style-src 'self' 'unsafe-inline'; " +
                   "img-src 'self' data: https:; " +
                   "font-src 'self' data:; " +
                   "connect-src 'self'; " +
                   "frame-ancestors 'self'; " +
                   "base-uri 'self'; " +
                   "form-action 'self';";
        }

        @Override
        public void init(FilterConfig filterConfig) {
            // 初始化时无需操作
        }

        @Override
        public void destroy() {
            // 销毁时无需操作
        }
    }
}

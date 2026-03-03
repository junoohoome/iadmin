package me.fjq.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * XSS 防护过滤器
 * <p>
 * 拦截所有 HTTP 请求，对请求参数进行 XSS 过滤
 * 排除文件上传等特殊请求
 * </p>
 *
 * @author fjq
 */
@Slf4j
@Component
@WebFilter(filterName = "xssFilter", urlPatterns = "/*")
@Order(1)
public class XssFilter implements Filter {

    /**
     * 排除的 URL 路径（这些路径不进行 XSS 过滤）
     */
    private static final List<String> EXCLUDE_URLS = Arrays.asList(
            "/avatar/",
            "/file/",
            "/druid/",
            "/swagger-ui/",
            "/v3/api-docs"
    );

    /**
     * 排除的 Content-Type（文件上传等不进行 XSS 过滤）
     */
    private static final List<String> EXCLUDE_CONTENT_TYPES = Arrays.asList(
            "multipart/form-data",
            "application/octet-stream"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("XSS 过滤器初始化完成");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // 检查是否需要排除
        if (shouldExclude(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        // 包装请求，进行 XSS 过滤
        XssHttpServletRequestWrapper wrappedRequest = new XssHttpServletRequestWrapper(httpRequest);
        chain.doFilter(wrappedRequest, response);
    }

    @Override
    public void destroy() {
        log.info("XSS 过滤器销毁");
    }

    /**
     * 判断是否需要排除 XSS 过滤
     *
     * @param request HTTP 请求
     * @return true 表示排除，不进行 XSS 过滤
     */
    private boolean shouldExclude(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contentType = request.getContentType();

        // 检查 URL 路径
        for (String excludeUrl : EXCLUDE_URLS) {
            if (uri.contains(excludeUrl)) {
                return true;
            }
        }

        // 检查 Content-Type
        if (contentType != null) {
            for (String excludeType : EXCLUDE_CONTENT_TYPES) {
                if (contentType.contains(excludeType)) {
                    return true;
                }
            }
        }

        return false;
    }
}

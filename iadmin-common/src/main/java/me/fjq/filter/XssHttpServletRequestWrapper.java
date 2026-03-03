package me.fjq.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.web.util.HtmlUtils;

/**
 * XSS 防护 Request 包装器
 * <p>
 * 对请求参数进行 HTML 转义，防止 XSS 攻击
 * </p>
 *
 * @author fjq
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        return cleanXSS(value);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }

        String[] cleanValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            cleanValues[i] = cleanXSS(values[i]);
        }
        return cleanValues;
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return cleanXSS(value);
    }

    /**
     * 清理 XSS 攻击字符
     * <p>
     * 使用 Spring HtmlUtils 进行 HTML 转义
     * </p>
     *
     * @param value 原始值
     * @return 转义后的值
     */
    private String cleanXSS(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        // 使用 Spring 的 HtmlUtils 进行 HTML 转义
        String escaped = HtmlUtils.htmlEscape(value);

        // 额外处理一些特殊字符
        escaped = escaped.replaceAll("%3C", "&lt;")
                .replaceAll("%3E", "&gt;")
                .replaceAll("%28", "&#40;")
                .replaceAll("%29", "&#41;")
                .replaceAll("%22", "&quot;")
                .replaceAll("%27", "&#39;");

        return escaped;
    }
}

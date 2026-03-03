package me.fjq.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.HtmlUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

/**
 * XSS 防护 Request 包装器
 * <p>
 * 对请求参数和 JSON 请求体进行 HTML 转义，防止 XSS 攻击
 * </p>
 *
 * @author fjq
 */
@Slf4j
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private byte[] cachedBody;

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (cachedBody == null) {
            // 读取原始请求体
            String contentType = getContentType();
            String body = StreamUtils.copyToString(super.getInputStream(), StandardCharsets.UTF_8);

            // 如果是 JSON 请求，对 JSON 内容进行 XSS 过滤
            if (contentType != null && contentType.contains("application/json") && !body.isEmpty()) {
                body = sanitizeJsonBody(body);
            }

            cachedBody = body.getBytes(StandardCharsets.UTF_8);
        }

        return new ServletInputStream() {
            private final ByteArrayInputStream inputStream = new ByteArrayInputStream(cachedBody);

            @Override
            public boolean isFinished() {
                return inputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(jakarta.servlet.ReadListener listener) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int read() throws IOException {
                return inputStream.read();
            }
        };
    }

    /**
     * 对 JSON 请求体中的字符串值进行 XSS 过滤
     *
     * @param json 原始 JSON 字符串
     * @return 过滤后的 JSON 字符串
     */
    private String sanitizeJsonBody(String json) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode sanitizedNode = sanitizeJsonNode(rootNode);
            return objectMapper.writeValueAsString(sanitizedNode);
        } catch (Exception e) {
            log.warn("JSON XSS 过滤失败，返回原始内容: {}", e.getMessage());
            return json;
        }
    }

    /**
     * 递归处理 JSON 节点
     */
    private JsonNode sanitizeJsonNode(JsonNode node) {
        if (node.isTextual()) {
            return TextNode.valueOf(cleanXSS(node.asText()));
        } else if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                // 使用反射来修改 ObjectNode 的字段值
                if (node.isObject() && node instanceof com.fasterxml.jackson.databind.node.ObjectNode) {
                    ((com.fasterxml.jackson.databind.node.ObjectNode) node).set(entry.getKey(), sanitizeJsonNode(entry.getValue()));
                }
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                if (node instanceof com.fasterxml.jackson.databind.node.ArrayNode) {
                    ((com.fasterxml.jackson.databind.node.ArrayNode) node).set(i, sanitizeJsonNode(node.get(i)));
                }
            }
        }
        return node;
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
        // 不对 Authorization 等 header 进行转义
        if ("authorization".equalsIgnoreCase(name) || "content-type".equalsIgnoreCase(name)) {
            return value;
        }
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

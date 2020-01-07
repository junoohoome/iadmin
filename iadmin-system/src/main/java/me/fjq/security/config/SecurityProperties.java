package me.fjq.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Jwt参数配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class SecurityProperties {
    /** Request Headers ： Authorization */
    public String header;

    /** 令牌前缀，最后留个空格 Bearer */
    public String tokenStartWith;

    /** 必须使用最少88位的Base64对该令牌进行编码 */
    public String base64Secret;

    /** 令牌过期时间 此处单位/毫秒 */
    public Long tokenValidityInSeconds;

    /** 在线用户 key，根据 key 查询 redis 中在线用户的数据 */
    public String onlineKey;

    /** 验证码 key */
    public String codeKey;

    public String getTokenStartWith() {
        return tokenStartWith + " ";
    }
}

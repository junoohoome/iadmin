package me.fjq.security;


import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import me.fjq.properties.SecurityProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * token验证处理
 *
 * @author fjq
 */
@Slf4j
@Component
public class JwtTokenService {

    /**
     * 用户名称
     */
    private static final String USERNAME = Claims.SUBJECT;
    /**
     * 用户信息
     */
    private static final String USERINFO = "userinfo";
    /**
     * 权限列表
     */
    private static final String AUTHORITIES = "authorities";

    private final SecurityProperties properties;

    public JwtTokenService(SecurityProperties properties) {
        this.properties = properties;
    }

    /**
     * 生成令牌
     *
     * @param authentication 认证方式
     * @return 令牌
     */
    public String generateToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Map<String, Object> claims = new HashMap<>(2);
        JwtUserDetails user = (JwtUserDetails) authentication.getPrincipal();
        claims.put(USERNAME, user.getUsername());
        claims.put(AUTHORITIES, authorities);
        claims.put(USERINFO, user);
        return generateToken(claims);
    }

    /**
     * 刷新令牌
     *
     * @param token 令牌
     * @return 新令牌
     */
    public String refreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return generateToken(claims);
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    /**
     * 从令牌中获取用户信息
     *
     * @param token 令牌
     * @return 用户名
     */
    public JwtUserDetails getUserInfoFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return (JwtUserDetails) claims.get(USERINFO);
    }

    /**
     * 获取请求token
     *
     * @param request
     * @return token
     */
    public String getToken(HttpServletRequest request) {
        String requestHeader = request.getHeader(properties.getHeader());
        String tokenStartsWith = properties.getTokenStartWith();
        if (StringUtils.isNotBlank(requestHeader) && requestHeader.startsWith(tokenStartsWith)) {
            return requestHeader.substring(tokenStartsWith.length());
        }
        return null;
    }

    /**
     * 判断令牌是否有效
     *
     * @param token 令牌
     * @return true/false
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT signature.");
            e.printStackTrace();
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token.");
            e.printStackTrace();
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token.");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            log.info("JWT token compact of handler are invalid.");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取认证方式
     *
     * @param token
     * @return 认证方式
     */
    public Authentication getAuthenticationFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * 判断令牌是否过期
     *
     * @param token 令牌
     * @return true/false
     */
    private Boolean isTokenExpired(String token) {
        Claims claims = getClaimsFromToken(token);
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    private String generateToken(Map<String, Object> claims) {
        Date expirationDate = new Date(System.currentTimeMillis() + properties.getTokenValidityInSeconds());
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, properties.base64Secret)
                .compact();
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(properties.getBase64Secret())
                .parseClaimsJws(token)
                .getBody();
    }

}

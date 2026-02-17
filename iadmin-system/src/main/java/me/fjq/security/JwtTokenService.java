package me.fjq.security;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import me.fjq.exception.JwtTokenException;
import me.fjq.properties.SecurityProperties;
import me.fjq.monitor.service.OnlineService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import javax.crypto.SecretKey;
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
    private final OnlineService onlineService;

    public JwtTokenService(SecurityProperties properties, OnlineService onlineService) {
        this.properties = properties;
        this.onlineService = onlineService;
    }

    /**
     * 获取签名密钥
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = properties.getBase64Secret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
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
     * @param request HttpServletRequest
     * @return 用户名
     */
    public JwtUserDetails getJwtUserDetails(HttpServletRequest request) {
        String token = getToken(request);
        if (ObjectUtil.isNull(token)) {
            throw new JwtTokenException("获取令牌异常");
        }
        Claims claims = getClaimsFromToken(token);
        Object obj = claims.get(USERINFO);
        if (ObjectUtil.isNull(obj)) {
            throw new JwtTokenException("获取用户信息异常");
        }
        // LinkHashMap类型转换
        return JSONUtil.toBean(JSONUtil.toJsonStr(obj), JwtUserDetails.class);
    }

    /**
     * 获取请求token
     *
     * @param request HttpServletRequest
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
    private Boolean validateToken(String token) {
        try {
            // 检查 token 是否在黑名单中
            if (onlineService.isBlacklisted(token)) {
                log.warn("Token is blacklisted: {}", token);
                return false;
            }
            return !isTokenExpired(token);
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT token compact of handler are invalid: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 获取认证方式
     *
     * @param token 令牌
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
     * 获取令牌进行认证
     *
     * @param request HttpServletRequest
     */
    public void checkAuthentication(HttpServletRequest request) {
        String token = getToken(request);
        String requestRri = request.getRequestURI();
        // 验证 token
        if (StringUtils.isNotBlank(token) && validateToken(token)) {
            // 获取令牌并根据令牌获取登录认证信息
            Authentication authentication = getAuthenticationFromToken(token);
            // 设置登录认证信息到上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("set Authentication to security context for '{}', uri: {}", authentication.getName(), requestRri);
        } else {
            log.debug("no valid JWT token found, uri: {}", requestRri);
        }
    }

    /**
     * 系统登录认证
     *
     * @param username                     用户名
     * @param password                     密码
     * @param authenticationManagerBuilder 认证管理器
     * @return token
     */
    public String login(String username, String password, AuthenticationManagerBuilder authenticationManagerBuilder) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        // 执行登录认证过程,该方法会去调用UserDetailsServiceImpl.loadUserByUsername
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        // 认证成功存储认证信息到上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return generateToken(authentication);
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
                .claims(claims)
                .expiration(expirationDate)
                .signWith(getSignInKey())
                .compact();
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}

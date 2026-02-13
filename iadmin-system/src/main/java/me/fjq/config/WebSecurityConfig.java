package me.fjq.config;


import lombok.AllArgsConstructor;
import me.fjq.security.JwtAuthenticationTokenFilter;
import me.fjq.security.handle.JwtAccessDeniedHandler;
import me.fjq.security.handle.JwtAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

import java.util.HashSet;
import java.util.Set;

/**
 * Spring Security配置
 *
 * @author fjq
 */
@AllArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WebSecurityConfig {

    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    private final CorsFilter corsFilter;
    private final JwtAuthenticationEntryPoint authenticationErrorHandler;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 密码加密方式
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        Set<String> anonymousUrls = new HashSet<>();
        anonymousUrls.add("/auth/code");
        anonymousUrls.add("/auth/login");
        anonymousUrls.add("/auth/testLogin");  // 测试登录接口（仅开发环境）

        httpSecurity
                // 禁用 CSRF
                .csrf(csrf -> csrf.disable())
                // 添加 CORS 过滤器
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                // 授权异常处理
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationErrorHandler)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                // 防止iframe 造成跨域
                .headers(headers -> headers.frameOptions().disable())
                // 不创建会话
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 授权配置
                .authorizeHttpRequests(auth -> auth
                        // 静态资源 - Spring Boot 默认已处理，这里只放行特定目录
                        .requestMatchers("/webSocket/**").permitAll()
                        // SpringDoc 文档
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs/**", "/swagger-resources/**",
                                "/webjars/**", "/api-docs/**").permitAll()
                        // 文件
                        .requestMatchers("/avatar/**", "/file/**").permitAll()
                        // 阿里巴巴 druid
                        .requestMatchers("/druid/**").permitAll()
                        // 自定义匿名访问所有url放行 ： 允许匿名和带权限以及登录用户访问
                        .requestMatchers(anonymousUrls.toArray(new String[0])).permitAll()
                        // 所有请求都需要认证
                        .anyRequest().authenticated()
                );

        // 添加JWT filter
        httpSecurity.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

}

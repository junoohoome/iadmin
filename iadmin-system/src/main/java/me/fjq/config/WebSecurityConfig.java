package me.fjq.config;


import me.fjq.security.JwtAuthenticationTokenFilter;
import me.fjq.security.handle.JwtAccessDeniedHandler;
import me.fjq.security.handle.JwtAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
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
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WebSecurityConfig {

    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    private final CorsFilter corsFilter;
    private final JwtAuthenticationEntryPoint authenticationErrorHandler;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    public WebSecurityConfig(JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter,
                              CorsFilter corsFilter,
                              JwtAuthenticationEntryPoint authenticationErrorHandler,
                              JwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.jwtAuthenticationTokenFilter = jwtAuthenticationTokenFilter;
        this.corsFilter = corsFilter;
        this.authenticationErrorHandler = authenticationErrorHandler;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

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
                .authorizeHttpRequests(auth -> {
                    // 静态资源 - Spring Boot 默认已处理，这里只放行特定目录
                    auth.requestMatchers("/webSocket/**").permitAll();
                    // 文件（头像、上传文件）
                    auth.requestMatchers("/avatar/**", "/file/**").permitAll();
                    // 自定义匿名访问所有url放行
                    auth.requestMatchers(anonymousUrls.toArray(new String[0])).permitAll();

                    // 安全配置：Swagger 和 Druid 仅在开发环境可访问
                    if ("dev".equals(activeProfile) || "local".equals(activeProfile)) {
                        // SpringDoc 文档 - 仅开发环境
                        auth.requestMatchers("/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs/**", "/swagger-resources/**",
                                "/webjars/**", "/api-docs/**").permitAll();
                        // Druid 监控 - 仅开发环境
                        auth.requestMatchers("/druid/**").permitAll();
                    } else {
                        // 生产环境：Swagger 和 Druid 需要管理员权限
                        auth.requestMatchers("/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs/**", "/swagger-resources/**",
                                "/webjars/**", "/api-docs/**").hasRole("ADMIN");
                        auth.requestMatchers("/druid/**").hasRole("ADMIN");
                    }

                    // 所有请求都需要认证
                    auth.anyRequest().authenticated();
                });

        // 添加JWT filter
        httpSecurity.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

}

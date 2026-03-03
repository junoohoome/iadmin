package me.fjq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * WebMvcConfigurer
 *
 * @author fjq
 */
@Slf4j
@Configuration
@EnableWebMvc
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.path}")
    private String path;

    @Value("${file.avatar}")
    private String avatar;

    /**
     * 允许的 CORS 来源域名
     * 生产环境必须配置具体的域名
     */
    @Value("${cors.allowed-origins:http://localhost:3000,http://127.0.0.1:3000}")
    private String[] allowedOrigins;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 允许携带凭证（Cookies）
        config.setAllowCredentials(true);

        // 配置允许的来源
        if ("prod".equals(activeProfile)) {
            // 生产环境：仅允许配置的域名
            if (allowedOrigins.length == 0 ||
                (allowedOrigins.length == 1 && allowedOrigins[0].contains("localhost"))) {
                log.warn("生产环境未配置 CORS 允许的域名，请检查 cors.allowed-origins 配置！");
            }
            config.setAllowedOrigins(Arrays.asList(allowedOrigins));
            log.info("CORS 配置 - 生产模式，允许的域名: {}", Arrays.toString(allowedOrigins));
        } else {
            // 开发环境：仅允许本地开发域名
            config.setAllowedOrigins(Arrays.asList(
                    "http://localhost:3000",
                    "http://127.0.0.1:3000",
                    "http://localhost:8080",
                    "http://127.0.0.1:8080"
            ));
            log.info("CORS 配置 - 开发模式，允许的域名: localhost:3000, localhost:8080");
        }

        // 允许的请求头（明确指定，不使用 *）
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "X-New-Token",
                "Cache-Control"
        ));

        // 允许的 HTTP 方法
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // 暴露的响应头（前端可以读取）
        config.setExposedHeaders(Arrays.asList(
                "X-New-Token",
                "Content-Disposition"
        ));

        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String avatarUtl = "file:" + avatar.replace("\\", "/");
        String pathUtl = "file:" + path.replace("\\", "/");
        registry.addResourceHandler("/avatar/**").addResourceLocations(avatarUtl).setCachePeriod(0);
        registry.addResourceHandler("/file/**").addResourceLocations(pathUtl).setCachePeriod(0);
    }
}

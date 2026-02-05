package me.fjq.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 配置 (Swagger 3.0)
 *
 * @author fjq
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("iAdmin 后台管理系统 API")
                        .version("3.0.0")
                        .description("iAdmin 后台管理系统接口文档")
                        .contact(new Contact()
                                .name("fjq")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .components(new Components()
                        .addSecuritySchemes("Bearer",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("请输入JWT令牌，格式：Bearer {token}")
                        )
                );
    }

    /**
     * 系统管理模块 API 分组
     */
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("system")
                .pathsToMatch("/system/**")
                .build();
    }

    /**
     * 监控管理模块 API 分组
     */
    @Bean
    public GroupedOpenApi monitorApi() {
        return GroupedOpenApi.builder()
                .group("monitor")
                .pathsToMatch("/monitor/**")
                .build();
    }

    /**
     * 代码生成器模块 API 分组
     */
    @Bean
    public GroupedOpenApi generatorApi() {
        return GroupedOpenApi.builder()
                .group("generator")
                .pathsToMatch("/generator/**")
                .build();
    }

    /**
     * 认证授权模块 API 分组
     */
    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("auth")
                .pathsToMatch("/auth/**", "/login/**", "/logout/**")
                .build();
    }

}

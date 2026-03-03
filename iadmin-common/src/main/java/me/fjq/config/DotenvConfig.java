package me.fjq.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 从 .env 文件加载环境变量到 Spring Environment
 * <p>
 * 加载顺序：
 * 1. 尝试加载当前工作目录下的 .env.dev（开发环境）
 * 2. 尝试加载当前工作目录下的 .env
 * 3. 尝试加载 classpath 下的 .env.dev
 * 4. 尝试加载 classpath 下的 .env
 * </p>
 *
 * @author fjq
 */
@Slf4j
public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String[] ENV_FILES = {".env.dev", ".env"};

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String activeProfile = environment.getProperty("spring.profiles.active", "dev");

        // 仅在开发环境加载 .env 文件
        if (!"dev".equals(activeProfile) && !"local".equals(activeProfile)) {
            log.debug("非开发环境，跳过 .env 文件加载");
            return;
        }

        Dotenv dotenv = loadDotenv();
        if (dotenv == null) {
            log.info("未找到 .env 文件，使用系统环境变量");
            return;
        }

        // 将 .env 中的变量添加到 Spring Environment
        Map<String, Object> envMap = new HashMap<>();
        dotenv.entries().forEach(entry -> {
            envMap.put(entry.getKey(), entry.getValue());
            // 同时设置系统属性，以便 @Value 注解可以读取
            System.setProperty(entry.getKey(), entry.getValue());
        });

        environment.getPropertySources()
                .addFirst(new MapPropertySource("dotenvProperties", envMap));

        log.info("成功加载 .env 文件，共 {} 个配置项", envMap.size());
    }

    private Dotenv loadDotenv() {
        // 尝试从多个位置加载
        for (String filename : ENV_FILES) {
            // 1. 当前工作目录
            File file = new File(filename);
            if (file.exists()) {
                try {
                    Dotenv dotenv = Dotenv.configure()
                            .directory(file.getParent())
                            .filename(file.getName())
                            .ignoreIfMissing()
                            .load();
                    log.info("从工作目录加载环境配置: {}", file.getAbsolutePath());
                    return dotenv;
                } catch (DotenvException e) {
                    log.warn("加载 {} 失败: {}", file.getAbsolutePath(), e.getMessage());
                }
            }

            // 2. classpath
            try {
                Dotenv dotenv = Dotenv.configure()
                        .directory("src/main/resources")
                        .filename(filename)
                        .ignoreIfMissing()
                        .load();
                log.info("从 classpath 加载环境配置: {}", filename);
                return dotenv;
            } catch (DotenvException e) {
                log.debug("从 classpath 加载 {} 失败: {}", filename, e.getMessage());
            }
        }

        return null;
    }
}

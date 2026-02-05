package me.fjq;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码生成工具
 * 用于生成 BCrypt 加密的密码
 */
public class PasswordGeneratorTest {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 原始密码
        String rawPassword = "admin123";

        // 生成 BCrypt 哈希
        String hashedPassword = encoder.encode(rawPassword);

        System.out.println("原始密码: " + rawPassword);
        System.out.println("BCrypt 哈希: " + hashedPassword);

        // 验证
        boolean matches = encoder.matches(rawPassword, hashedPassword);
        System.out.println("验证结果: " + matches);

        // 测试数据库中的哈希值
        String dbHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi";
        boolean dbMatches = encoder.matches(rawPassword, dbHash);
        System.out.println("数据库哈希验证: " + dbMatches);

        // 生成一个新的用于替换
        System.out.println("\n新的哈希值（用于替换）:");
        System.out.println(encoder.encode(rawPassword));
    }
}

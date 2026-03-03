package me.fjq.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 密码复杂度验证器
 * <p>
 * 验证规则：
 * 1. 最小长度 8 位
 * 2. 最大长度 32 位
 * 3. 必须包含大写字母
 * 4. 必须包含小写字母
 * 5. 必须包含数字
 * 6. 必须包含特殊字符
 * 7. 不能包含用户名
 * 8. 不能包含常见弱密码
 * </p>
 *
 * @author fjq
 */
@Slf4j
@Component
public class PasswordValidator {

    /**
     * 最小密码长度
     */
    private static final int MIN_LENGTH = 8;

    /**
     * 最大密码长度
     */
    private static final int MAX_LENGTH = 32;

    /**
     * 大写字母正则
     */
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");

    /**
     * 小写字母正则
     */
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile(".*[a-z].*");

    /**
     * 数字正则
     */
    private static final Pattern DIGIT_PATTERN = Pattern.compile(".*\\d.*");

    /**
     * 特殊字符正则
     */
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?`~].*");

    /**
     * 常见弱密码列表
     */
    private static final List<String> WEAK_PASSWORDS = List.of(
            "password", "123456", "12345678", "qwerty", "abc123",
            "monkey", "master", "dragon", "111111", "baseball",
            "iloveyou", "trustno1", "sunshine", "princess", "welcome",
            "shadow", "superman", "michael", "football", "admin",
            "password1", "password123", "admin123", "root123"
    );

    /**
     * 验证密码复杂度
     *
     * @param password 密码
     * @return 验证结果
     */
    public PasswordValidationResult validate(String password) {
        return validate(password, null);
    }

    /**
     * 验证密码复杂度（带用户名检查）
     *
     * @param password 密码
     * @param username 用户名（可选，用于检查密码是否包含用户名）
     * @return 验证结果
     */
    public PasswordValidationResult validate(String password, String username) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            return PasswordValidationResult.fail("密码不能为空");
        }

        // 1. 长度检查
        if (password.length() < MIN_LENGTH) {
            errors.add("密码长度不能少于 " + MIN_LENGTH + " 位");
        }
        if (password.length() > MAX_LENGTH) {
            errors.add("密码长度不能超过 " + MAX_LENGTH + " 位");
        }

        // 2. 大写字母检查
        if (!UPPERCASE_PATTERN.matcher(password).matches()) {
            errors.add("密码必须包含至少一个大写字母");
        }

        // 3. 小写字母检查
        if (!LOWERCASE_PATTERN.matcher(password).matches()) {
            errors.add("密码必须包含至少一个小写字母");
        }

        // 4. 数字检查
        if (!DIGIT_PATTERN.matcher(password).matches()) {
            errors.add("密码必须包含至少一个数字");
        }

        // 5. 特殊字符检查
        if (!SPECIAL_CHAR_PATTERN.matcher(password).matches()) {
            errors.add("密码必须包含至少一个特殊字符 (!@#$%^&* 等)");
        }

        // 6. 用户名检查
        if (username != null && !username.isEmpty()) {
            if (password.toLowerCase().contains(username.toLowerCase())) {
                errors.add("密码不能包含用户名");
            }
        }

        // 7. 弱密码检查
        String lowerPassword = password.toLowerCase();
        for (String weakPassword : WEAK_PASSWORDS) {
            if (lowerPassword.contains(weakPassword)) {
                errors.add("密码不能包含常见弱密码");
                break;
            }
        }

        // 8. 连续字符检查
        if (hasConsecutiveChars(password, 3)) {
            errors.add("密码不能包含 3 个或更多连续相同的字符");
        }

        // 9. 键盘序列检查
        if (hasKeyboardSequence(password)) {
            errors.add("密码不能包含键盘连续序列");
        }

        if (errors.isEmpty()) {
            return PasswordValidationResult.success();
        }

        return PasswordValidationResult.fail(String.join("；", errors));
    }

    /**
     * 检查是否有连续相同字符
     *
     * @param password 密码
     * @param count    连续数量
     * @return true 表示有连续相同字符
     */
    private boolean hasConsecutiveChars(String password, int count) {
        if (password.length() < count) {
            return false;
        }

        for (int i = 0; i <= password.length() - count; i++) {
            char c = password.charAt(i);
            boolean consecutive = true;
            for (int j = 1; j < count; j++) {
                if (password.charAt(i + j) != c) {
                    consecutive = false;
                    break;
                }
            }
            if (consecutive) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查是否有键盘序列
     *
     * @param password 密码
     * @return true 表示有键盘序列
     */
    private boolean hasKeyboardSequence(String password) {
        String[] sequences = {
                "qwerty", "asdfgh", "zxcvbn",
                "123456", "234567", "345678",
                "abcdef", "bcdefg", "cdefgh",
                "qazwsx", "edcrfv"
        };

        String lowerPassword = password.toLowerCase();
        for (String seq : sequences) {
            if (lowerPassword.contains(seq)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取密码强度等级
     *
     * @param password 密码
     * @return 强度等级（1-5）
     */
    public int getStrengthLevel(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int score = 0;

        // 长度得分
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.length() >= 16) score++;

        // 字符类型得分
        if (UPPERCASE_PATTERN.matcher(password).matches()) score++;
        if (LOWERCASE_PATTERN.matcher(password).matches()) score++;
        if (DIGIT_PATTERN.matcher(password).matches()) score++;
        if (SPECIAL_CHAR_PATTERN.matcher(password).matches()) score++;

        // 归一化到 1-5
        return Math.min(5, Math.max(1, score));
    }

    /**
     * 获取密码强度描述
     *
     * @param level 强度等级
     * @return 强度描述
     */
    public String getStrengthDescription(int level) {
        return switch (level) {
            case 1 -> "非常弱";
            case 2 -> "弱";
            case 3 -> "中等";
            case 4 -> "强";
            case 5 -> "非常强";
            default -> "未知";
        };
    }

    /**
     * 获取密码复杂度要求说明
     *
     * @return 复杂度要求说明
     */
    public String getRequirements() {
        return String.format(
                "密码必须满足以下要求：" +
                "1. 长度 %d-%d 位；" +
                "2. 包含大写字母；" +
                "3. 包含小写字母；" +
                "4. 包含数字；" +
                "5. 包含特殊字符 (!@#$%%^&* 等)",
                MIN_LENGTH, MAX_LENGTH
        );
    }

    /**
     * 密码验证结果
     */
    public static class PasswordValidationResult {
        private final boolean valid;
        private final String message;
        private final int strengthLevel;

        private PasswordValidationResult(boolean valid, String message, int strengthLevel) {
            this.valid = valid;
            this.message = message;
            this.strengthLevel = strengthLevel;
        }

        public static PasswordValidationResult success() {
            return new PasswordValidationResult(true, "密码验证通过", 0);
        }

        public static PasswordValidationResult fail(String message) {
            return new PasswordValidationResult(false, message, 0);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public int getStrengthLevel() {
            return strengthLevel;
        }
    }
}

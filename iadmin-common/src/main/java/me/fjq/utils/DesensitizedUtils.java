package me.fjq.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志脱敏工具类
 *
 * <p>用于对敏感信息进行脱敏处理，防止敏感数据泄露到日志中
 *
 * @author fjq
 * @since 2025-02-05
 */
@Slf4j
public class DesensitizedUtils {

    /**
     * 用户名脱敏
     * <p>规则：保留前2位，中间用*替换，最后保留1位
     * <p>示例：admin -> a***n
     *
     * @param username 用户名
     * @return 脱敏后的用户名
     */
    public static String username(String username) {
        if (StrUtil.isBlank(username)) {
            return "";
        }
        int len = username.length();
        if (len <= 3) {
            return "***";
        }
        return username.charAt(0) + "***" + username.charAt(len - 1);
    }

    /**
     * 手机号脱敏
     * <p>规则：保留前3位，中间用*替换，最后保留4位
     * <p>示例：13812345678 -> 138****5678
     *
     * @param mobile 手机号
     * @return 脱敏后的手机号
     */
    public static String mobile(String mobile) {
        if (StrUtil.isBlank(mobile) || mobile.length() != 11) {
            return "";
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(7);
    }

    /**
     * 邮箱脱敏
     * <p>规则：@前只显示前2位，其他用*替换
     * <p>示例：example@gmail.com -> ex***@gmail.com
     *
     * @param email 邮箱
     * @return 脱敏后的邮箱
     */
    public static String email(String email) {
        if (StrUtil.isBlank(email)) {
            return "";
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) {
            return "***" + email.substring(atIndex);
        }
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }

    /**
     * 身份证号脱敏
     * <p>规则：保留前6位，中间用*替换，最后保留4位
     * <p>示例：110101199001011234 -> 110101********1234
     *
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    public static String idCard(String idCard) {
        if (StrUtil.isBlank(idCard) || idCard.length() != 18) {
            return "";
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(14);
    }

    /**
     * 密码脱敏
     * <p>规则：全部用*替换
     *
     * @param password 密码
     * @return 脱敏后的密码
     */
    public static String password(String password) {
        if (StrUtil.isBlank(password)) {
            return "";
        }
        return "******";
    }

    /**
     * 银行卡号脱敏
     * <p>规则：保留前4位，中间用*替换，最后保留4位
     * <p>示例：6222021234567890123 -> 6222***********0123
     *
     * @param cardNo 银行卡号
     * @return 脱敏后的银行卡号
     */
    public static String bankCard(String cardNo) {
        if (StrUtil.isBlank(cardNo) || cardNo.length() < 16) {
            return "";
        }
        return cardNo.substring(0, 4) + "***********" + cardNo.substring(cardNo.length() - 4);
    }

    /**
     * 地址脱敏
     * <p>规则：只显示到区/县，后面用*替换
     *
     * @param address 地址
     * @return 脱敏后的地址
     */
    public static String address(String address) {
        if (StrUtil.isBlank(address)) {
            return "";
        }
        int length = 10;
        if (address.length() < length) {
            return "***";
        }
        return address.substring(0, length) + "***";
    }

    /**
     * JSON 字符串脱敏
     * <p>自动识别并脱敏 JSON 中的敏感字段
     *
     * @param json JSON 字符串
     * @return 脱敏后的 JSON 字符串
     */
    public static String json(String json) {
        if (StrUtil.isBlank(json)) {
            return "";
        }

        String result = json;

        // 脱敏密码字段
        result = desensitizeField(result, "\"password\"", "******");
        result = desensitizeField(result, "\"pwd\"", "******");
        result = desensitizeField(result, "\"passwd\"", "******");

        // 脱敏手机号字段
        result = desensitizeField(result, "\"mobile\"", "mobile");
        result = desensitizeField(result, "\"phone\"", "mobile");

        // 脱敏身份证号字段
        result = desensitizeField(result, "\"idCard\"", "idCard");
        result = desensitizeField(result, "\"idcard\"", "idCard");

        // 脱敏邮箱字段
        result = desensitizeField(result, "\"email\"", "email");

        return result;
    }

    /**
     * 脱敏 JSON 中的指定字段
     *
     * @param json        JSON 字符串
     * @param fieldName   字段名
     * @param replacement 替换值
     * @return 脱敏后的 JSON 字符串
     */
    private static String desensitizeField(String json, String fieldName, String replacement) {
        Pattern pattern = Pattern.compile(fieldName + "\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);

        if (replacement.equals("mobile")) {
            // 对手机号进行脱敏
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String mobile = matcher.group(1);
                matcher.appendReplacement(sb, fieldName + ":\"" + mobile(mobile) + "\"");
            }
            return matcher.appendTail(sb).toString();
        } else if (replacement.equals("idCard")) {
            // 对身份证号进行脱敏
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String idCard = matcher.group(1);
                matcher.appendReplacement(sb, fieldName + ":\"" + idCard(idCard) + "\"");
            }
            return matcher.appendTail(sb).toString();
        } else if (replacement.equals("email")) {
            // 对邮箱进行脱敏
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String email = matcher.group(1);
                matcher.appendReplacement(sb, fieldName + ":\"" + email(email) + "\"");
            }
            return matcher.appendTail(sb).toString();
        } else {
            // 直接替换
            return json.replaceAll(fieldName + "\\s*:\\s*\"([^\"]+)\"", fieldName + ":\"" + replacement + "\"");
        }
    }

    /**
     * 通用脱敏方法
     * <p>根据指定的前缀和后缀保留位数进行脱敏
     *
     * @param str            待脱敏字符串
     * @param prefixKeepLen 前缀保留位数
     * @param suffixKeepLen 后缀保留位数
     * @return 脱敏后的字符串
     */
    public static String desensitize(String str, int prefixKeepLen, int suffixKeepLen) {
        if (StrUtil.isBlank(str)) {
            return "";
        }
        int len = str.length();
        if (len <= prefixKeepLen + suffixKeepLen) {
            return "***";
        }
        String prefix = str.substring(0, prefixKeepLen);
        String suffix = str.substring(len - suffixKeepLen);
        int asteriskCount = len - prefixKeepLen - suffixKeepLen;
        StringBuilder asterisks = new StringBuilder();
        for (int i = 0; i < asteriskCount; i++) {
            asterisks.append("*");
        }
        return prefix + asterisks + suffix;
    }

    private DesensitizedUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}

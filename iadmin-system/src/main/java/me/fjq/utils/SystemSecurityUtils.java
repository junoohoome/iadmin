package me.fjq.utils;

import me.fjq.exception.BadRequestException;
import me.fjq.security.JwtUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 系统模块安全工具类扩展
 *
 * @author fjq
 * @since 2025-02-05
 */
public class SystemSecurityUtils extends SecurityUtils {

    /**
     * 获取当前登录用户
     *
     * @return JwtUserDetails
     */
    public static JwtUserDetails getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof JwtUserDetails) {
                return (JwtUserDetails) authentication.getPrincipal();
            }
        } catch (Exception e) {
            throw new BadRequestException(HttpStatus.UNAUTHORIZED, "登录状态过期");
        }
        return null;
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名
     */
    public static String getCurrentUsername() {
        JwtUserDetails user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

}

package me.fjq.utils;


import me.fjq.exception.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 安全服务工具类
 */
public class SecurityUtils {

    /**
     * 获取当前用户名
     *
     * @return 用户名
     */
    public static String getUsername() {
        String username = null;
        Authentication authentication = getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal != null && principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            }
        }
        return username;
    }

    /**
     * 获取用户名
     *
     * @return
     */
    public static String getUsername(Authentication authentication) {
        String username = null;
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal != null && principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            }
        }
        return username;
    }

    /**
     * 获取当前登录信息
     *
     * @return
     */
    public static Authentication getAuthentication() {
        if (SecurityContextHolder.getContext() == null) {
            return null;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication;
    }


    /**
     * 获取当前登录用户信息
     *
     * @return UserDetails
     */
    public static UserDetails getUserDetails() {
        UserDetails userDetails;
        try {
            userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (Exception e) {
            throw new BadRequestException(HttpStatus.UNAUTHORIZED, "登录状态过期");
        }
        return userDetails;
    }

    /**
     * 是否为管理员
     *
     * @param userId 用户ID
     * @return 结果
     */
    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }
}

package me.fjq.utils;


import me.fjq.exception.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
     * 是否为管理员（基于用户ID，仅用于兼容旧代码）
     *
     * @param userId 用户ID
     * @return 结果
     * @deprecated 使用 isAdmin(Collection<SimpleGrantedAuthority>) 进行基于角色的判断
     */
    @Deprecated
    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }

    /**
     * 检查用户是否拥有管理员权限（基于角色）
     *
     * @param authorities 用户权限集合
     * @return 是否为管理员
     */
    public static boolean isAdmin(java.util.Collection<SimpleGrantedAuthority> authorities) {
        if (authorities == null || authorities.isEmpty()) {
            return false;
        }
        return authorities.stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
    }
}

package me.fjq.config;

import me.fjq.constant.Constants;
import me.fjq.utils.SecurityUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author fjq
 */
@Service(value = "ss")
public class PermissionConfig {

    public Boolean hasPerms(String... permissions) {
        List<String> perms = SecurityUtils.getUserDetails().getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        // 管理员拥有所有权限
        if (perms.contains(Constants.SYS_ADMIN_PERMISSION)) {
            return true;
        }
        // 判断当前用户的所有权限是否包含接口上定义的权限
        // 处理逗号分隔的权限字符串（如 "admin,system:user:list"）
        return Arrays.stream(permissions)
                .flatMap(perm -> Arrays.stream(perm.split(",")))  // 分割逗号分隔的权限
                .map(String::trim)  // 去除空格
                .filter(s -> !s.isEmpty())  // 过滤空字符串
                .anyMatch(perms::contains);  // 检查用户是否拥有任一权限
    }
}

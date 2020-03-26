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
        // 判断当前用户的所有权限是否包含接口上定义的权限
        return perms.contains(Constants.SYS_ADMIN_PERMISSION) || Arrays.stream(permissions).anyMatch(perms::contains);
    }
}

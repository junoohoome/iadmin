package me.fjq.security;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.enums.UserStatus;
import me.fjq.exception.BadRequestException;
import me.fjq.system.entity.SysUser;
import me.fjq.system.service.SysMenuService;
import me.fjq.system.service.SysUserService;
import me.fjq.utils.SecurityUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

;

/**
 * @author fjq
 */
@Slf4j
@AllArgsConstructor
@Service("userDetailsService")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserService userService;
    private final SysMenuService menuService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        SysUser user = userService.selectUserByUserName(username);
        if (user == null) {
            throw new BadRequestException("账号不存在");
        }
        if (user.getStatus().equals(UserStatus.DISABLE.getCode())) {
            throw new BadRequestException("账号未激活");
        }
        Set<String> permissions = menuService.selectMenuPermsByUserId(user.getUserId());
        // 设置管理员权限
        if(SecurityUtils.isAdmin(user.getUserId())) {
            permissions.add("superadmin");
        }
        List<GrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new JwtUserDetails(
                user.getUserId(),
                user.getUserName(),
                user.getNickName(),
                user.getSex(),
                user.getPassword(),
                user.getAvatar(),
                user.getEmail(),
                user.getMobile(),
                authorities,
                user.getStatus(),
                user.getCreateTime());
    }

}

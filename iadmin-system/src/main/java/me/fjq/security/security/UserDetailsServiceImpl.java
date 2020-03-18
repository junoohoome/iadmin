package me.fjq.security.security;
import me.fjq.system.domain.SysUser;
import me.fjq.system.service.ISysMenuService;
import me.fjq.system.service.ISysUserService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户登录认证信息查询
 * @author fjq
 * @date 2020/03/18
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final ISysUserService sysUserService;
    private final ISysMenuService sysMenuService;

    public UserDetailsServiceImpl(ISysUserService sysUserService, ISysMenuService sysMenuService) {
        this.sysUserService = sysUserService;
        this.sysMenuService = sysMenuService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserService.selectUserByUserName(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        // 用户权限列表，根据用户拥有的权限标识与如 @PreAuthorize("hasAuthority('sys:menu:view')") 标注的接口对比，决定是否可以调用接口
        Set<String> permissions = sysMenuService.selectMenuPermsByUserId(user.getUserId());
        List<GrantedAuthority> grantedAuthorities = permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        return new JwtUserDetails(user.getUserName(), user.getPassword(), grantedAuthorities);
    }
}
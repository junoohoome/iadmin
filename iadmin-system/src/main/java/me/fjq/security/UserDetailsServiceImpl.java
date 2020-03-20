package me.fjq.security;


import lombok.extern.slf4j.Slf4j;
import me.fjq.enums.UserStatus;
import me.fjq.exception.BadRequestException;
import me.fjq.system.domain.SysUser;
import me.fjq.system.service.ISysMenuService;
import me.fjq.system.service.ISysUserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author fjq
 */
@Slf4j
@Service("userDetailsService")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class UserDetailsServiceImpl implements UserDetailsService {

    private final ISysUserService userService;
    private final ISysMenuService menuService;

    public UserDetailsServiceImpl(ISysUserService userService, ISysMenuService menuService) {
        this.userService = userService;
        this.menuService = menuService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        SysUser user = userService.selectUserByUserName(username);
        if (user == null) {
            throw new BadRequestException("账号不存在");
        }
        if (user.getStatus().equals(UserStatus.DISABLE.getCode())) {
            throw new BadRequestException("账号未激活");
        }
        return createJwtUser(user);
    }

    private UserDetails createJwtUser(SysUser user) {
        return new JwtUserDetails(
                user.getUserId(),
                user.getUserName(),
                user.getNickName(),
                user.getSex(),
                user.getPassword(),
                user.getAvatar(),
                user.getEmail(),
                user.getPhoneNumber(),
                menuService.mapToGrantedAuthorities(user),
                user.getStatus(),
                user.getCreateTime()
        );
    }
}

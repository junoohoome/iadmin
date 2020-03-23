package me.fjq.security;


import lombok.extern.slf4j.Slf4j;
import me.fjq.enums.UserStatus;
import me.fjq.exception.BadRequestException;
import me.fjq.system.entity.SysUser;
import me.fjq.system.service.SysUserService;
import me.fjq.system.service.SysMenuService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

;

/**
 * @author fjq
 */
@Slf4j
@Service("userDetailsService")
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserService userService;
    private final SysMenuService menuService;

    public UserDetailsServiceImpl(SysUserService userService, SysMenuService menuService) {
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
        return new JwtUserDetails(
                user.getUserId(),
                user.getUserName(),
                user.getNickName(),
                user.getSex(),
                user.getPassword(),
                user.getAvatar(),
                user.getEmail(),
                user.getMobile(),
                menuService.mapToGrantedAuthorities(user),
                user.getStatus(),
                user.getCreateTime());
    }

}

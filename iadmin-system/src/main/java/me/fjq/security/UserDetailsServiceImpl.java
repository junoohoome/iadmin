package me.fjq.security;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.constant.Constants;
import me.fjq.enums.UserStatus;
import me.fjq.exception.BadRequestException;
import me.fjq.system.entity.SysUser;
import me.fjq.system.service.SysMenuService;
import me.fjq.system.service.SysUserService;
import me.fjq.utils.RedisUtils;
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
        // 1. 查询用户（仍需从数据库获取，因为需要验证密码）
        SysUser user = userService.getOne(new QueryWrapper<SysUser>().lambda().eq(SysUser::getUserName, username));
        if (user == null) {
            throw new BadRequestException("账号不存在");
        }
        if (user.getStatus().equals(UserStatus.DISABLE.getCode())) {
            throw new BadRequestException("账号未激活");
        }

        // 2. 从数据库查询权限（暂时禁用缓存，避免序列化问题）
        Set<String> permissions = menuService.selectMenuPermsByUserId(user.getUserId());
        // 设置管理员权限
        if (SecurityUtils.isAdmin(user.getUserId())) {
            permissions.add(Constants.SYS_ADMIN_PERMISSION);
        }

        List<GrantedAuthority> authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return JwtUserDetails.builder()
                .id(user.getUserId())
                .username(user.getUserName())
                .nickName(user.getNickName())
                .sex(user.getSex())
                .password(user.getPassword())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .authorities(authorities)
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .deptId(user.getDeptId())
                .ancestors(user.getAncestors())
                .roles(null)
                .build();
    }

}

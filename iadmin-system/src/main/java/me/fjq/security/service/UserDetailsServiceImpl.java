//package me.fjq.security.service;
//
//
//import me.fjq.exception.BadRequestException;
//import me.fjq.security.security.vo.JwtUser;
//import me.fjq.system.domain.SysDept;
//import me.fjq.system.domain.SysPost;
//import me.fjq.system.domain.SysUser;
//import me.fjq.system.service.ISysRoleService;
//import me.fjq.system.service.ISysUserService;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Propagation;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Optional;
//
//
//@Service("userDetailsService")
//@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
//public class UserDetailsServiceImpl implements UserDetailsService {
//
//    private final ISysUserService userService;
//    private final ISysRoleService roleService;
//
//    public UserDetailsServiceImpl(ISysUserService userService, ISysRoleService roleService) {
//        this.userService = userService;
//        this.roleService = roleService;
//    }
//
//
//    @Override
//    public UserDetails loadUserByUsername(String username){
//        SysUser user = userService.selectUserByUserName(username);
//        if (user == null) {
//            throw new BadRequestException("账号不存在");
//        } else {
//            if (user.getStatus().equals("1")) {
//                throw new BadRequestException("账号未激活");
//            }
//            return createJwtUser(user);
//        }
//    }
//
//    private UserDetails createJwtUser(SysUser user) {
//        return new JwtUser(
//                user.getUserId(),
//                user.getUserName(),
//                user.getNickName(),
//                user.getSex(),
//                user.getPassword(),
//                user.getAvatar(),
//                user.getEmail(),
//                user.getPhonenumber(),
//                Optional.ofNullable(user.getDept()).map(SysDept::getDeptName).orElse(null),
//                roleService.mapToGrantedAuthorities(user),
//                Boolean.valueOf(user.getStatus()),
//                user.getCreateTime(),
//                user.getUpdateTime()
//        );
//    }
//}

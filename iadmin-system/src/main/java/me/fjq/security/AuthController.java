package me.fjq.security;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import me.fjq.exception.BadRequestException;
import me.fjq.security.config.SecurityProperties;
import me.fjq.security.security.TokenProvider;
import me.fjq.security.security.utils.SecurityUtils;
import me.fjq.security.security.vo.AuthUser;
import me.fjq.security.security.vo.JwtUser;
import me.fjq.system.service.ISysMenuService;
import me.fjq.system.service.ISysRoleService;
import me.fjq.utils.RedisUtils;
import me.fjq.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 授权、根据token获取用户详细信息
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${loginCode.expiration}")
    private Long expiration;
    @Value("${rsa.private_key}")
    private String privateKey;
    @Value("${single.login:false}")
    private Boolean singleLogin;
    private final SecurityProperties properties;
    private final RedisUtils redisUtils;
    private final UserDetailsService userDetailsService;
    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final ISysRoleService roleService;
    private final ISysMenuService menuService;

    public AuthController(SecurityProperties properties, RedisUtils redisUtils, UserDetailsService userDetailsService,
                          TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder, ISysRoleService roleService, ISysMenuService menuService) {
        this.properties = properties;
        this.redisUtils = redisUtils;
        this.userDetailsService = userDetailsService;
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.roleService = roleService;
        this.menuService = menuService;
    }


    @PostMapping(value = "/login")
    public ResponseEntity<Object> login(@Validated @RequestBody AuthUser authUser) {
        // 密码解密
        RSA rsa = new RSA(privateKey, null);
        String password = new String(rsa.decrypt(authUser.getPassword(), KeyType.PrivateKey));
        // 查询验证码
        String code = (String) redisUtils.get(authUser.getUuid());
        // 清除验证码
        redisUtils.del(authUser.getUuid());

        if (StringUtils.isBlank(code)) {
            throw new BadRequestException("验证码不存在或已过期");
        }
        if (StringUtils.isBlank(authUser.getCode()) || !authUser.getCode().equalsIgnoreCase(code)) {
            throw new BadRequestException("验证码错误");
        }
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(authUser.getUsername(), password);
        // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // 生成令牌
        String token = tokenProvider.createToken(authentication);
        Map<String, Object> res = new HashMap<String, Object>(1) {{
            put("token", properties.getTokenStartWith() + token);
        }};
        return ResponseEntity.ok(res);
    }

    @GetMapping(value = "/info")
    public ResponseEntity<Object> getUserInfo() {
        JwtUser jwtUser = (JwtUser) userDetailsService.loadUserByUsername(SecurityUtils.getUsername());
        Set<String> roles = new HashSet<>();
        Set<String> permissions = new HashSet<>();
        // 管理员拥有所有权限
        boolean isAdmin = SecurityUtils.isAdmin(jwtUser.getId());
        if (isAdmin) {
            roles.add("admin");
            permissions.add("*:*:*");
        } else {
            roles.addAll(roleService.selectRolePermissionByUserId(jwtUser.getId()));
            permissions.addAll(menuService.selectMenuPermsByUserId(jwtUser.getId()));
        }
        HashMap map = new HashMap(3);
        map.put("user", jwtUser);
        map.put("roles", roles);
        map.put("permissions", permissions);
        return ResponseEntity.ok(map);
    }

    @GetMapping(value = "/code")
    public ResponseEntity<Object> getCode() {
        // 算术类型
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(111, 36);
        // 几位数运算，默认是两位
        captcha.setLen(2);
        // 获取运算的结果
        String result = captcha.text();
        String uuid = properties.getCodeKey() + IdUtil.simpleUUID();
        // 保存
        redisUtils.set(uuid, result, expiration, TimeUnit.MINUTES);
        // 验证码信息
        Map<String, Object> imgResult = new HashMap<String, Object>(2) {{
            put("img", captcha.toBase64());
            put("uuid", uuid);
        }};
        return ResponseEntity.ok(imgResult);
    }

    @DeleteMapping(value = "/logout")
    public ResponseEntity<Object> logout() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}

package me.fjq.system.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import me.fjq.core.HttpResult;
import me.fjq.exception.BadRequestException;
import me.fjq.security.config.SecurityProperties;
import me.fjq.security.security.JwtAuthenticatioToken;
import me.fjq.security.security.utils.SecurityUtils;
import me.fjq.system.domain.SysUser;
import me.fjq.system.service.ISysUserService;
import me.fjq.system.vo.LoginUser;
import me.fjq.utils.RedisUtils;
import me.fjq.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author fang
 * @date 2020/3/18 16:56
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class LoginController {
    @Value("${loginCode.expiration}")
    private Long expiration;
    @Value("${rsa.private_key}")
    private String privateKey;
    @Value("${single.login:false}")
    private Boolean singleLogin;

    private final SecurityProperties properties;
    private final RedisUtils redisUtils;
    private final ISysUserService sysUserService;
    private final AuthenticationManager authenticationManager;

    public LoginController(SecurityProperties properties, RedisUtils redisUtils, ISysUserService sysUserService, AuthenticationManager authenticationManager) {
        this.properties = properties;
        this.redisUtils = redisUtils;
        this.sysUserService = sysUserService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping(value = "/login")
    public HttpResult login(HttpServletRequest request, @Validated @RequestBody LoginUser loginUser) {
        // 密码解密
        RSA rsa = new RSA(privateKey, null);
        String password = new String(rsa.decrypt(loginUser.getPassword(), KeyType.PrivateKey));
        // 查询验证码
        String code = (String) redisUtils.get(loginUser.getUuid());
        // 清除验证码
        redisUtils.del(loginUser.getUuid());

        if (StringUtils.isBlank(code)) {
            throw new BadRequestException("验证码不存在或已过期");
        }
        if (StringUtils.isBlank(loginUser.getCode()) || !loginUser.getCode().equalsIgnoreCase(code)) {
            throw new BadRequestException("验证码错误");
        }
        // 用户信息
        SysUser user = sysUserService.selectUserByUserName(loginUser.getUsername());
        // 账号不存在、密码错误
        if (user == null) {
            return HttpResult.error("账号不存在");
        }
//        if (!PasswordUtils.matches(user.getSalt(), password, user.getPassword())) {
//            return HttpResult.error("密码不正确");
//        }
        // 系统登录认证
        JwtAuthenticatioToken token = SecurityUtils.login(request, loginUser.getUsername(), password, authenticationManager);
        return HttpResult.ok(token);
    }

    @GetMapping(value = "/code")
    public HttpResult getCode() {
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
        return HttpResult.ok(imgResult);
    }

    @DeleteMapping(value = "/logout")
    public HttpResult logout() {
        return HttpResult.ok();
    }
}

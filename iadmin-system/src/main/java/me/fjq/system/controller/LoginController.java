package me.fjq.system.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import me.fjq.constant.Constants;
import me.fjq.core.HttpResult;
import me.fjq.properties.SecurityProperties;
import me.fjq.security.JwtTokenService;
import me.fjq.system.vo.AuthUser;
import me.fjq.utils.RedisUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 授权、根据token获取用户详细信息
 *
 * @author fjq
 */
@Slf4j
@RestController
@RequestMapping
public class LoginController {

    @Value("${rsa.private_key}")
    private String privateKey;
    private final SecurityProperties properties;
    private final RedisUtils redisUtils;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public LoginController(SecurityProperties properties, RedisUtils redisUtils,
                           JwtTokenService jwtTokenService, AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.properties = properties;
        this.redisUtils = redisUtils;
        this.jwtTokenService = jwtTokenService;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }


    @PostMapping(value = "auth/login")
    public HttpResult login(@Validated @RequestBody AuthUser authUser) {
        // 密码解密
        RSA rsa = new RSA(privateKey, null);
        String password = new String(rsa.decrypt(authUser.getPassword(), KeyType.PrivateKey));
        // 查询验证码
        String code = (String) redisUtils.get(authUser.getUuid());
        // 清除验证码
        redisUtils.del(authUser.getUuid());
        if (StringUtils.isBlank(code)) {
            return HttpResult.error("验证码不存在或已过期");
        }
        if (StringUtils.isBlank(authUser.getCode()) || !authUser.getCode().equalsIgnoreCase(code)) {
            return HttpResult.error("验证码错误");
        }
        // 系统登录认证并返回令牌
        String token = jwtTokenService.login(authUser.getUsername(), password, authenticationManagerBuilder);
        return HttpResult.ok(properties.getTokenStartWith().concat(token));
    }

    @GetMapping(value = "auth/code")
    public HttpResult getCode() {
        // 算术类型
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(111, 36);
        // 几位数运算，默认是两位
        captcha.setLen(2);
        // 获取运算的结果
        String result = captcha.text();
        String uuid = Constants.CODE_KEY + IdUtil.simpleUUID();
        // 保存
        redisUtils.set(uuid, result, Constants.CODE_EXPIRE_TIME, TimeUnit.MINUTES);
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

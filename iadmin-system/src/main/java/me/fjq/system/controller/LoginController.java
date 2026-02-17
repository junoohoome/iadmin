package me.fjq.system.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import lombok.extern.slf4j.Slf4j;
import me.fjq.constant.Constants;
import me.fjq.core.HttpResult;
import me.fjq.properties.SecurityProperties;
import me.fjq.security.JwtTokenService;
import me.fjq.monitor.service.OnlineService;
import me.fjq.event.LoginRecordEvent;
import org.springframework.context.ApplicationEventPublisher;
import me.fjq.system.vo.AuthUser;
import me.fjq.utils.IpUtils;
import me.fjq.utils.RedisUtils;
import me.fjq.utils.ServletUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.AuthenticationException;
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
@RequestMapping("auth")
public class LoginController {

    @Value("${rsa.private_key}")
    private String privateKey;
    private final SecurityProperties properties;
    private final RedisUtils redisUtils;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final String activeProfile;
    private final OnlineService onlineService;
    private final ApplicationEventPublisher eventPublisher;

    public LoginController(SecurityProperties properties, RedisUtils redisUtils,
                           JwtTokenService jwtTokenService, AuthenticationManagerBuilder authenticationManagerBuilder,
                           OnlineService onlineService,
                           ApplicationEventPublisher eventPublisher,
                           @org.springframework.beans.factory.annotation.Value("${spring.profiles.active:dev}") String activeProfile) {
        this.properties = properties;
        this.redisUtils = redisUtils;
        this.jwtTokenService = jwtTokenService;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.onlineService = onlineService;
        this.eventPublisher = eventPublisher;
        this.activeProfile = activeProfile;
    }

    @PostMapping(value = "login")
    public HttpResult login(@Validated @RequestBody AuthUser authUser) {
        String username = authUser.getUsername();
        String ipaddr = IpUtils.getIp(ServletUtils.getRequest());
        String browser = IpUtils.getBrowser(ServletUtils.getRequest());
        String os = IpUtils.getOs(ServletUtils.getRequest());

        // 密码解密
        RSA rsa = new RSA(privateKey, null);
        String password = new String(rsa.decrypt(authUser.getPassword(), KeyType.PrivateKey));

        // 开发环境跳过验证码验证
        if (!"dev".equals(activeProfile)) {
            // 查询验证码
            String code = (String) redisUtils.get(authUser.getUuid());
            // 清除验证码
            redisUtils.del(authUser.getUuid());
            if (StringUtils.isBlank(code)) {
                eventPublisher.publishEvent(LoginRecordEvent.fail(username, ipaddr, browser, os, "验证码不存在"));
                return HttpResult.error(Constants.CAPTCHA_NOT_EXIST);
            }
            if (StringUtils.isBlank(authUser.getCode()) || !authUser.getCode().equalsIgnoreCase(code)) {
                eventPublisher.publishEvent(LoginRecordEvent.fail(username, ipaddr, browser, os, "验证码错误"));
                return HttpResult.error(Constants.CAPTCHA_ERROR);
            }
        }

        // 系统登录认证并返回令牌
        try {
            String token = jwtTokenService.login(username, password, authenticationManagerBuilder);
            // 记录登录成功日志
            eventPublisher.publishEvent(LoginRecordEvent.success(username, ipaddr, browser, os));
            return HttpResult.ok(properties.getTokenStartWith().concat(token));
        } catch (AuthenticationException e) {
            // 记录登录失败日志
            eventPublisher.publishEvent(LoginRecordEvent.fail(username, ipaddr, browser, os, e.getMessage()));
            throw e;
        }
    }

    @GetMapping(value = "code")
    public HttpResult getCode() {
        // 字符类型验证码
        SpecCaptcha captcha = new SpecCaptcha(111, 36);
        // 几位字符，默认是4位
        captcha.setLen(4);
        // 生成验证码图片（必须先调用此方法生成文本）
        String img = captcha.toBase64();
        // 获取验证码文本
        String result = captcha.text();
        String uuid = Constants.CODE_KEY + IdUtil.simpleUUID();
        // 安全：不记录验证码明文到日志
        log.debug("Captcha generated - UUID: {}", uuid);
        // 保存到 Redis
        redisUtils.set(uuid, result, Constants.CODE_EXPIRE_TIME, TimeUnit.MINUTES);
        // 验证码信息
        Map<String, Object> imgResult = new HashMap<String, Object>(2) {{
            put("img", img);
            put("uuid", uuid);
        }};
        return HttpResult.ok(imgResult);
    }

    /**
     * 用户登出
     * <p>将当前用户的 token 加入黑名单，使其失效
     *
     * @return 操作结果
     */
    @DeleteMapping(value = "logout")
    public HttpResult logout() {
        try {
            // 获取当前请求的 token
            String token = jwtTokenService.getToken(ServletUtils.getRequest());
            if (token != null) {
                // 将 token 加入黑名单，使其失效
                onlineService.addToBlacklist(token);
                log.info("用户登出成功");
            }
            return HttpResult.ok();
        } catch (Exception e) {
            log.error("登出失败", e);
            return HttpResult.ok();  // 即使失败也返回成功，避免前端错误提示
        }
    }

    /**
     * 测试登录接口 - 跳过验证码检查
     * 仅用于自动化测试，生产环境应禁用
     */
    @Profile("dev")
    @PostMapping(value = "testLogin")
    public HttpResult testLogin(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String ipaddr = IpUtils.getIp(ServletUtils.getRequest());
        String browser = IpUtils.getBrowser(ServletUtils.getRequest());
        String os = IpUtils.getOs(ServletUtils.getRequest());

        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            eventPublisher.publishEvent(LoginRecordEvent.fail(username, ipaddr, browser, os, "用户名和密码不能为空"));
            return HttpResult.error("用户名和密码不能为空");
        }

        try {
            String token = jwtTokenService.login(username, password, authenticationManagerBuilder);
            eventPublisher.publishEvent(LoginRecordEvent.success(username, ipaddr, browser, os));
            return HttpResult.ok(properties.getTokenStartWith().concat(token));
        } catch (AuthenticationException e) {
            eventPublisher.publishEvent(LoginRecordEvent.fail(username, ipaddr, browser, os, e.getMessage()));
            throw e;
        }
    }

}

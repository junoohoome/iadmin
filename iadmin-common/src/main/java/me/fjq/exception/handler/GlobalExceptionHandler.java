package me.fjq.exception.handler;

import lombok.extern.slf4j.Slf4j;
import me.fjq.core.HttpResult;
import me.fjq.exception.BadRequestException;
import me.fjq.exception.JwtTokenException;
import me.fjq.utils.ThrowableUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 *
 * <p>使用 Java 21 instanceof 模式匹配优化异常处理逻辑
 *
 * @author fjq
 * @date 2020/03/18
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有不可知的异常
     * 使用 Java 21 instanceof 模式匹配进行类型判断
     */
    @ExceptionHandler(Throwable.class)
    public HttpResult handleException(Throwable e) {
        log.error(ThrowableUtil.getStackTrace(e));

        // Java 21 instanceof 模式匹配：同时进行类型检查和类型转换
        // 变量 bre, bce, iae 自动转换为目标类型，无需手动强制转换
        if (e instanceof BadRequestException bre) {
            return HttpResult.error(bre.getStatus(), bre.getMessage());
        } else if (e instanceof BadCredentialsException bce) {
            String message = "坏的凭证".equals(bce.getMessage()) ? "用户名或密码不正确" : bce.getMessage();
            return HttpResult.error(message);
        } else if (e instanceof IllegalArgumentException iae) {
            return HttpResult.error(iae.getMessage());
        }

        return HttpResult.error(e.getMessage());
    }

    /**
     * 坏的凭证异常
     */
    @ExceptionHandler(BadCredentialsException.class)
    public HttpResult badCredentialsException(BadCredentialsException e) {
        String message = "坏的凭证".equals(e.getMessage()) ? "用户名或密码不正确" : e.getMessage();
        log.error(message);
        return HttpResult.error(message);
    }

    /**
     * 处理自定义异常
     */
    @ExceptionHandler(value = BadRequestException.class)
    public HttpResult badRequestException(BadRequestException e) {
        log.error(ThrowableUtil.getStackTrace(e));
        return HttpResult.error(e.getStatus(), e.getMessage());
    }

    /**
     * 处理令牌异常
     */
    @ExceptionHandler(value = JwtTokenException.class)
    public HttpResult jwtTokenException(JwtTokenException e) {
        log.error(ThrowableUtil.getStackTrace(e));
        return HttpResult.error(e.getStatus(), e.getMessage());
    }

}

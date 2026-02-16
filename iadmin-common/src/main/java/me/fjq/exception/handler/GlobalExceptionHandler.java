package me.fjq.exception.handler;

import lombok.extern.slf4j.Slf4j;
import me.fjq.core.HttpResult;
import me.fjq.exception.BadRequestException;
import me.fjq.exception.JwtTokenException;
import me.fjq.utils.ThrowableUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

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
     * 处理参数校验异常（@RequestBody）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public HttpResult handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败: {}", message);
        return HttpResult.error(HttpStatus.BAD_REQUEST.value(), message);
    }

    /**
     * 处理参数绑定异常（@ModelAttribute）
     */
    @ExceptionHandler(BindException.class)
    public HttpResult handleBindException(BindException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", message);
        return HttpResult.error(HttpStatus.BAD_REQUEST.value(), message);
    }

    /**
     * 处理所有不可知的异常
     * <p>仅处理未在下面特定处理器中定义的异常
     */
    @ExceptionHandler(Throwable.class)
    public HttpResult handleException(Throwable e) {
        log.error("未处理的异常: {}", ThrowableUtil.getStackTrace(e));
        return HttpResult.error("系统异常，请稍后重试");
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

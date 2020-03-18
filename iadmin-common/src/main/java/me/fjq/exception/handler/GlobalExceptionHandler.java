package me.fjq.exception.handler;

import lombok.extern.slf4j.Slf4j;
import me.fjq.exception.BadRequestException;
import me.fjq.utils.ThrowableUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Zheng Jie
 * @date 2018-11-23
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理所有不可知的异常
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiError> handleException(Throwable e){
        // 打印堆栈信息
        log.error(ThrowableUtil.getStackTrace(e));
        return buildResponseEntity(ApiError.error(e.getMessage()));
    }

    /**
     * BadCredentialsException
     */
//    @ExceptionHandler(BadCredentialsException.class)
//    public ResponseEntity<ApiError> badCredentialsException(BadCredentialsException e){
//        // 打印堆栈信息
//        String message = "坏的凭证".equals(e.getMessage()) ? "用户名或密码不正确" : e.getMessage();
//        log.error(message);
//        return buildResponseEntity(ApiError.error(message));
//    }

    /**
     * 处理自定义异常
     */
	@ExceptionHandler(value = BadRequestException.class)
	public ResponseEntity<ApiError> badRequestException(BadRequestException e) {
        // 打印堆栈信息
        log.error(ThrowableUtil.getStackTrace(e));
        return buildResponseEntity(ApiError.error(e.getStatus(),e.getMessage()));
	}


    /**
     * 统一返回
     */
    private ResponseEntity<ApiError> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, HttpStatus.valueOf(apiError.getStatus()));
    }
}

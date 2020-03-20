package me.fjq.core;

import me.fjq.enums.ErrorCode;
import lombok.Data;
import org.springframework.http.HttpStatus;


@Data
public class HttpResult<T> {

    private int code;
    private String msg;
    private T data;


    private HttpResult(HttpStatus httpStatus) {
        this(httpStatus.value(), httpStatus.getReasonPhrase(), null);
    }

    private HttpResult(HttpStatus httpStatus, T data) {
        this(httpStatus.value(), httpStatus.getReasonPhrase(), data);
    }

    private HttpResult(int code, String msg) {
        this(code, msg, null);
    }

    private HttpResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> HttpResult<T> ok() {
        return new HttpResult<>(HttpStatus.OK);
    }

    public static <T> HttpResult<T> ok(T data) {
        return new HttpResult<>(HttpStatus.OK, data);
    }

    public static <T> HttpResult<T> error() {
        return new HttpResult<>(HttpStatus.BAD_REQUEST);
    }

    public static <T> HttpResult<T> error(HttpStatus httpStatus) {
        return new HttpResult<>(httpStatus);
    }

    public static <T> HttpResult<T> error(ErrorCode errorCode) {
        return new HttpResult<>(errorCode.code, errorCode.msg);
    }

    public static <T> HttpResult<T> error(Integer code, String msg) {
        return new HttpResult<>(code, msg);
    }

    public static <T> HttpResult<T> error(String msg) {
        return new HttpResult<>(HttpStatus.BAD_REQUEST.value(), msg);
    }

    public static <T> HttpResult<T> unauthorized(String msg) {
        return new HttpResult<>(HttpStatus.UNAUTHORIZED.value(), msg);
    }
}

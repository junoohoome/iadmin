package me.fjq.Domain;

import me.fjq.enums.ErrorCode;
import lombok.Data;
import org.springframework.http.HttpStatus;


@Data
public class ResponseEntity<T> {

    private int code;
    private String msg;
    private T data;


    private ResponseEntity(HttpStatus httpStatus) {
        this(httpStatus.value(), httpStatus.getReasonPhrase(), null);
    }

    private ResponseEntity(HttpStatus httpStatus, T data) {
        this(httpStatus.value(), httpStatus.getReasonPhrase(), data);
    }

    private ResponseEntity(int code, String msg) {
        this(code, msg, null);
    }

    private ResponseEntity(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ResponseEntity<T> ok() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public static <T> ResponseEntity<T> ok(T data) {
        return new ResponseEntity<>(HttpStatus.OK, data);
    }

    public static <T> ResponseEntity<T> error() {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    public static <T> ResponseEntity<T> error(HttpStatus httpStatus) {
        return new ResponseEntity<>(httpStatus);
    }

    public static <T> ResponseEntity<T> error(ErrorCode errorCode) {
        return new ResponseEntity<>(errorCode.code, errorCode.msg);
    }

    public static <T> ResponseEntity<T> error(Integer code, String msg) {
        return new ResponseEntity<>(code, msg);
    }

    public static <T> ResponseEntity<T> error(String msg) {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST.value(), msg);
    }

    public static <T> ResponseEntity<T> unauthorized(String msg) {
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED.value(), msg);
    }
}

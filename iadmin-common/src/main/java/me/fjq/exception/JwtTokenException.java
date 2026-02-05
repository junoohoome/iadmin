package me.fjq.exception;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * 令牌异常
 *
 * @author fjq
 */
public class JwtTokenException extends RuntimeException {

    private Integer status = INTERNAL_SERVER_ERROR.value();

    public JwtTokenException(String msg) {
        super(msg);
    }

    public JwtTokenException(HttpStatus status, String msg) {
        super(msg);
        this.status = status.value();
    }

    public Integer getStatus() {
        return status;
    }
}

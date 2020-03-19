package me.fjq.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

/**
 * HTTP结果封装
 *
 * @author FJQ
 * @date 2020/03/18
 */
@Getter
@Setter
public class HttpResult {

    private int code = 200;
    private String msg;
    private Object data;

    public static HttpResult error() {
        return error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "未知异常，请联系管理员");
    }

    public static HttpResult error(String msg) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR.value(), msg);
    }

    public static HttpResult error(int code, String msg) {
        HttpResult r = new HttpResult();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

    public static HttpResult ok(String msg) {
        HttpResult r = new HttpResult();
        r.setMsg(msg);
        return r;
    }

    public static HttpResult ok(Object data) {
        HttpResult r = new HttpResult();
        r.setData(data);
        return r;
    }

    public static HttpResult ok() {
        return new HttpResult();
    }

}

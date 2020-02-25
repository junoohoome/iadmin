package me.fjq.enums;


public enum ErrorCode {

    IMAG_CODE_EXPIRED(10000, "验证码已过期"),
    IMAG_CODE_ERROR(10001, "验证码不正确"),
    ACCOUNT_ERROE(10002, "账号或密码不正确"),
    FREQUENT_REQUEST(10003, "请求频繁"),
    SYS_ERROR(-1, "系统繁忙，请稍后操作");

    public int code;
    public String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}

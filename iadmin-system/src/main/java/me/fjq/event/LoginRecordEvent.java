package me.fjq.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录记录事件
 */
@Getter
@AllArgsConstructor
public class LoginRecordEvent {

    /** 用户名 */
    private final String username;

    /** IP地址 */
    private final String ipaddr;

    /** 浏览器 */
    private final String browser;

    /** 操作系统 */
    private final String os;

    /** 状态: 0=成功, 1=失败 */
    private final Integer status;

    /** 消息 */
    private final String message;

    /**
     * 创建成功事件
     */
    public static LoginRecordEvent success(String username, String ipaddr, String browser, String os) {
        return new LoginRecordEvent(username, ipaddr, browser, os, 0, "登录成功");
    }

    /**
     * 创建失败事件
     */
    public static LoginRecordEvent fail(String username, String ipaddr, String browser, String os, String message) {
        return new LoginRecordEvent(username, ipaddr, browser, os, 1, message);
    }
}

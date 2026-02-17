package me.fjq.monitor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import me.fjq.monitor.entity.Logininfor;

/**
 * 系统访问记录(Logininfor)表服务接口
 *
 * @author fjq
 * @since 2020-03-23 22:43:48
 */
public interface LogininforService extends IService<Logininfor> {

    /**
     * 记录登录日志
     *
     * @param userName   用户名
     * @param status     登录状态（0成功 1失败）
     * @param ipaddr     IP地址
     * @param loginLocation 登录地点
     * @param browser    浏览器
     * @param os         操作系统
     * @param msg        提示消息
     */
    void recordLoginLog(String userName, String status, String ipaddr,
                       String loginLocation, String browser, String os, String msg);

    /**
     * 记录登录成功日志
     *
     * @param userName 用户名
     * @param ipaddr   IP地址
     * @param browser  浏览器
     * @param os       操作系统
     */
    default void recordLoginSuccess(String userName, String ipaddr, String browser, String os) {
        recordLoginLog(userName, "0", ipaddr, "内网IP", browser, os, "登录成功");
    }

    /**
     * 记录登录失败日志
     *
     * @param userName 用户名
     * @param ipaddr   IP地址
     * @param browser  浏览器
     * @param os       操作系统
     * @param msg      失败原因
     */
    default void recordLoginFail(String userName, String ipaddr, String browser, String os, String msg) {
        recordLoginLog(userName, "1", ipaddr, "内网IP", browser, os, msg);
    }
}

package me.fjq.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import me.fjq.config.AsyncConfig;
import me.fjq.system.entity.SysLogininfor;
import me.fjq.system.mapper.SysLogininforMapper;
import me.fjq.system.service.SysLogininforService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 系统访问记录(SysLogininfor)表服务实现类
 *
 * @author fjq
 * @since 2020-03-23 22:43:48
 */
@Slf4j
@Service("sysLogininforService")
public class SysLogininforServiceImpl extends ServiceImpl<SysLogininforMapper, SysLogininfor> implements SysLogininforService {

    /**
     * 异步记录登录日志
     *
     * @param userName      用户名
     * @param status        登录状态（0成功 1失败）
     * @param ipaddr        IP地址
     * @param loginLocation 登录地点
     * @param browser       浏览器
     * @param os            操作系统
     * @param msg           提示消息
     */
    @Async(AsyncConfig.TASK_EXECUTOR)
    @Override
    public void recordLoginLog(String userName, String status, String ipaddr,
                               String loginLocation, String browser, String os, String msg) {
        try {
            SysLogininfor logininfor = new SysLogininfor();
            logininfor.setUserName(userName);
            logininfor.setStatus(status);
            logininfor.setIpaddr(ipaddr);
            logininfor.setLoginLocation(loginLocation);
            logininfor.setBrowser(browser);
            logininfor.setOs(os);
            logininfor.setMsg(msg);
            logininfor.setLoginTime(new Date());

            save(logininfor);
            log.debug("登录日志记录成功: {} - {}", userName, msg);
        } catch (Exception e) {
            log.error("登录日志记录失败", e);
        }
    }
}

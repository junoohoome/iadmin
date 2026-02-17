package me.fjq.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.monitor.service.LogininforService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 登录记录事件监听器 - 异步处理登录日志
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginRecordListener {

    private final LogininforService logininforService;

    @Async("loginLogExecutor")
    @EventListener
    public void handleLoginEvent(LoginRecordEvent event) {
        try {
            if (event.getStatus() == 0) {
                logininforService.recordLoginSuccess(
                        event.getUsername(),
                        event.getIpaddr(),
                        event.getBrowser(),
                        event.getOs()
                );
            } else {
                logininforService.recordLoginFail(
                        event.getUsername(),
                        event.getIpaddr(),
                        event.getBrowser(),
                        event.getOs(),
                        event.getMessage()
                );
            }
        } catch (Exception e) {
            log.error("异步记录登录日志失败: username={}, error={}",
                    event.getUsername(), e.getMessage());
        }
    }
}

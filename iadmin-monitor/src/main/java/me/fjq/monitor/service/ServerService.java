package me.fjq.monitor.service;

import lombok.extern.slf4j.Slf4j;
import me.fjq.monitor.entity.Server;
import org.springframework.stereotype.Service;

/**
 * 服务器监控服务
 *
 * @author fjq
 * @since 2025-02-05
 */
@Slf4j
@Service
public class ServerService {

    /**
     * 获取服务器信息
     *
     * @return 服务器信息
     */
    public Server getServerInfo() {
        try {
            Server server = new Server();
            server.copyTo();
            return server;
        } catch (Exception e) {
            log.error("获取服务器信息失败", e);
            return new Server();
        }
    }

}

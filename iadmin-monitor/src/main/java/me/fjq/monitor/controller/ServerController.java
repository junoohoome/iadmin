package me.fjq.monitor.controller;

import lombok.AllArgsConstructor;
import me.fjq.core.HttpResult;
import me.fjq.monitor.entity.Server;
import me.fjq.monitor.service.ServerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务器监控 Controller
 *
 * @author fjq
 * @since 2025-02-05
 */
@RestController
@RequestMapping("/monitor/server")
@AllArgsConstructor
public class ServerController {

    private final ServerService serverService;

    /**
     * 获取服务器监控信息
     *
     * @return 服务器信息
     */
    @GetMapping
    @PreAuthorize("@ss.hasPerms('monitor:server:list')")
    public HttpResult<Server> getInfo() throws Exception {
        Server server = serverService.getServerInfo();
        return HttpResult.ok(server);
    }

}

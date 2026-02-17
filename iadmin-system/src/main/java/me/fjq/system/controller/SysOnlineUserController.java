package me.fjq.system.controller;

import lombok.AllArgsConstructor;
import me.fjq.core.HttpResult;
import me.fjq.system.entity.OnlineUser;
import me.fjq.system.service.OnlineUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 在线用户 Controller
 *
 * @author fjq
 * @since 2025-02-05
 */
@RestController
@RequestMapping("/system/online")
@AllArgsConstructor
public class SysOnlineUserController {

    private final OnlineUserService onlineUserService;

    /**
     * 获取在线用户列表
     *
     * @return 在线用户列表
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPerms('system:online:list')")
    public HttpResult<List<OnlineUser>> list() {
        List<OnlineUser> onlineUsers = onlineUserService.getOnlineUsers();
        return HttpResult.ok(onlineUsers);
    }

    /**
     * 强制用户下线
     *
     * @param token 登录令牌
     * @return 操作结果
     */
    @DeleteMapping("/{token}")
    @PreAuthorize("@ss.hasPerms('system:online:forceLogout')")
    public HttpResult<Boolean> forceLogout(@PathVariable String token) {
        onlineUserService.addToBlacklist(token);
        return HttpResult.ok(true);
    }

    /**
     * 根据用户名强制下线
     *
     * @param username 用户名
     * @return 强制下线的用户数量
     */
    @DeleteMapping("/username/{username}")
    @PreAuthorize("@ss.hasPerms('system:online:forceLogout')")
    public HttpResult<Integer> forceLogoutByUsername(@PathVariable String username) {
        int count = onlineUserService.forceLogout(username);
        return HttpResult.ok(count);
    }

    /**
     * 检查用户是否在线
     *
     * @param token 登录令牌
     * @return 是否在线
     */
    @GetMapping("/check/{token}")
    public HttpResult<Boolean> checkOnline(@PathVariable String token) {
        return HttpResult.ok(onlineUserService.isOnline(token));
    }

}

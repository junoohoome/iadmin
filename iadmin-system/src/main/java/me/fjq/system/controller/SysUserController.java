package me.fjq.system.controller;


import me.fjq.system.domain.SysUser;
import me.fjq.system.service.ISysUserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author fjq
 */
@RestController
@RequestMapping("/user")
public class SysUserController {

    private final ISysUserService sysUserService;

    public SysUserController(ISysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    public void getById(String id) {
        SysUser sysUser = sysUserService.getById(id);
    }

}

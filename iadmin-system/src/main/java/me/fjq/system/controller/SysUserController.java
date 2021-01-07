package me.fjq.system.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import me.fjq.core.HttpResult;
import me.fjq.security.JwtTokenService;
import me.fjq.security.JwtUserDetails;
import me.fjq.system.entity.SysUser;
import me.fjq.system.query.SysUserQuery;
import me.fjq.system.service.SysRoleService;
import me.fjq.system.service.SysUserService;
import me.fjq.utils.ServletUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;


/**
 * 用户信息表(SysUser)表控制层
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@AllArgsConstructor
@RestController
@RequestMapping("sysUser")
public class SysUserController {
    /**
     * 服务对象
     */
    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 分页查询所有数据
     *
     * @param page  分页对象
     * @param query 查询实体
     * @return 所有数据
     */
    @PreAuthorize("@ss.hasPerms('admin,system:user:list')")
    @GetMapping
    public HttpResult selectAll(Page page, SysUserQuery query) {
        return HttpResult.ok(this.sysUserService.selectPage(page, query));
    }

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("{id}")
    public HttpResult selectOne(@PathVariable Serializable id) {
        return HttpResult.ok(this.sysUserService.getById(id));
    }

    /**
     * 新增数据
     *
     * @param sysUser 实体对象
     * @return 新增结果
     */
    @PreAuthorize("@ss.hasPerms('admin,system:user:add')")
    @PostMapping
    public HttpResult insert(@RequestBody SysUser sysUser) {
        sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        return HttpResult.ok(this.sysUserService.save(sysUser));
    }

    /**
     * 修改数据
     *
     * @param sysUser 实体对象
     * @return 修改结果
     */
    @PreAuthorize("@ss.hasPerms('admin,system:user:edit')")
    @PutMapping
    public HttpResult update(@RequestBody SysUser sysUser) {
        return HttpResult.ok(this.sysUserService.updateById(sysUser));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合
     * @return 删除结果
     */
    @PreAuthorize("@ss.hasPerms('admin,system:user:del')")
    @DeleteMapping
    public HttpResult delete(@RequestParam("idList") List<Long> idList) {
        return HttpResult.ok(this.sysUserService.removeByIds(idList));
    }

    @PutMapping("update/status")
    public HttpResult<Boolean> updateStatus(@RequestParam("id") Long id, @RequestParam("status") String status) {
        SysUser sysUser = new SysUser();
        sysUser.setStatus(status);
        sysUser.setUserId(id);
        return HttpResult.ok(this.sysUserService.updateById(sysUser));
    }

    @GetMapping("profile")
    public HttpResult<Object> getUserProfile() {
        JwtUserDetails jwtUserDetails = jwtTokenService.getJwtUserDetails(ServletUtils.getRequest());
        return HttpResult.ok(sysUserService.getById(jwtUserDetails.getId()));
    }

    @PutMapping("profile")
    public HttpResult<Boolean> updateUserProfile(@RequestBody SysUser entity) {
        SysUser sysUser = new SysUser();
        sysUser.setNickName(entity.getNickName());
        sysUser.setMobile(entity.getMobile());
        sysUser.setEmail(entity.getEmail());
        sysUser.setUserId(entity.getUserId());
        return HttpResult.ok(sysUserService.updateById(sysUser));
    }

    @PutMapping("profile/updatePwd")
    public HttpResult<Boolean> updateUserPwd(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword) {
        JwtUserDetails jwtUserDetails = jwtTokenService.getJwtUserDetails(ServletUtils.getRequest());
        SysUser user = sysUserService.getById(jwtUserDetails.getId());
        if (passwordEncoder.matches(oldPassword, user.getPassword())) {
            SysUser sysUser = new SysUser();
            sysUser.setPassword(passwordEncoder.encode(newPassword));
            sysUser.setUserId(jwtUserDetails.getId());
            sysUserService.updateById(sysUser);
            return HttpResult.ok();
        }
        return HttpResult.error("修改密码错误");
    }
}
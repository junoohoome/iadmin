package me.fjq.system.controller;


import lombok.extern.slf4j.Slf4j;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import me.fjq.annotation.Log;
import me.fjq.core.HttpResult;
import me.fjq.security.JwtTokenService;
import me.fjq.security.JwtUserDetails;
import me.fjq.system.entity.SysUser;
import me.fjq.system.query.SysUserQuery;
import me.fjq.system.service.SysRoleService;
import me.fjq.system.service.SysUserService;
import me.fjq.utils.ServletUtils;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import me.fjq.system.vo.system.SysUserVo;


/**
 * 用户信息表(SysUser)表控制层
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@RestController
@RequestMapping("sysUser")
@Slf4j
public class SysUserController {
    /**
     * 服务对象
     */
    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    @Value("${file.avatar:./avatar/}")
    private String avatarPath;

    @Value("${file.avatarMaxSize:5}")
    private int avatarMaxSize;

    public SysUserController(SysUserService sysUserService, SysRoleService sysRoleService,
                            JwtTokenService jwtTokenService, PasswordEncoder passwordEncoder) {
        this.sysUserService = sysUserService;
        this.sysRoleService = sysRoleService;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
    }

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
        Page<SysUserVo> result = this.sysUserService.selectPage(page, query);
        log.debug("selectPage result - records: {}, total: {}", result.getRecords() != null ? result.getRecords().size() : 0, result.getTotal());
        return HttpResult.ok(result);
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
    @Log(title = "用户管理", businessType = 1)
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
    @Log(title = "用户管理", businessType = 2)
    @PreAuthorize("@ss.hasPerms('admin,system:user:edit')")
    @PutMapping
    public HttpResult update(@RequestBody SysUser sysUser) {
        return HttpResult.ok(this.sysUserService.updateById(sysUser));
    }

    /**
     * 删除数据
     *
     * @param idList 主键结合 (comma-separated string)
     * @return 删除结果
     */
    @Log(title = "用户管理", businessType = 3)
    @PreAuthorize("@ss.hasPerms('admin,system:user:del')")
    @DeleteMapping("{idList}")
    public HttpResult delete(@PathVariable String idList) {
        List<Long> ids = Arrays.stream(idList.split(","))
            .map(Long::valueOf)
            .collect(Collectors.toList());
        return HttpResult.ok(this.sysUserService.removeByIds(ids));
    }

    @PutMapping("update/status")
    public HttpResult<Boolean> updateStatus(@RequestParam("id") Long id, @RequestParam("status") String status) {
        SysUser sysUser = new SysUser();
        sysUser.setStatus(status);
        sysUser.setUserId(id);
        return HttpResult.ok(this.sysUserService.updateById(sysUser));
    }

    /**
     * 获取当前登录用户信息
     * <p>直接从 JWT Token 中获取，避免重复查询数据库
     *
     * @return 用户信息
     */
    @GetMapping("profile")
    public HttpResult<Object> getUserProfile() {
        JwtUserDetails jwtUserDetails = jwtTokenService.getJwtUserDetails(ServletUtils.getRequest());
        return HttpResult.ok(jwtUserDetails);
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

    /**
     * 重置用户密码（管理员功能）
     *
     * @param userId 用户ID
     * @param password 新密码
     * @return 操作结果
     */
    @PreAuthorize("@ss.hasPerms('admin,system:user:resetPwd')")
    @PostMapping("resetPwd")
    public HttpResult<Boolean> resetPassword(@RequestParam("userId") Long userId, @RequestParam("password") String password) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setPassword(passwordEncoder.encode(password));
        return HttpResult.ok(sysUserService.updateById(sysUser));
    }

    /**
     * 上传用户头像
     *
     * @param avatarFile 头像文件
     * @return 上传结果（头像访问URL）
     */
    @Log(title = "用户管理", function = "上传头像")
    @PostMapping("profile/avatar")
    public HttpResult<String> uploadAvatar(@RequestParam("avatarFile") MultipartFile avatarFile) {
        JwtUserDetails jwtUserDetails = jwtTokenService.getJwtUserDetails(ServletUtils.getRequest());
        SysUser user = sysUserService.getById(jwtUserDetails.getId());

        if (user == null) {
            return HttpResult.error("用户不存在");
        }

        // 1. 文件校验
        if (avatarFile == null || avatarFile.isEmpty()) {
            return HttpResult.error("请选择要上传的文件");
        }

        // 2. 文件类型校验
        String contentType = avatarFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return HttpResult.error("只能上传图片文件");
        }

        // 3. 文件大小校验（MB）
        long fileSizeMB = avatarFile.getSize() / (1024 * 1024);
        if (fileSizeMB > avatarMaxSize) {
            return HttpResult.error("文件大小不能超过" + avatarMaxSize + "MB");
        }

        try {
            // 4. 生成唯一文件名
            String originalFilename = avatarFile.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

            // 5. 确保目录存在
            File dir = new File(avatarPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 6. 保存文件
            File destFile = new File(avatarPath + fileName);
            avatarFile.transferTo(destFile);

            // 7. 更新用户头像字段
            String avatarUrl = "/avatar/" + fileName;
            SysUser updateUser = new SysUser();
            updateUser.setUserId(user.getUserId());
            updateUser.setAvatar(avatarUrl);
            sysUserService.updateById(updateUser);

            log.info("用户头像上传成功: userId={}, avatar={}", user.getUserId(), avatarUrl);
            return HttpResult.ok(avatarUrl);

        } catch (IOException e) {
            log.error("头像上传失败", e);
            return HttpResult.error("文件上传失败，请稍后重试");
        }
    }
}
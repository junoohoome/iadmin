package me.fjq.system.controller;

import lombok.AllArgsConstructor;
import me.fjq.core.HttpResult;
import me.fjq.system.service.SysRoleDeptService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色数据权限配置 Controller
 *
 * @author fjq
 * @since 2025-02-05
 */
@RestController
@RequestMapping("/system/role/dataScope")
@AllArgsConstructor
public class SysRoleDeptController {

    private final SysRoleDeptService roleDeptService;

    /**
     * 保存角色数据权限配置
     *
     * @param roleId  角色ID
     * @param deptIds 部门ID列表
     * @return 操作结果
     */
    @PostMapping("/{roleId}")
    @PreAuthorize("@ss.hasPerms('system:role:edit')")
    public HttpResult<Boolean> saveDataScope(
            @PathVariable Long roleId,
            @RequestBody List<Long> deptIds) {
        return HttpResult.ok(roleDeptService.saveRoleDataScope(roleId, deptIds));
    }

    /**
     * 查询角色的数据权限部门列表
     *
     * @param roleId 角色ID
     * @return 部门ID列表
     */
    @GetMapping("/{roleId}")
    @PreAuthorize("@ss.hasPerms('system:role:query')")
    public HttpResult<List<Long>> getDataScope(@PathVariable Long roleId) {
        return HttpResult.ok(roleDeptService.selectDeptIdsByRoleId(roleId));
    }

    /**
     * 删除角色数据权限配置
     *
     * @param roleId 角色ID
     * @return 操作结果
     */
    @DeleteMapping("/{roleId}")
    @PreAuthorize("@ss.hasPerms('system:role:remove')")
    public HttpResult<Boolean> deleteDataScope(@PathVariable Long roleId) {
        return HttpResult.ok(roleDeptService.deleteByRoleId(roleId));
    }

}

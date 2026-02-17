package me.fjq.quartz.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.annotation.Log;
import me.fjq.core.HttpResult;
import me.fjq.quartz.entity.SysJobLog;
import me.fjq.quartz.service.SysJobLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 定时任务调度日志 Controller
 *
 * @author fjq
 */
@Slf4j
@RestController
@RequestMapping("/monitor/jobLog")
@AllArgsConstructor
public class SysJobLogController {

    private final SysJobLogService sysJobLogService;

    /**
     * 查询定时任务日志列表
     */
    @PreAuthorize("@ss.hasPerms('monitor:job:list')")
    @GetMapping("/list")
    public HttpResult<Page<SysJobLog>> list(Page<SysJobLog> page, SysJobLog sysJobLog) {
        Page<SysJobLog> result = sysJobLogService.page(page,
                Wrappers.<SysJobLog>lambdaQuery()
                        .like(sysJobLog.getJobName() != null, SysJobLog::getJobName, sysJobLog.getJobName())
                        .eq(sysJobLog.getJobGroup() != null, SysJobLog::getJobGroup, sysJobLog.getJobGroup())
                        .eq(sysJobLog.getStatus() != null, SysJobLog::getStatus, sysJobLog.getStatus())
                        .orderByDesc(SysJobLog::getCreateTime)
        );
        return HttpResult.ok(result);
    }

    /**
     * 获取定时任务日志详细信息
     */
    @PreAuthorize("@ss.hasPerms('monitor:job:query')")
    @GetMapping("/{jobLogId}")
    public HttpResult<SysJobLog> getInfo(@PathVariable Long jobLogId) {
        return HttpResult.ok(sysJobLogService.selectJobLogById(jobLogId));
    }

    /**
     * 删除定时任务日志
     */
    @Log(title = "定时任务日志", businessType = 3)
    @PreAuthorize("@ss.hasPerms('monitor:job:remove')")
    @DeleteMapping("/{jobLogIds}")
    public HttpResult<Boolean> remove(@PathVariable Long[] jobLogIds) {
        return HttpResult.ok(sysJobLogService.deleteJobLogByIds(jobLogIds));
    }

    /**
     * 清空定时任务日志
     */
    @Log(title = "定时任务日志", function = "清空日志")
    @PreAuthorize("@ss.hasPerms('monitor:job:remove')")
    @DeleteMapping("/clean")
    public HttpResult<Void> clean() {
        sysJobLogService.cleanJobLog();
        return HttpResult.ok();
    }
}

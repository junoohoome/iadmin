package me.fjq.quartz.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.annotation.Log;
import me.fjq.core.HttpResult;
import me.fjq.quartz.entity.SysJob;
import me.fjq.quartz.service.SysJobService;
import me.fjq.quartz.util.CronUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 定时任务调度 Controller
 *
 * @author fjq
 */
@Slf4j
@RestController
@RequestMapping("/monitor/job")
@AllArgsConstructor
public class SysJobController {

    private final SysJobService sysJobService;

    /**
     * 查询定时任务列表
     */
    @PreAuthorize("@ss.hasPerms('monitor:job:list')")
    @GetMapping("/list")
    public HttpResult<Page<SysJob>> list(Page<SysJob> page, SysJob sysJob) {
        Page<SysJob> result = sysJobService.page(page,
                Wrappers.<SysJob>lambdaQuery()
                        .like(sysJob.getJobName() != null, SysJob::getJobName, sysJob.getJobName())
                        .eq(sysJob.getJobGroup() != null, SysJob::getJobGroup, sysJob.getJobGroup())
                        .eq(sysJob.getStatus() != null, SysJob::getStatus, sysJob.getStatus())
                        .orderByAsc(SysJob::getJobId)
        );
        return HttpResult.ok(result);
    }

    /**
     * 获取定时任务详细信息
     */
    @PreAuthorize("@ss.hasPerms('monitor:job:query')")
    @GetMapping("/{jobId}")
    public HttpResult<SysJob> getInfo(@PathVariable Long jobId) {
        return HttpResult.ok(sysJobService.selectJobById(jobId));
    }

    /**
     * 新增定时任务
     */
    @Log(title = "定时任务", businessType = 1)
    @PreAuthorize("@ss.hasPerms('monitor:job:add')")
    @PostMapping
    public HttpResult<Boolean> add(@RequestBody SysJob job) {
        if (!sysJobService.checkCronExpressionIsValid(job)) {
            return HttpResult.error("cron表达式不正确");
        }
        return HttpResult.ok(sysJobService.insertJob(job));
    }

    /**
     * 修改定时任务
     */
    @Log(title = "定时任务", businessType = 2)
    @PreAuthorize("@ss.hasPerms('monitor:job:edit')")
    @PutMapping
    public HttpResult<Boolean> edit(@RequestBody SysJob job) {
        if (!sysJobService.checkCronExpressionIsValid(job)) {
            return HttpResult.error("cron表达式不正确");
        }
        return HttpResult.ok(sysJobService.updateJob(job));
    }

    /**
     * 删除定时任务
     */
    @Log(title = "定时任务", businessType = 3)
    @PreAuthorize("@ss.hasPerms('monitor:job:remove')")
    @DeleteMapping("/{jobIds}")
    public HttpResult<Boolean> remove(@PathVariable Long[] jobIds) {
        return HttpResult.ok(sysJobService.deleteJobByIds(jobIds));
    }

    /**
     * 修改定时任务状态
     */
    @Log(title = "定时任务", function = "修改状态")
    @PreAuthorize("@ss.hasPerms('monitor:job:changeStatus')")
    @PutMapping("/changeStatus")
    public HttpResult<Boolean> changeStatus(@RequestBody SysJob job) {
        SysJob newJob = new SysJob();
        newJob.setJobId(job.getJobId());
        newJob.setStatus(job.getStatus());
        return HttpResult.ok(sysJobService.changeStatus(newJob));
    }

    /**
     * 立即执行任务
     */
    @Log(title = "定时任务", function = "立即执行")
    @PreAuthorize("@ss.hasPerms('monitor:job:run')")
    @PostMapping("/run")
    public HttpResult<Boolean> run(@RequestBody SysJob job) {
        return HttpResult.ok(sysJobService.run(sysJobService.selectJobById(job.getJobId())));
    }

    /**
     * 获取所有任务列表（不分页）
     */
    @GetMapping("/all")
    public HttpResult<List<SysJob>> listAll() {
        return HttpResult.ok(sysJobService.list());
    }
}

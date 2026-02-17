package me.fjq.quartz.job;

import lombok.extern.slf4j.Slf4j;
import me.fjq.quartz.entity.SysJob;
import me.fjq.quartz.entity.SysJobLog;
import me.fjq.quartz.mapper.SysJobLogMapper;
import me.fjq.quartz.util.JobInvokeUtil;
import me.fjq.quartz.util.SpringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

/**
 * 允许并发执行的 Job
 *
 * @author fjq
 */
@Slf4j
public class QuartzJobExecution implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SysJob sysJob = (SysJob) context.getMergedJobDataMap().get("TASK_PROPERTIES");
        executeJob(context, sysJob);
    }

    /**
     * 执行任务并记录日志
     */
    protected void executeJob(JobExecutionContext context, SysJob sysJob) {
        SysJobLogMapper jobLogMapper = SpringUtils.getBean(SysJobLogMapper.class);
        SysJobLog jobLog = new SysJobLog();
        jobLog.setJobName(sysJob.getJobName());
        jobLog.setJobGroup(sysJob.getJobGroup());
        jobLog.setInvokeTarget(sysJob.getInvokeTarget());
        jobLog.setCreateTime(new Date());

        long startTime = System.currentTimeMillis();
        try {
            // 执行任务
            JobInvokeUtil.invokeMethod(sysJob);
            long elapsed = System.currentTimeMillis() - startTime;
            jobLog.setJobMessage(sysJob.getJobName() + " 执行成功，耗时：" + elapsed + " 毫秒");
            jobLog.setStatus(SysJobLog.STATUS_SUCCESS);
            log.info("定时任务执行成功: {}", sysJob.getJobName());
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            jobLog.setJobMessage(sysJob.getJobName() + " 执行失败，耗时：" + elapsed + " 毫秒");
            jobLog.setStatus(SysJobLog.STATUS_FAIL);
            String exceptionInfo = e.getMessage();
            if (exceptionInfo != null && exceptionInfo.length() > 2000) {
                exceptionInfo = exceptionInfo.substring(0, 2000);
            }
            jobLog.setExceptionInfo(exceptionInfo);
            log.error("定时任务执行失败: {}", sysJob.getJobName(), e);
        }

        // 保存日志
        jobLogMapper.insert(jobLog);
    }
}

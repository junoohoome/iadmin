package me.fjq.quartz.util;

import lombok.extern.slf4j.Slf4j;
import me.fjq.quartz.entity.SysJob;
import me.fjq.quartz.job.QuartzDisallowConcurrentExecution;
import me.fjq.quartz.job.QuartzJobExecution;
import org.quartz.*;

/**
 * 定时任务工具类
 *
 * @author fjq
 */
@Slf4j
public class ScheduleUtils {

    private ScheduleUtils() {
    }

    /**
     * 任务调度参数 key
     */
    public static final String TASK_PROPERTIES = "TASK_PROPERTIES";

    /**
     * 获取触发器 key
     */
    public static TriggerKey getTriggerKey(Long jobId, String jobGroup) {
        return TriggerKey.triggerKey(String.valueOf(jobId), jobGroup);
    }

    /**
     * 获取任务 key
     */
    public static JobKey getJobKey(Long jobId, String jobGroup) {
        return JobKey.jobKey(String.valueOf(jobId), jobGroup);
    }

    /**
     * 创建定时任务
     */
    public static void createScheduleJob(Scheduler scheduler, SysJob job) throws SchedulerException {
        // 构建任务类
        Class<? extends Job> jobClass = SysJob.CONCURRENT_DISALLOW.equals(job.getConcurrent())
                ? QuartzDisallowConcurrentExecution.class
                : QuartzJobExecution.class;

        // 构建 jobDetail
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(getJobKey(jobId, jobGroup))
                .build();

        // 表达式调度构建器
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
        cronScheduleBuilder = handleCronScheduleMisfirePolicy(job, cronScheduleBuilder);

        // 按新的 cronExpression 构建一个新的 trigger
        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(getTriggerKey(jobId, jobGroup))
                .withSchedule(cronScheduleBuilder)
                .build();

        // 放入参数，运行时的方法可以获得
        jobDetail.getJobDataMap().put(TASK_PROPERTIES, job);

        // 判断是否存在
        if (scheduler.checkExists(getJobKey(jobId, jobGroup))) {
            // 防止创建时存在数据问题，先移除，然后在执行创建操作
            scheduler.deleteJob(getJobKey(jobId, jobGroup));
        }

        scheduler.scheduleJob(jobDetail, trigger);

        // 暂停任务
        if (SysJob.STATUS_PAUSE.equals(job.getStatus())) {
            scheduler.pauseJob(getJobKey(jobId, jobGroup));
        }
    }

    /**
     * 设置定时任务策略
     */
    public static CronScheduleBuilder handleCronScheduleMisfirePolicy(SysJob job, CronScheduleBuilder cb) {
        return switch (job.getMisfirePolicy()) {
            case SysJob.MISFIRE_RUN_NOW -> cb.withMisfireHandlingInstructionIgnoreMisfires();
            case SysJob.MISFIRE_IGNORE_MISFIRES -> cb.withMisfireHandlingInstructionIgnoreMisfires();
            case SysJob.MISFIRE_DO_NOTHING -> cb.withMisfireHandlingInstructionDoNothing();
            default -> cb;
        };
    }
}

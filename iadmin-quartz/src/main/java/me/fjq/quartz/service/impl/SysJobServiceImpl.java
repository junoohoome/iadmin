package me.fjq.quartz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.quartz.entity.SysJob;
import me.fjq.quartz.mapper.SysJobMapper;
import me.fjq.quartz.service.SysJobService;
import me.fjq.quartz.util.CronUtils;
import me.fjq.quartz.util.ScheduleUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.util.List;

/**
 * 定时任务调度 Service 实现类
 *
 * @author fjq
 */
@Slf4j
@AllArgsConstructor
@Service("sysJobService")
@Transactional(rollbackFor = Exception.class)
public class SysJobServiceImpl extends ServiceImpl<SysJobMapper, SysJob> implements SysJobService {

    private final Scheduler scheduler;
    private final SysJobMapper sysJobMapper;

    /**
     * 项目启动时，初始化定时器
     */
    @PostConstruct
    public void init() throws SchedulerException {
        scheduler.clear();
        List<SysJob> jobList = selectJobAll();
        for (SysJob job : jobList) {
            try {
                ScheduleUtils.createScheduleJob(scheduler, job);
            } catch (SchedulerException e) {
                log.error("初始化定时任务失败: {}", job.getJobName(), e);
            }
        }
    }

    @Override
    public List<SysJob> selectJobAll() {
        return sysJobMapper.selectList(
                new LambdaQueryWrapper<SysJob>()
                        .eq(SysJob::getStatus, SysJob.STATUS_NORMAL)
        );
    }

    @Override
    public SysJob selectJobById(Long jobId) {
        return sysJobMapper.selectById(jobId);
    }

    @Override
    public boolean insertJob(SysJob job) {
        // 设置默认值
        if (StringUtils.isBlank(job.getStatus())) {
            job.setStatus(SysJob.STATUS_NORMAL);
        }
        if (StringUtils.isBlank(job.getMisfirePolicy())) {
            job.setMisfirePolicy(SysJob.MISFIRE_DO_NOTHING);
        }
        if (StringUtils.isBlank(job.getConcurrent())) {
            job.setConcurrent(SysJob.CONCURRENT_ALLOW);
        }

        int rows = sysJobMapper.insert(job);
        if (rows > 0) {
            try {
                ScheduleUtils.createScheduleJob(scheduler, job);
            } catch (SchedulerException e) {
                log.error("创建定时任务失败", e);
                throw new RuntimeException("创建定时任务失败: " + e.getMessage());
            }
        }
        return rows > 0;
    }

    @Override
    public boolean updateJob(SysJob job) {
        SysJob oldJob = selectJobById(job.getJobId());
        if (oldJob == null) {
            return false;
        }

        int rows = sysJobMapper.updateById(job);
        if (rows > 0) {
            try {
                updateSchedulerJob(job, oldJob.getJobGroup());
            } catch (SchedulerException e) {
                log.error("更新定时任务失败", e);
                throw new RuntimeException("更新定时任务失败: " + e.getMessage());
            }
        }
        return rows > 0;
    }

    @Override
    public boolean deleteJob(SysJob job) {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();

        int rows = sysJobMapper.deleteById(jobId);
        if (rows > 0) {
            try {
                scheduler.deleteJob(ScheduleUtils.getJobKey(jobId, jobGroup));
            } catch (SchedulerException e) {
                log.error("删除定时任务失败", e);
            }
        }
        return rows > 0;
    }

    @Override
    public boolean deleteJobByIds(Long[] jobIds) {
        for (Long jobId : jobIds) {
            SysJob job = selectJobById(jobId);
            if (job != null) {
                deleteJob(job);
            }
        }
        return true;
    }

    @Override
    public boolean changeStatus(SysJob job) {
        int rows = sysJobMapper.updateById(job);
        if (rows > 0) {
            String status = job.getStatus();
            Long jobId = job.getJobId();
            String jobGroup = job.getJobGroup();
            try {
                if (SysJob.STATUS_NORMAL.equals(status)) {
                    scheduler.resumeJob(ScheduleUtils.getJobKey(jobId, jobGroup));
                } else if (SysJob.STATUS_PAUSE.equals(status)) {
                    scheduler.pauseJob(ScheduleUtils.getJobKey(jobId, jobGroup));
                }
            } catch (SchedulerException e) {
                log.error("修改定时任务状态失败", e);
            }
        }
        return rows > 0;
    }

    @Override
    public boolean run(SysJob job) {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        SysJob properties = selectJobById(jobId);
        if (properties == null) {
            return false;
        }

        try {
            JobDataMap dataMap = new JobDataMap();
            dataMap.put(ScheduleUtils.TASK_PROPERTIES, properties);
            scheduler.triggerJob(ScheduleUtils.getJobKey(jobId, jobGroup), dataMap);
        } catch (SchedulerException e) {
            log.error("立即执行定时任务失败", e);
            throw new RuntimeException("执行定时任务失败: " + e.getMessage());
        }
        return true;
    }

    @Override
    public boolean checkCronExpressionIsValid(SysJob job) {
        return CronUtils.isValid(job.getCronExpression());
    }

    /**
     * 更新调度任务
     */
    private void updateSchedulerJob(SysJob job, String oldJobGroup) throws SchedulerException {
        Long jobId = job.getJobId();
        // 删除旧任务
        scheduler.deleteJob(ScheduleUtils.getJobKey(jobId, oldJobGroup));
        // 创建新任务
        ScheduleUtils.createScheduleJob(scheduler, job);
    }
}

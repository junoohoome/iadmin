package me.fjq.quartz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import me.fjq.quartz.entity.SysJob;

import java.util.List;

/**
 * 定时任务调度 Service 接口
 *
 * @author fjq
 */
public interface SysJobService extends IService<SysJob> {

    /**
     * 获取所有待执行的任务
     *
     * @return 任务列表
     */
    List<SysJob> selectJobAll();

    /**
     * 通过任务ID查询任务
     *
     * @param jobId 任务ID
     * @return 任务
     */
    SysJob selectJobById(Long jobId);

    /**
     * 新增任务
     *
     * @param job 任务
     * @return 是否成功
     */
    boolean insertJob(SysJob job);

    /**
     * 更新任务
     *
     * @param job 任务
     * @return 是否成功
     */
    boolean updateJob(SysJob job);

    /**
     * 删除任务
     *
     * @param job 任务
     * @return 是否成功
     */
    boolean deleteJob(SysJob job);

    /**
     * 批量删除任务
     *
     * @param jobIds 任务ID列表
     * @return 是否成功
     */
    boolean deleteJobByIds(Long[] jobIds);

    /**
     * 任务状态修改
     *
     * @param job 任务
     * @return 是否成功
     */
    boolean changeStatus(SysJob job);

    /**
     * 立即运行任务
     *
     * @param job 任务
     * @return 是否成功
     */
    boolean run(SysJob job);

    /**
     * 验证 cron 表达式是否有效
     *
     * @param job 任务
     * @return 是否有效
     */
    boolean checkCronExpressionIsValid(SysJob job);
}

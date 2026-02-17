package me.fjq.quartz.service;

import com.baomidou.mybatisplus.extension.service.IService;
import me.fjq.quartz.entity.SysJobLog;

import java.util.List;

/**
 * 定时任务调度日志 Service 接口
 *
 * @author fjq
 */
public interface SysJobLogService extends IService<SysJobLog> {

    /**
     * 获取日志详情
     *
     * @param jobLogId 日志ID
     * @return 日志
     */
    SysJobLog selectJobLogById(Long jobLogId);

    /**
     * 批量删除日志
     *
     * @param jobLogIds 日志ID列表
     * @return 是否成功
     */
    boolean deleteJobLogByIds(Long[] jobLogIds);

    /**
     * 清空所有日志
     */
    void cleanJobLog();
}

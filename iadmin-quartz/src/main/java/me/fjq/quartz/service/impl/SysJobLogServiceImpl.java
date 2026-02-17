package me.fjq.quartz.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.quartz.entity.SysJobLog;
import me.fjq.quartz.mapper.SysJobLogMapper;
import me.fjq.quartz.service.SysJobLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 定时任务调度日志 Service 实现类
 *
 * @author fjq
 */
@Slf4j
@AllArgsConstructor
@Service("sysJobLogService")
@Transactional(rollbackFor = Exception.class)
public class SysJobLogServiceImpl extends ServiceImpl<SysJobLogMapper, SysJobLog> implements SysJobLogService {

    private final SysJobLogMapper sysJobLogMapper;

    @Override
    public SysJobLog selectJobLogById(Long jobLogId) {
        return sysJobLogMapper.selectById(jobLogId);
    }

    @Override
    public boolean deleteJobLogByIds(Long[] jobLogIds) {
        for (Long jobLogId : jobLogIds) {
            sysJobLogMapper.deleteById(jobLogId);
        }
        return true;
    }

    @Override
    public void cleanJobLog() {
        sysJobLogMapper.delete(new LambdaQueryWrapper<>());
    }
}

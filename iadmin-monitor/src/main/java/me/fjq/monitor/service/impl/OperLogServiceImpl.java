package me.fjq.monitor.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.fjq.monitor.mapper.OperLogMapper;
import me.fjq.monitor.entity.OperLog;
import me.fjq.monitor.service.OperLogService;
import org.springframework.stereotype.Service;

/**
 * 操作日志记录(OperLog)表服务实现类
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@Service("operLogService")
public class OperLogServiceImpl extends ServiceImpl<OperLogMapper, OperLog> implements OperLogService {

}

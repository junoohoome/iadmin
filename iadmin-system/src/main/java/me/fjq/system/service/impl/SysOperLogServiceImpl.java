package me.fjq.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.fjq.system.mapper.SysOperLogMapper;
import me.fjq.system.entity.SysOperLog;
import me.fjq.system.service.SysOperLogService;
import org.springframework.stereotype.Service;

/**
 * 操作日志记录(SysOperLog)表服务实现类
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
@Service("sysOperLogService")
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements SysOperLogService {

}
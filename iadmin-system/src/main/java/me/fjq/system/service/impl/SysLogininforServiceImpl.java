package me.fjq.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.fjq.system.mapper.SysLogininforMapper;
import me.fjq.system.entity.SysLogininfor;
import me.fjq.system.service.SysLogininforService;
import org.springframework.stereotype.Service;

/**
 * 系统访问记录(SysLogininfor)表服务实现类
 *
 * @author fjq
 * @since 2020-03-23 22:43:48
 */
@Service("sysLogininforService")
public class SysLogininforServiceImpl extends ServiceImpl<SysLogininforMapper, SysLogininfor> implements SysLogininforService {

}
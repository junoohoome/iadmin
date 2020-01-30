package me.fjq.system.service.impl;


import me.fjq.system.mapper.SysConfigMapper;
import me.fjq.system.service.ISysConfigService;
import org.springframework.stereotype.Service;

/**
 * 参数配置 服务层实现
 */
@Service
public class SysConfigServiceImpl implements ISysConfigService {

    private final SysConfigMapper configMapper;

    public SysConfigServiceImpl(SysConfigMapper configMapper) {
        this.configMapper = configMapper;
    }


}

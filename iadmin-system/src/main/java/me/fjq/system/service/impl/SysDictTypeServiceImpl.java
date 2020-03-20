package me.fjq.system.service.impl;


import me.fjq.system.mapper.SysDictDataMapper;
import me.fjq.system.mapper.SysDictTypeMapper;
import me.fjq.system.service.ISysDictTypeService;
import org.springframework.stereotype.Service;

/**
 * 字典 业务层处理
 */
@Service
public class SysDictTypeServiceImpl implements ISysDictTypeService {

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;

    public SysDictTypeServiceImpl(SysDictTypeMapper dictTypeMapper, SysDictDataMapper dictDataMapper) {
        this.dictTypeMapper = dictTypeMapper;
        this.dictDataMapper = dictDataMapper;
    }


}

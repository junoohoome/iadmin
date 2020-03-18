package me.fjq.system.service.impl;


import cn.hutool.core.util.ObjectUtil;
import me.fjq.constant.UserConstants;
import me.fjq.system.domain.SysDictType;
import me.fjq.system.mapper.SysDictDataMapper;
import me.fjq.system.mapper.SysDictTypeMapper;
import me.fjq.system.service.ISysDictTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

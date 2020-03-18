package me.fjq.system.service.impl;


import me.fjq.system.domain.SysDictData;
import me.fjq.system.mapper.SysDictDataMapper;
import me.fjq.system.service.ISysDictDataService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典 业务层处理
 */
@Service
public class SysDictDataServiceImpl implements ISysDictDataService {

    private final SysDictDataMapper dictDataMapper;

    public SysDictDataServiceImpl(SysDictDataMapper dictDataMapper) {
        this.dictDataMapper = dictDataMapper;
    }


}

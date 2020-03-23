package me.fjq.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.fjq.system.mapper.SysDictDataMapper;
import me.fjq.system.entity.SysDictData;
import me.fjq.system.service.SysDictDataService;
import org.springframework.stereotype.Service;

/**
 * 字典数据表(SysDictData)表服务实现类
 *
 * @author fjq
 * @since 2020-03-23 22:43:48
 */
@Service("sysDictDataService")
public class SysDictDataServiceImpl extends ServiceImpl<SysDictDataMapper, SysDictData> implements SysDictDataService {

}
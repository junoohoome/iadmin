package me.fjq.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import me.fjq.system.mapper.SysDictTypeMapper;
import me.fjq.system.entity.SysDictType;
import me.fjq.system.service.SysDictTypeService;
import org.springframework.stereotype.Service;

/**
 * 字典类型表(SysDictType)表服务实现类
 *
 * @author fjq
 * @since 2020-03-23 22:43:48
 */
@Service("sysDictTypeService")
public class SysDictTypeServiceImpl extends ServiceImpl<SysDictTypeMapper, SysDictType> implements SysDictTypeService {

}
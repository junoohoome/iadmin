package me.fjq.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import me.fjq.system.entity.SysUser;
import me.fjq.system.query.SysUserQuery;
import me.fjq.system.vo.system.SysUserVo;
import org.springframework.data.repository.query.Param;

/**
 * 用户信息表(SysUser)表服务接口
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
public interface SysUserService extends IService<SysUser> {

    Page<SysUserVo> selectPage(Page page, @Param("query") SysUserQuery query);

}
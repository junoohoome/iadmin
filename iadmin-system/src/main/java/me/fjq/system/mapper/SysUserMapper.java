package me.fjq.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.lettuce.core.dynamic.annotation.Param;
import me.fjq.annotation.DataScope;
import me.fjq.system.entity.SysUser;
import me.fjq.system.query.SysUserQuery;
import me.fjq.system.vo.system.SysUserVo;

import java.util.List;


/**
 * 用户信息表(SysUser)表数据库访问层
 *
 * @author fjq
 * @since 2020-03-23 22:43:49
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 分页查询用户列表（带数据权限过滤）
     *
     * @param page  分页对象
     * @param query 查询条件
     * @return 用户列表
     */
    @DataScope(deptAlias = "u", userAlias = "u")
    List<SysUserVo> selectPage(Page page, @Param("query") SysUserQuery query);

}
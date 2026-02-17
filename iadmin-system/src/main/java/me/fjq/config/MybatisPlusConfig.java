package me.fjq.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.List;

/**
 * MybatisPlus配置
 *
 * @author fjq
 */
@MapperScan({"me.fjq.system.mapper", "me.fjq.monitor.mapper", "me.fjq.quartz.mapper"})
@Configuration
public class MybatisPlusConfig implements SmartInitializingSingleton {

    @Lazy
    @Autowired(required = false)
    private List<SqlSessionFactory> sqlSessionFactoryList;

    /**
     * mybatis-plus 拦截器（包含分页插件）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 设置单页分页条数限制（可选）
        paginationInnerInterceptor.setMaxLimit(1000L);
        // 设置请求的页面大于最大页后操作，true调回到首页，false继续请求
        paginationInnerInterceptor.setOverflow(false);
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }

    /**
     * 注册数据权限拦截器到所有 SqlSessionFactory
     * 使用 SmartInitializingSingleton 回调，此时所有 bean 已初始化完成，避免循环依赖
     */
    @Override
    public void afterSingletonsInstantiated() {
        DataScopeInterceptor interceptor = new DataScopeInterceptor();
        if (sqlSessionFactoryList != null) {
            for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
                org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
                configuration.addInterceptor(interceptor);
            }
        }
    }

}

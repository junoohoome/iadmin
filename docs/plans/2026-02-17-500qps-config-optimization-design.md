# 方案 A：配置参数优化 - 达到 500 QPS

## 一、背景与目标

### 1.1 目标
- 支撑 **500 QPS**（每秒请求数）
- 读写均衡场景
- 单机部署环境

### 1.2 当前瓶颈分析

| 瓶颈层 | 当前配置 | 目标配置 | 风险等级 |
|--------|----------|----------|----------|
| Tomcat 线程池 | 默认 200 线程 | 500 线程 | 高 |
| 数据库连接池 | 50 连接 | 100 连接 | 中 |
| Redis 连接池 | 20 连接 | 50 连接 | 中 |
| 异步线程池 | 配置冲突 | 统一配置 | 需修复 |

### 1.3 预期效果
- 配置优化后预计达到 **200-300 QPS**
- 为后续缓存优化打下基础

---

## 二、优化方案详细设计

### 2.1 Tomcat 线程池配置

**文件**：`iadmin-system/src/main/resources/application.yml`

**配置内容**：
```yaml
server:
  port: 8090
  tomcat:
    threads:
      max: 500           # 最大工作线程数
      min-spare: 20      # 最小空闲线程数
    max-connections: 10000  # 最大TCP连接数
    accept-count: 200    # 等待队列长度
```

**参数说明**：
| 参数 | 默认值 | 优化值 | 说明 |
|------|--------|--------|------|
| threads.max | 200 | 500 | 同时处理请求的最大线程数 |
| threads.min-spare | 10 | 20 | 保持的最小空闲线程数 |
| max-connections | 8192 | 10000 | Tomcat能接受的最大TCP连接 |
| accept-count | 100 | 200 | 所有线程忙时的等待队列长度 |

**资源消耗**：每个线程约 1MB 栈空间，500 线程约需 500MB 额外内存

---

### 2.2 数据库连接池配置

**文件**：`iadmin-system/src/main/resources/application-dev.yml`

**配置内容**：
```yaml
spring:
  datasource:
    druid:
      initial-size: 10          # 初始连接数
      min-idle: 10              # 最小空闲连接
      max-active: 100           # 最大连接数
      max-wait: 10000           # 获取连接超时时间(ms)
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: select 1
      test-while-idle: true
      pool-prepared-statements: true
      max-open-prepared-statements: 50
      max-pool-prepared-statement-per-connection-size: 20
```

**变化对比**：
| 参数 | 原值 | 优化值 | 说明 |
|------|------|--------|------|
| initial-size | 5 | 10 | 启动时预热更多连接 |
| min-idle | 5 | 10 | 保持更多空闲连接 |
| max-active | 50 | 100 | 支持更高并发 |
| max-wait | 60000 | 10000 | 快速失败，便于排查 |

**MySQL 配置要求**：
```sql
-- 检查当前配置
SHOW VARIABLES LIKE 'max_connections';

-- 需要确保 MySQL max_connections >= 150
-- 在 my.cnf 中配置：
max_connections = 300
```

---

### 2.3 Redis 连接池配置

**文件**：`iadmin-system/src/main/resources/application-dev.yml`

**配置内容**：
```yaml
spring:
  data:
    redis:
      database: 8
      host: localhost
      port: 6379
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 50      # 最大连接数
          max-idle: 20        # 最大空闲连接
          min-idle: 10        # 最小空闲连接
          max-wait: 5000ms    # 获取连接最大等待时间
```

**变化对比**：
| 参数 | 原值 | 优化值 | 说明 |
|------|------|--------|------|
| max-active | 20 | 50 | 增加可用连接数 |
| max-idle | 10 | 20 | 增加空闲连接 |
| min-idle | 5 | 10 | 预热更多连接 |
| max-wait | 3000ms | 5000ms | 稍微延长等待时间 |

---

### 2.4 异步线程池配置修复

#### 2.4.1 删除重复配置

**删除文件**：`iadmin-system/src/main/java/me/fjq/config/AsyncConfig.java`

**原因**：与 `iadmin-common` 中的配置冲突，可能导致 Bean 覆盖

#### 2.4.2 优化统一配置

**文件**：`iadmin-common/src/main/java/me/fjq/config/AsyncConfig.java`

**配置内容**：
```java
@Configuration
@EnableAsync
public class AsyncConfig {

    public static final String TASK_EXECUTOR = "taskExecutor";

    @Bean(TASK_EXECUTOR)
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);                              // 核心线程数
        executor.setMaxPoolSize(50);                               // 最大线程数
        executor.setQueueCapacity(500);                            // 队列容量
        executor.setThreadNamePrefix("async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        return executor;
    }
}
```

**变化对比**：
| 参数 | 原值 | 优化值 | 说明 |
|------|------|--------|------|
| corePoolSize | 5 | 10 | 增加核心线程 |
| maxPoolSize | 20 | 50 | 增加最大线程 |
| queueCapacity | 100 | 500 | 增加队列容量 |

**拒绝策略**：
- `CallerRunsPolicy`：调用者线程执行任务，保证不丢失数据

---

## 三、实施步骤

### 步骤 1：配置 Tomcat 线程池
修改 `iadmin-system/src/main/resources/application.yml`

### 步骤 2：优化数据库连接池
修改 `iadmin-system/src/main/resources/application-dev.yml`

### 步骤 3：优化 Redis 连接池
修改 `iadmin-system/src/main/resources/application-dev.yml`

### 步骤 4：删除重复配置
删除 `iadmin-system/src/main/java/me/fjq/config/AsyncConfig.java`

### 步骤 5：优化异步线程池
修改 `iadmin-common/src/main/java/me/fjq/config/AsyncConfig.java`

### 步骤 6：验证测试
1. 启动应用，检查日志无报错
2. 使用 JMeter 进行压力测试
3. 监控各项指标

---

## 四、验证方法

### 4.1 配置验证
```bash
# 检查 Tomcat 线程配置（启动后查看日志或通过 actuator）
curl http://localhost:8090/actuator/metrics/tomcat.threads.config.max

# 检查数据库连接池
# 访问 Druid 监控页面
http://localhost:8090/druid/
```

### 4.2 压力测试
使用已有的 JMeter 测试计划：
```
stress-test/jmeter/iadmin-load-test.jmx
```

测试场景：
- 线程数：500
- Ramp-Up：60秒
- 持续时间：180秒

### 4.3 成功标准
- 无请求超时或错误
- 平均响应时间 < 500ms
- 错误率 < 1%

---

## 五、风险与回滚

### 5.1 潜在风险
| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 内存不足 | OOM | 监控 JVM 内存，必要时调整堆大小 |
| MySQL 连接耗尽 | 数据库报错 | 确保 MySQL max_connections >= 150 |
| 线程竞争 | 性能下降 | 通过压测验证，必要时降低配置 |

### 5.2 回滚方案
保留原配置文件备份，出问题时快速恢复：
```bash
cp application.yml application.yml.bak
cp application-dev.yml application-dev.yml.bak
```

---

## 六、后续优化方向

如果方案 A 达不到预期效果，可继续执行：
- **方案 B**：增加热点数据缓存
- **方案 C**：启用 Java 21 虚拟线程 + SQL 优化

---

*文档创建日期：2026-02-17*

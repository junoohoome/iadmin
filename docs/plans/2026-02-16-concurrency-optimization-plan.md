# iAdmin 单体部署并发能力优化实施计划

> 创建时间：2026-02-16
> 目标：提升单节点并发处理能力，QPS 从 ~400 提升至 ~800-1000

## 一、优化目标

| 指标 | 当前值 | 目标值 |
|------|--------|--------|
| 简单查询 QPS | ~400 | ~800-1000 |
| 复杂查询 QPS | ~100 | ~250-300 |
| DB 连接等待风险 | 高 | 低 |
| 线程数可控性 | 差 | 良好 |

## 二、实施步骤

### Step 1: 扩大数据库连接池

**修改文件**: `iadmin-system/src/main/resources/application-dev.yml`

```yaml
spring:
  datasource:
    druid:
      # 最大连接数: 20 → 50
      max-active: 50
      # 初始连接数: 1 → 5
      initial-size: 5
      # 最小连接池数量: 1 → 5
      min-idle: 5
```

**验证方法**:
```bash
# 启动后访问 Druid 监控
http://localhost:8090/druid/
# 用户名: admin, 密码: Druid@2025Admin!
# 查看 "数据源" 页面，确认 max-active 为 50
```

---

### Step 2: 添加 Redis 连接池

**修改文件**: `iadmin-system/src/main/resources/application-dev.yml`

```yaml
spring:
  data:
    redis:
      database: 8
      host: localhost
      port: 6379
      timeout: 5000ms
      # 新增连接池配置
      lettuce:
        pool:
          max-active: 20      # 最大连接数
          max-idle: 10        # 最大空闲连接
          min-idle: 5         # 最小空闲连接
          max-wait: 3000ms    # 获取连接最大等待时间
```

**依赖检查**: `spring-boot-starter-data-redis` 已包含 lettuce，无需额外依赖

**验证方法**:
```bash
# 启动后通过 Redis CLI 监控连接数
redis-cli -n 8 CLIENT LIST | wc -l
```

---

### Step 3: 配置异步任务线程池

**新增文件**: `iadmin-common/src/main/java/me/fjq/config/AsyncConfig.java`

```java
package me.fjq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步任务线程池配置
 *
 * @author fjq
 */
@EnableAsync
@Configuration
public class AsyncConfig {

    public static final String TASK_EXECUTOR = "taskExecutor";

    @Bean(TASK_EXECUTOR)
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数
        executor.setCorePoolSize(5);
        // 最大线程数
        executor.setMaxPoolSize(20);
        // 队列容量
        executor.setQueueCapacity(100);
        // 线程名前缀
        executor.setThreadNamePrefix("async-");
        // 拒绝策略：由调用线程处理
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        // 等待任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
```

**修改文件**: `iadmin-system/src/main/java/me/fjq/system/service/impl/SysLogininforServiceImpl.java`

```java
// 修改 @Async 注解，指定使用配置的线程池
@Async(AsyncConfig.TASK_EXECUTOR)
@Override
public void recordLoginLog(...) {
    // 方法体不变
}
```

**验证方法**:
```bash
# 启动后登录几次，查看日志或线程
jstack <pid> | grep "async-" | wc -l
```

---

### Step 4: LogAspect 线程池优化（可选）

**修改文件**: `iadmin-system/src/main/java/me/fjq/aspect/LogAspect.java`

将 `CompletableFuture.runAsync()` 改为使用配置的线程池：

```java
// 注入线程池
private final Executor taskExecutor;

// 构造函数
public LogAspect(SysOperLogService operLogService, @Qualifier("taskExecutor") Executor taskExecutor) {
    this.operLogService = operLogService;
    this.taskExecutor = taskExecutor;
}

// 修改异步调用方式
CompletableFuture.runAsync(() -> {
    // ...
}, taskExecutor);
```

---

## 三、实施顺序

```
Step 1 (DB连接池) ──→ Step 2 (Redis连接池) ──→ Step 3 (异步线程池) ──→ Step 4 (LogAspect)
     │                      │                       │                      │
     ↓                      ↓                       ↓                      ↓
   必须先做              必须先做                必须先做               可选优化
```

**建议**：按顺序依次实施，每完成一步进行验证，确保无问题再进行下一步。

---

## 四、验证清单

### 4.1 功能验证

- [ ] 登录功能正常
- [ ] 用户列表查询正常
- [ ] 操作日志正常记录
- [ ] 登录日志正常记录
- [ ] Druid 监控页面可访问

### 4.2 性能验证（可选）

使用 JMeter 或 ab 进行压测：

```bash
# 安装 ab (Apache Benchmark)
# Mac: brew install httpd

# 简单压测示例（需要先获取有效 token）
ab -n 1000 -c 50 -H "Authorization: Bearer <token>" http://localhost:8090/system/user/list
```

### 4.3 监控指标

| 指标 | 查看位置 | 关注点 |
|------|----------|--------|
| DB 连接池状态 | Druid 监控 | 活跃连接数、等待线程数 |
| Redis 连接数 | `redis-cli CLIENT LIST` | 连接数是否稳定 |
| JVM 线程数 | JConsole / VisualVM | async- 线程数量 |

---

## 五、回滚方案

每个 Step 都可独立回滚：

| Step | 回滚操作 |
|------|----------|
| Step 1 | 将 max-active 改回 20，min-idle 改回 1 |
| Step 2 | 删除 lettuce.pool 配置块 |
| Step 3 | 删除 AsyncConfig.java，@Async 注解去掉参数 |
| Step 4 | LogAspect 恢复使用 CompletableFuture.runAsync() |

---

## 六、风险评估

| 风险 | 等级 | 缓解措施 |
|------|------|----------|
| DB 连接数增加导致 MySQL 压力增大 | 中 | 监控 MySQL 连接数，逐步调大 |
| Redis 连接池配置错误导致启动失败 | 低 | 先在开发环境验证 |
| 异步线程池满导致任务拒绝 | 低 | 配置 CallerRunsPolicy 回退策略 |

---

## 七、后续优化方向（本次不做）

1. **缓存策略**：热点数据 Redis 缓存，减少 DB 查询
2. **读写分离**：如果读多写少，可配置主从分离
3. **JVM 调优**：根据实际内存使用调整堆大小
4. **虚拟线程**：Java 21 特性，可用于 I/O 密集型场景

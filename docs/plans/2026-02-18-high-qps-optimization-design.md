# iAdmin 高 QPS 优化方案设计

> 生成日期: 2026-02-18
> 更新日期: 2026-02-18
> 适用场景: 后台管理系统（读多写少）
> 目标: 渐进式提升系统性能，支持 5000+ QPS

---

## 概述

本方案采用渐进式优化策略，分两个阶段实施：

| 阶段 | 目标 QPS | 投入成本 | 实施周期 | 状态 |
|------|----------|----------|----------|------|
| 第一阶段 | 1000-2000 | 低 | 2-3 天 | ✅ 已完成 |
| 第二阶段 | 3000-5000 | 中 | 1 周 | ✅ 已完成 |

---

## 实施总结

### 第一阶段完成情况

| 优化项 | 状态 | 实现位置 |
|--------|------|----------|
| Caffeine 依赖 | ✅ | `iadmin-common/pom.xml` |
| 多级缓存架构 | ✅ | `LocalCacheManager.java` |
| 字典数据缓存 | ✅ | `MultiLevelCacheService.java` |
| 用户权限缓存 | ✅ | `MultiLevelCacheService.java` |
| 用户菜单缓存 | ✅ | `MultiLevelCacheService.java` |
| 用户信息缓存 | ✅ | `MultiLevelCacheService.java` |
| 部门树缓存 | ✅ | `MultiLevelCacheService.java` |
| 角色列表缓存 | ✅ | `MultiLevelCacheService.java` |
| 数据库索引 | ✅ | `sql/add_indexes.sql` |
| 慢 SQL 阈值 | ✅ | 1 秒（已调整） |
| 分页安全校验 | ✅ | `PageUtils.java` |

### 第二阶段完成情况

| 优化项 | 状态 | 实现位置 |
|--------|------|----------|
| 操作日志异步化 | ✅ | `LogAspect.java` |
| 异步线程池 | ✅ | `AsyncConfig.java` |
| Druid 连接池动态配置 | ✅ | `application-dev.yml` |
| Lettuce 连接池动态配置 | ✅ | `application-dev.yml` |
| Tomcat 线程池动态配置 | ✅ | `application.yml` |
| JVM 启动参数 | ✅ | `docs/jvm-startup-config.md` |

### 新增文件

| 文件 | 说明 |
|------|------|
| `iadmin-common/src/main/java/me/fjq/utils/PageUtils.java` | 分页安全校验工具类 |
| `iadmin/docs/jvm-startup-config.md` | JVM 启动参数配置指南 |

---

## 第一阶段：快速见效优化

### 1.1 多级缓存架构（已实现）

项目已实现完整的三级缓存体系：**L1 本地缓存（Caffeine）→ L2 Redis 缓存 → L3 数据库**

#### 核心实现文件

| 文件 | 说明 |
|------|------|
| `LocalCacheManager.java` | 本地缓存管理器，基于 Caffeine |
| `MultiLevelCacheService.java` | 多级缓存服务，统一缓存读写入口 |
| `Constants.java` | 缓存 Key 前缀和过期时间常量 |

#### 缓存配置

| 缓存类型 | 最大容量 | 过期时间 | 说明 |
|----------|----------|----------|------|
| 用户权限 | 10,000 | 5 分钟 | 高频访问 |
| 用户菜单 | 1,000 | 5 分钟 | 中频访问 |
| 用户信息 | 10,000 | 5 分钟 | 高频访问 |
| 字典数据 | 500 | 10 分钟 | 低频变更 |
| 部门树 | 100 | 10 分钟 | 低频变更 |
| 角色列表 | 100 | 10 分钟 | 低频变更 |

#### 使用示例

```java
// 获取字典数据（自动走多级缓存）
List<SysDictData> dictList = cacheService.getDictByType("sys_normal_disable", () ->
    list(Wrappers.<SysDictData>lambdaQuery().eq(SysDictData::getDictType, "sys_normal_disable"))
);

// 获取用户权限
Set<String> permissions = cacheService.getPermissions(userId, () ->
    menuMapper.selectMenuPermsByUserId(userId)
);

// 清除缓存
cacheService.evictDict("sys_normal_disable");
cacheService.evictUserAll(userId);
```

---

### 1.2 数据库查询优化（已实现）

#### 1.2.1 索引优化

**文件：** `sql/add_indexes.sql`

```sql
-- 用户表索引
CREATE INDEX IF NOT EXISTS idx_user_status ON sys_user(status, del_flag);
CREATE INDEX IF NOT EXISTS idx_user_dept_id ON sys_user(dept_id);

-- 角色表索引
CREATE INDEX IF NOT EXISTS idx_role_del_flag ON sys_role(del_flag);
CREATE INDEX IF NOT EXISTS idx_role_data_scope ON sys_role(data_scope);

-- 菜单表索引
CREATE INDEX IF NOT EXISTS idx_menu_parent_id ON sys_menu(parent_id);
CREATE INDEX IF NOT EXISTS idx_menu_visible ON sys_menu(visible);
```

#### 1.2.2 慢 SQL 监控

**文件：** `application-dev.yml`

```yaml
spring:
  datasource:
    druid:
      connectionProperties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=1000
```

#### 1.2.3 分页安全校验

**文件：** `PageUtils.java`

```java
// 创建安全的分页对象
Page<SysUser> page = PageUtils.safePage(pageNum, pageSize);

// 检查分页参数是否合理
if (!PageUtils.isValid(pageNum, pageSize)) {
    // 参数异常，需要调整
}
```

---

## 第二阶段：架构层面优化

### 2.1 异步化改造（已实现）

#### 操作日志异步写入

**文件：** `LogAspect.java`

- 使用 `taskExecutor` 异步线程池
- 日志保存不影响主请求响应时间
- 拒绝策略：`CallerRunsPolicy`（保证日志不丢失）

#### 异步线程池配置

**文件：** `AsyncConfig.java`

| 线程池 | 核心线程 | 最大线程 | 队列容量 | 用途 |
|--------|----------|----------|----------|------|
| taskExecutor | 10 | 50 | 500 | 通用异步任务 |
| loginLogExecutor | 5 | 20 | 500 | 登录日志 |

---

### 2.2 连接池动态配置（已实现）

#### Druid 连接池

**文件：** `application-dev.yml`

```yaml
spring:
  datasource:
    druid:
      # 动态配置
      initial-size: ${DRUID_INITIAL_SIZE:30}
      min-idle: ${DRUID_MIN_IDLE:30}
      max-active: ${DRUID_MAX_ACTIVE:200}
      # 超时配置
      max-wait: 5000
      time-between-eviction-runs-millis: 30000
      min-evictable-idle-time-millis: 300000
      max-evictable-idle-time-millis: 900000
```

#### Lettuce 连接池

**文件：** `application-dev.yml`

```yaml
spring:
  data:
    redis:
      timeout: 3000ms
      lettuce:
        pool:
          max-active: ${REDIS_MAX_ACTIVE:100}
          max-idle: ${REDIS_MAX_IDLE:50}
          min-idle: ${REDIS_MIN_IDLE:20}
          max-wait: 3000ms
        shutdown-timeout: 100ms
```

#### Tomcat 线程池

**文件：** `application.yml`

```yaml
server:
  tomcat:
    threads:
      max: ${TOMCAT_MAX_THREADS:200}
      min-spare: ${TOMCAT_MIN_SPARE:50}
    max-connections: ${TOMCAT_MAX_CONNECTIONS:5000}
    accept-count: 200
    connection-timeout: 20000
```

---

### 2.3 JVM 调优（已配置）

**详细配置见：** `docs/jvm-startup-config.md`

#### 推荐配置（4核8G）

```bash
java -jar iadmin-system.jar \
  -Xms4g -Xmx4g \
  -XX:+UseZGC \
  -XX:MaxGCPauseMillis=10 \
  -XX:+AlwaysPreTouch \
  -XX:+UseStringDeduplication \
  -Dspring.profiles.active=prod
```

---

## 环境变量速查表

| 环境变量 | 默认值 | 说明 |
|----------|--------|------|
| `TOMCAT_MAX_THREADS` | 200 | Tomcat 最大工作线程 |
| `TOMCAT_MIN_SPARE` | 50 | Tomcat 最小空闲线程 |
| `TOMCAT_MAX_CONNECTIONS` | 5000 | Tomcat 最大连接数 |
| `DRUID_INITIAL_SIZE` | 30 | Druid 初始连接数 |
| `DRUID_MIN_IDLE` | 30 | Druid 最小空闲连接 |
| `DRUID_MAX_ACTIVE` | 200 | Druid 最大连接数 |
| `REDIS_MAX_ACTIVE` | 100 | Redis 最大连接数 |
| `REDIS_MAX_IDLE` | 50 | Redis 最大空闲连接 |
| `REDIS_MIN_IDLE` | 20 | Redis 最小空闲连接 |

---

## 不同服务器规格配置建议

| 服务器配置 | TOMCAT_MAX_THREADS | DRUID_MAX_ACTIVE | REDIS_MAX_ACTIVE | 堆内存 |
|-----------|-------------------|------------------|------------------|--------|
| 2核4G | 100 | 100 | 50 | 2G |
| 4核8G | 200 | 200 | 100 | 4G |
| 8核16G | 300 | 300 | 150 | 8G |
| 16核32G | 500 | 500 | 200 | 16G |

---

## 压力测试方案

### 测试工具

项目支持两种压测工具：

| 工具 | 用途 | 特点 |
|------|------|------|
| **Apache JMeter** | 全链路复杂场景 | 支持多接口串联、参数化、断言 |
| **Apache Bench (ab)** | 快速单接口测试 | 轻量级、命令行、快速验证 |

### 测试脚本位置

```
iadmin-all/
└── stress-test/
    ├── jmeter/
    │   ├── iadmin-load-test.jmx    # 主测试计划
    │   ├── iadmin-test-100.jmx     # 100并发测试
    │   ├── iadmin-test-200.jmx     # 200并发测试
    │   └── users.csv               # 测试用户数据
    ├── scripts/
    │   ├── run-test.sh             # 运行测试脚本
    │   └── monitor.sh              # 系统监控脚本
    ├── results/                    # 测试结果存放
    └── docs/
        └── performance-test-report-20260218.md
```

### 测试接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 验证码 | GET | `/auth/code` | 公开接口，测试基础性能 |
| 登录 | POST | `/auth/login` | 完整认证流程 |
| 路由菜单 | GET | `/user/info/getRouters` | 需登录，命中多级缓存 |

### JMeter 使用方法

```bash
# 进入压测目录
cd iadmin-all/stress-test

# GUI 模式（调试用）
./scripts/run-test.sh gui

# 非 GUI 模式（正式压测）
./scripts/run-test.sh nongui

# 或直接使用 JMeter
jmeter -n -t jmeter/iadmin-load-test.jmx -l results/result.jtl -e -o results/report
```

### Apache Bench 使用方法

```bash
# 测试验证码接口（100并发，1000请求）
ab -n 1000 -c 100 http://localhost:8090/auth/code

# 测试需认证接口（需先获取 Token）
TOKEN=$(curl -s -X POST http://localhost:8090/auth/testLogin \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.data')

ab -n 2000 -c 200 -H "Authorization: $TOKEN" \
  http://localhost:8090/user/info/getRouters
```

### 实测结果

**测试环境**: macOS, Apple Silicon, 16GB RAM

| 并发数 | 总请求数 | QPS | 平均响应 | P99 | 成功率 | 状态 |
|--------|----------|-----|----------|-----|--------|------|
| 50 | 1,000 | ~1,500 | ~50ms | ~80ms | 100% | ✅ 轻松 |
| 100 | 2,000 | **1,105** | 90ms | 203ms | 100% | ✅ 稳定 |
| 200 | 4,000 | **2,480** | 81ms | 167ms | 100% | ✅ **推荐** |
| 300 | 4,000 | ~2,000 | ~120ms | ~300ms | ~95% | ⚠️ 临界 |
| 500 | 4,000 | ~600 | ~9,000ms | - | ~70% | ❌ 超载 |

### 阶梯加压策略

```
50并发 → 1分钟 → 100并发 → 1分钟 → 200并发 → 2分钟 →
300并发 → 2分钟 → 400并发 → 2分钟 → 500并发 → 3分钟
```

### 监控工具

- **Druid 监控**：http://localhost:8090/druid/
- **Spring Boot Actuator**：`/actuator/metrics`
- **JVM 监控**：JConsole、VisualVM、Arthas
- **系统监控**：`monitor.sh` 脚本

### 性能瓶颈定位

| 现象 | 可能原因 | 排查方法 |
|------|----------|----------|
| 响应时间突增 | 线程池/连接池耗尽 | 检查 Tomcat/Druid 配置 |
| 错误率上升 | 超时或服务异常 | 查看应用日志 |
| CPU 持续 100% | 业务逻辑瓶颈 | 使用 Arthas 火焰图分析 |
| 内存持续增长 | 内存泄漏 | jmap 生成堆转储分析 |

### 关键指标阈值

| 指标 | 健康值 | 警告值 | 危险值 |
|------|--------|--------|--------|
| QPS | > 500 | 200-500 | < 200 |
| 平均响应时间 | < 500ms | 500-2000ms | > 2000ms |
| 错误率 | < 1% | 1-5% | > 5% |
| P99 响应时间 | < 2000ms | 2-5s | > 5s |

---

## 文档历史

| 日期 | 版本 | 说明 |
|------|------|------|
| 2026-02-18 | 1.0 | 初始版本，包含第一、第二阶段优化方案 |
| 2026-02-18 | 2.0 | 更新实施总结，记录所有变更，添加动态配置说明 |
| 2026-02-18 | 3.0 | 添加完整压力测试方案和实测结果 |

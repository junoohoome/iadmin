# 登录性能优化设计文档

## 背景

500并发压测结果显示登录接口存在性能瓶颈：
- 登录接口平均响应时间：831ms
- 500并发下成功率仅54%（大量30秒超时）
- 认证后接口响应优秀（4-9ms）

## 优化目标

| 指标 | 当前值 | 目标值 |
|------|--------|--------|
| 登录接口响应时间 | 831ms | <200ms |
| 500并发成功率 | 54% | >95% |
| 高频重复登录 | 每次完整认证 | Token自动续期 |

## 优化方案

采用渐进式优化，分三个独立模块实现：

### 1. Token续期机制

**目标**：减少高频重复登录，提升用户体验

**实现思路**：
```
请求流程：
请求 → JwtAuthenticationTokenFilter
     → 检查Token剩余有效期
     → 若 < 1小时，自动刷新生成新Token
     → 通过响应头 X-New-Token 返回给前端
```

**关键设计**：

| 项目 | 设计 |
|------|------|
| 续期阈值 | Token剩余有效期 < 25%时触发（4小时Token即<1小时） |
| 续期方式 | 生成新Token，不修改原Token，避免并发问题 |
| 前端配合 | 响应拦截器检测X-New-Token头，自动更新localStorage |
| 黑名单处理 | 旧Token加入Redis黑名单（TTL=剩余有效期），防重放 |

**新增文件**：
```
iadmin-system/src/main/java/me/fjq/security/
└── TokenRefreshService.java
```

**核心代码结构**：
```java
@Service
public class TokenRefreshService {

    private static final long REFRESH_THRESHOLD = 60 * 60 * 1000; // 1小时

    /**
     * 检查并刷新Token
     * @return 新Token，如果不需要刷新返回null
     */
    public String refreshTokenIfNeeded(String token) {
        // 1. 解析Token获取过期时间
        // 2. 计算剩余有效期
        // 3. 若 < 阈值，生成新Token
        // 4. 旧Token加入黑名单
        // 5. 返回新Token
    }
}
```

**前端改造**（iadmin-web）：
```typescript
// src/utils/request.ts 响应拦截器
response => {
    const newToken = response.headers['x-new-token']
    if (newToken) {
        localStorage.setItem('token', newToken)
    }
    return response.data
}
```

---

### 2. 用户信息缓存

**目标**：减少数据库访问，加速登录认证

**实现思路**：
```
原流程：login() → DB查用户 → DB查角色 → DB查权限 → 生成Token
新流程：login() → Redis查缓存 → (命中则直接用) → 生成Token
                   ↓ (未命中)
              DB查询 → 写入缓存 → 生成Token
```

**缓存结构**：

| Redis Key | 内容 | TTL |
|-----------|------|-----|
| `user:cache:{userId}` | 用户基本信息JSON | 30分钟 |
| `user:roles:{userId}` | 角色编码列表 | 30分钟 |
| `user:permissions:{userId}` | 权限标识列表 | 30分钟 |

**缓存失效策略**：

| 触发条件 | 处理方式 |
|---------|---------|
| 用户信息修改(SysUserService.updateUser) | 删除该用户所有缓存 |
| 角色权限变更(SysRoleService.updateRole) | 删除该角色关联的所有用户缓存 |
| Token续期成功 | 延长缓存TTL至30分钟 |
| 用户登出 | 保留缓存（其他设备可能在线） |

**新增文件**：
```
iadmin-system/src/main/java/me/fjq/security/
└── UserCacheService.java
```

**修改文件**：
```
iadmin-system/src/main/java/me/fjq/security/
└── UserDetailsServiceImpl.java  // 优先从缓存获取
```

**核心代码结构**：
```java
@Service
public class UserCacheService {

    private static final String USER_CACHE_PREFIX = "user:cache:";
    private static final long CACHE_TTL = 30 * 60; // 30分钟

    /**
     * 获取缓存的用户信息
     */
    public SysUser getCachedUser(Long userId) {
        String key = USER_CACHE_PREFIX + userId;
        return redisUtils.get(key);
    }

    /**
     * 缓存用户信息
     */
    public void cacheUser(SysUser user) {
        String key = USER_CACHE_PREFIX + user.getUserId();
        redisUtils.set(key, user, CACHE_TTL, TimeUnit.SECONDS);
    }

    /**
     * 清除用户缓存
     */
    public void evictUserCache(Long userId) {
        redisUtils.del(USER_CACHE_PREFIX + userId);
        redisUtils.del("user:roles:" + userId);
        redisUtils.del("user:permissions:" + userId);
    }

    /**
     * 续期缓存
     */
    public void renewCache(Long userId) {
        redisUtils.expire(USER_CACHE_PREFIX + userId, CACHE_TTL, TimeUnit.SECONDS);
        redisUtils.expire("user:roles:" + userId, CACHE_TTL, TimeUnit.SECONDS);
        redisUtils.expire("user:permissions:" + userId, CACHE_TTL, TimeUnit.SECONDS);
    }
}
```

---

### 3. 异步登录日志

**目标**：日志写入不阻塞登录主流程

**实现思路**：
```
原流程：登录验证 → 记录日志(同步DB) → 返回Token
新流程：登录验证 → 发布日志事件 → 返回Token
                      ↓
                 异步线程池 → 写入DB
```

**技术方案**：Spring Event + @Async

**线程池配置**：

| 参数 | 值 | 说明 |
|------|-----|------|
| 核心线程数 | 2 | 日志写入IO密集型 |
| 最大线程数 | 5 | 高峰期扩展 |
| 队列容量 | 1000 | 缓冲队列 |
| 拒绝策略 | DiscardOldest | 满时丢弃最旧 |
| 线程名前缀 | login-log- | 便于排查 |

**新增文件**：
```
iadmin-system/src/main/java/me/fjq/config/
└── AsyncConfig.java           // 异步线程池配置

iadmin-system/src/main/java/me/fjq/event/
├── LoginRecordEvent.java      // 登录日志事件
└── LoginRecordListener.java   // 异步监听器
```

**修改文件**：
```
iadmin-system/src/main/java/me/fjq/system/controller/
└── LoginController.java       // 改用事件发布
```

**核心代码结构**：

```java
// LoginRecordEvent.java
@Data
@AllArgsConstructor
public class LoginRecordEvent {
    private String username;
    private String ipaddr;
    private String browser;
    private String os;
    private Integer status;      // 0=成功, 1=失败
    private String message;
}

// LoginRecordListener.java
@Component
@Slf4j
public class LoginRecordListener {

    private final LogininforService logininforService;

    @Async("loginLogExecutor")
    @EventListener
    public void handleLoginEvent(LoginRecordEvent event) {
        try {
            if (event.getStatus() == 0) {
                logininforService.recordLoginSuccess(
                    event.getUsername(),
                    event.getIpaddr(),
                    event.getBrowser(),
                    event.getOs()
                );
            } else {
                logininforService.recordLoginFail(
                    event.getUsername(),
                    event.getIpaddr(),
                    event.getBrowser(),
                    event.getOs(),
                    event.getMessage()
                );
            }
        } catch (Exception e) {
            log.error("异步记录登录日志失败: {}", e.getMessage());
        }
    }
}

// AsyncConfig.java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("loginLogExecutor")
    public Executor loginLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("login-log-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        executor.initialize();
        return executor;
    }
}
```

---

## 文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 新增 | security/TokenRefreshService.java | Token续期服务 |
| 新增 | security/UserCacheService.java | 用户缓存服务 |
| 新增 | config/AsyncConfig.java | 异步线程池配置 |
| 新增 | event/LoginRecordEvent.java | 登录日志事件 |
| 新增 | event/LoginRecordListener.java | 异步监听器 |
| 修改 | security/JwtAuthenticationTokenFilter.java | 增加续期检查 |
| 修改 | security/UserDetailsServiceImpl.java | 优先从缓存获取 |
| 修改 | controller/LoginController.java | 改用事件发布日志 |
| 修改 | service/SysUserService.java | 用户修改时清除缓存 |
| 修改 | service/SysRoleService.java | 角色变更时清除缓存 |

**前端变更**（iadmin-web）：
| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | utils/request.ts | 响应拦截器处理X-New-Token |

---

## 实施计划

| 阶段 | 内容 | 预估工作量 | 风险 |
|------|------|-----------|------|
| 阶段1 | Token续期机制 | 中 | 低（新功能，不影响现有流程） |
| 阶段2 | 用户信息缓存 | 高 | 中（需处理缓存一致性） |
| 阶段3 | 异步登录日志 | 低 | 低（仅改变写入时机） |

建议按顺序实施，每阶段完成后进行压测验证。

---

## 测试验证

每个阶段完成后执行以下验证：

1. **功能测试**
   - 正常登录/登出流程
   - Token续期触发和前端更新
   - 用户信息修改后缓存失效

2. **压测验证**
   - 500并发登录测试，成功率目标>95%
   - 登录接口响应时间目标<200ms

3. **监控指标**
   - Redis缓存命中率
   - 登录日志异步队列堆积情况
   - Token续期触发频率

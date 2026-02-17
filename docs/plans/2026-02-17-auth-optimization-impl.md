# 登录性能优化实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 优化登录接口性能，使500并发成功率从54%提升到>95%，响应时间从831ms降低到<200ms

**Architecture:** 渐进式优化 - Token续期机制 + 用户信息缓存 + 异步登录日志

**Tech Stack:** Spring Boot 3.2.5, Java 21, Redis, Spring Event, @Async

---

## 阶段1: 异步登录日志（风险最低，先实施）

### Task 1.1: 创建异步线程池配置

**Files:**
- Create: `iadmin-system/src/main/java/me/fjq/config/AsyncConfig.java`

**Step 1: 创建AsyncConfig.java**

```java
package me.fjq.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 登录日志异步执行器
     */
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

**Step 2: 编译验证**

Run: `cd iadmin-system && mvn compile -DskipTests -q`
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add iadmin-system/src/main/java/me/fjq/config/AsyncConfig.java
git commit -m "feat: 添加异步线程池配置用于登录日志"
```

---

### Task 1.2: 创建登录日志事件类

**Files:**
- Create: `iadmin-system/src/main/java/me/fjq/event/LoginRecordEvent.java`

**Step 1: 创建LoginRecordEvent.java**

```java
package me.fjq.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录记录事件
 */
@Getter
@AllArgsConstructor
public class LoginRecordEvent {

    /** 用户名 */
    private final String username;

    /** IP地址 */
    private final String ipaddr;

    /** 浏览器 */
    private final String browser;

    /** 操作系统 */
    private final String os;

    /** 状态: 0=成功, 1=失败 */
    private final Integer status;

    /** 消息 */
    private final String message;

    /**
     * 创建成功事件
     */
    public static LoginRecordEvent success(String username, String ipaddr, String browser, String os) {
        return new LoginRecordEvent(username, ipaddr, browser, os, 0, "登录成功");
    }

    /**
     * 创建失败事件
     */
    public static LoginRecordEvent fail(String username, String ipaddr, String browser, String os, String message) {
        return new LoginRecordEvent(username, ipaddr, browser, os, 1, message);
    }
}
```

**Step 2: 编译验证**

Run: `cd iadmin-system && mvn compile -DskipTests -q`
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add iadmin-system/src/main/java/me/fjq/event/LoginRecordEvent.java
git commit -m "feat: 添加登录记录事件类"
```

---

### Task 1.3: 创建异步事件监听器

**Files:**
- Create: `iadmin-system/src/main/java/me/fjq/event/LoginRecordListener.java`

**Step 1: 创建LoginRecordListener.java**

```java
package me.fjq.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.monitor.service.LogininforService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 登录记录事件监听器 - 异步处理登录日志
 */
@Slf4j
@Component
@RequiredArgsConstructor
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
            log.error("异步记录登录日志失败: username={}, error={}",
                    event.getUsername(), e.getMessage());
        }
    }
}
```

**Step 2: 编译验证**

Run: `cd iadmin-system && mvn compile -DskipTests -q`
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add iadmin-system/src/main/java/me/fjq/event/LoginRecordListener.java
git commit -m "feat: 添加登录记录异步监听器"
```

---

### Task 1.4: 修改LoginController使用事件发布

**Files:**
- Modify: `iadmin-system/src/main/java/me/fjq/system/controller/LoginController.java`

**Step 1: 添加ApplicationEventPublisher依赖**

在LoginController中添加:
```java
import org.springframework.context.ApplicationEventPublisher;

// 在构造函数参数中添加:
private final ApplicationEventPublisher eventPublisher;

// 修改构造函数:
public LoginController(SecurityProperties properties, RedisUtils redisUtils,
                       JwtTokenService jwtTokenService, AuthenticationManagerBuilder authenticationManagerBuilder,
                       OnlineService onlineService,
                       LogininforService logininforService,
                       ApplicationEventPublisher eventPublisher,
                       @Value("${spring.profiles.active:dev}") String activeProfile) {
    this.properties = properties;
    this.redisUtils = redisUtils;
    this.jwtTokenService = jwtTokenService;
    this.authenticationManagerBuilder = authenticationManagerBuilder;
    this.onlineService = onlineService;
    this.logininforService = logininforService;
    this.eventPublisher = eventPublisher;
    this.activeProfile = activeProfile;
}
```

**Step 2: 替换login方法中的日志调用**

将:
```java
logininforService.recordLoginSuccess(username, ipaddr, browser, os);
```
替换为:
```java
eventPublisher.publishEvent(LoginRecordEvent.success(username, ipaddr, browser, os));
```

将:
```java
logininforService.recordLoginFail(username, ipaddr, browser, os, "验证码不存在");
```
替换为:
```java
eventPublisher.publishEvent(LoginRecordEvent.fail(username, ipaddr, browser, os, "验证码不存在"));
```

**Step 3: 替换testLogin方法中的日志调用**

同样将logininforService调用替换为eventPublisher.publishEvent

**Step 4: 移除未使用的logininforService字段**

由于不再直接使用logininforService，可以从字段和构造函数中移除（如果其他地方也不使用）

**Step 5: 编译验证**

Run: `cd iadmin-system && mvn compile -DskipTests -q`
Expected: BUILD SUCCESS

**Step 6: Commit**

```bash
git add iadmin-system/src/main/java/me/fjq/system/controller/LoginController.java
git commit -m "refactor: 登录日志改为异步事件发布"
```

---

## 阶段2: 用户信息缓存

### Task 2.1: 创建用户缓存服务

**Files:**
- Create: `iadmin-system/src/main/java/me/fjq/security/UserCacheService.java`

**Step 1: 创建UserCacheService.java**

```java
package me.fjq.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.system.entity.SysUser;
import me.fjq.utils.RedisUtils;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 用户信息缓存服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheService {

    private static final String USER_CACHE_PREFIX = "user:cache:";
    private static final String USER_ROLES_PREFIX = "user:roles:";
    private static final String USER_PERMISSIONS_PREFIX = "user:permissions:";
    private static final long CACHE_TTL = 30 * 60; // 30分钟

    private final RedisUtils redisUtils;

    /**
     * 获取缓存的用户信息
     */
    public SysUser getCachedUser(Long userId) {
        try {
            String key = USER_CACHE_PREFIX + userId;
            return (SysUser) redisUtils.get(key);
        } catch (Exception e) {
            log.warn("获取用户缓存失败: userId={}, error={}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 缓存用户信息
     */
    public void cacheUser(SysUser user) {
        try {
            String key = USER_CACHE_PREFIX + user.getUserId();
            redisUtils.set(key, user, CACHE_TTL, TimeUnit.SECONDS);
            log.debug("用户信息已缓存: userId={}", user.getUserId());
        } catch (Exception e) {
            log.warn("缓存用户信息失败: userId={}, error={}", user.getUserId(), e.getMessage());
        }
    }

    /**
     * 获取缓存的权限列表
     */
    @SuppressWarnings("unchecked")
    public Set<String> getCachedPermissions(Long userId) {
        try {
            String key = USER_PERMISSIONS_PREFIX + userId;
            return (Set<String>) redisUtils.get(key);
        } catch (Exception e) {
            log.warn("获取权限缓存失败: userId={}, error={}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * 缓存权限列表
     */
    public void cachePermissions(Long userId, Set<String> permissions) {
        try {
            String key = USER_PERMISSIONS_PREFIX + userId;
            redisUtils.set(key, permissions, CACHE_TTL, TimeUnit.SECONDS);
            log.debug("权限信息已缓存: userId={}, count={}", userId, permissions.size());
        } catch (Exception e) {
            log.warn("缓存权限信息失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 清除用户所有缓存
     */
    public void evictUserCache(Long userId) {
        try {
            redisUtils.del(USER_CACHE_PREFIX + userId);
            redisUtils.del(USER_ROLES_PREFIX + userId);
            redisUtils.del(USER_PERMISSIONS_PREFIX + userId);
            log.debug("用户缓存已清除: userId={}", userId);
        } catch (Exception e) {
            log.warn("清除用户缓存失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    /**
     * 续期缓存（延长TTL）
     */
    public void renewCache(Long userId) {
        try {
            redisUtils.expire(USER_CACHE_PREFIX + userId, CACHE_TTL);
            redisUtils.expire(USER_ROLES_PREFIX + userId, CACHE_TTL);
            redisUtils.expire(USER_PERMISSIONS_PREFIX + userId, CACHE_TTL);
            log.debug("用户缓存已续期: userId={}", userId);
        } catch (Exception e) {
            log.warn("续期用户缓存失败: userId={}, error={}", userId, e.getMessage());
        }
    }
}
```

**Step 2: 编译验证**

Run: `cd iadmin-system && mvn compile -DskipTests -q`
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add iadmin-system/src/main/java/me/fjq/security/UserCacheService.java
git commit -m "feat: 添加用户信息缓存服务"
```

---

### Task 2.2: 修改UserDetailsServiceImpl使用缓存

**Files:**
- Modify: `iadmin-system/src/main/java/me/fjq/security/UserDetailsServiceImpl.java`

**Step 1: 添加UserCacheService依赖**

```java
// 添加import
import me.fjq.security.UserCacheService;

// 添加字段
private final UserCacheService userCacheService;

// 修改构造函数
```

**Step 2: 修改loadUserByUsername方法**

```java
@Override
public UserDetails loadUserByUsername(String username) {
    // 1. 查询用户（仍需从数据库获取，因为需要验证密码）
    SysUser user = userService.getOne(new QueryWrapper<SysUser>().lambda().eq(SysUser::getUserName, username));
    if (user == null) {
        throw new BadRequestException("账号不存在");
    }
    if (user.getStatus().equals(UserStatus.DISABLE.getCode())) {
        throw new BadRequestException("账号未激活");
    }

    // 2. 尝试从缓存获取权限
    Set<String> permissions = userCacheService.getCachedPermissions(user.getUserId());

    if (permissions == null) {
        // 缓存未命中，从数据库查询
        permissions = menuService.selectMenuPermsByUserId(user.getUserId());
        // 设置管理员权限
        if (SecurityUtils.isAdmin(user.getUserId())) {
            permissions.add(Constants.SYS_ADMIN_PERMISSION);
        }
        // 写入缓存
        userCacheService.cachePermissions(user.getUserId(), permissions);
        log.debug("权限信息从数据库加载并缓存: userId={}", user.getUserId());
    } else {
        log.debug("权限信息从缓存加载: userId={}", user.getUserId());
    }

    List<GrantedAuthority> authorities = permissions.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

    return JwtUserDetails.builder()
            .id(user.getUserId())
            .username(user.getUserName())
            .nickName(user.getNickName())
            .sex(user.getSex())
            .password(user.getPassword())
            .avatar(user.getAvatar())
            .email(user.getEmail())
            .mobile(user.getMobile())
            .authorities(authorities)
            .status(user.getStatus())
            .createTime(user.getCreateTime())
            .deptId(user.getDeptId())
            .ancestors(user.getAncestors())
            .roles(null)
            .build();
}
```

**Step 3: 编译验证**

Run: `cd iadmin-system && mvn compile -DskipTests -q`
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add iadmin-system/src/main/java/me/fjq/security/UserDetailsServiceImpl.java
git commit -m "feat: 用户权限信息优先从缓存获取"
```

---

### Task 2.3: 在用户修改时清除缓存

**Files:**
- Modify: `iadmin-system/src/main/java/me/fjq/system/service/impl/SysUserServiceImpl.java`

**Step 1: 添加UserCacheService依赖**

在SysUserServiceImpl中注入UserCacheService

**Step 2: 在updateUser方法中添加缓存清除**

```java
// 在更新用户信息后添加:
userCacheService.evictUserCache(user.getUserId());
```

**Step 3: 编译验证**

Run: `cd iadmin-system && mvn compile -DskipTests -q`
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add iadmin-system/src/main/java/me/fjq/system/service/impl/SysUserServiceImpl.java
git commit -m "feat: 用户信息修改时清除缓存"
```

---

## 阶段3: Token续期机制

### Task 3.1: 创建Token刷新服务

**Files:**
- Create: `iadmin-system/src/main/java/me/fjq/security/TokenRefreshService.java`

**Step 1: 创建TokenRefreshService.java**

```java
package me.fjq.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.fjq.monitor.service.OnlineService;
import me.fjq.properties.SecurityProperties;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Token刷新服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRefreshService {

    /** 续期阈值：剩余有效期小于1小时时触发 */
    private static final long REFRESH_THRESHOLD_MS = 60 * 60 * 1000;

    private final JwtTokenService jwtTokenService;
    private final OnlineService onlineService;
    private final UserCacheService userCacheService;
    private final SecurityProperties properties;

    /**
     * 检查并刷新Token
     *
     * @param token 当前Token（不含Bearer前缀）
     * @return 新Token（如果需要刷新），否则返回null
     */
    public String refreshTokenIfNeeded(String token) {
        try {
            // 1. 获取Token过期时间
            Claims claims = jwtTokenService.getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            long now = System.currentTimeMillis();
            long remainingTime = expiration.getTime() - now;

            // 2. 检查是否需要续期
            if (remainingTime > REFRESH_THRESHOLD_MS) {
                return null; // 不需要续期
            }

            // 3. 生成新Token
            String newToken = jwtTokenService.refreshToken(token);

            // 4. 旧Token加入黑名单（TTL为剩余有效期）
            long ttlSeconds = remainingTime / 1000;
            if (ttlSeconds > 0) {
                onlineService.addToBlacklist(token, ttlSeconds);
            }

            // 5. 续期用户缓存
            String username = claims.getSubject();
            // 从新Token获取用户ID需要重新解析，这里简化处理
            log.info("Token续期成功: username={}, remainingTime={}ms", username, remainingTime);

            return newToken;

        } catch (Exception e) {
            log.warn("Token续期失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断是否需要刷新Token
     */
    public boolean needsRefresh(String token) {
        try {
            Claims claims = jwtTokenService.getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            long remainingTime = expiration.getTime() - System.currentTimeMillis();
            return remainingTime > 0 && remainingTime <= REFRESH_THRESHOLD_MS;
        } catch (Exception e) {
            return false;
        }
    }
}
```

**Step 2: 在JwtTokenService中添加公开的getClaimsFromToken方法**

将JwtTokenService中的private方法getClaimsFromToken改为public：

```java
// 将
private Claims getClaimsFromToken(String token)
// 改为
public Claims getClaimsFromToken(String token)
```

**Step 3: 编译验证**

Run: `cd iadmin-system && mvn compile -DskipTests -q`
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add iadmin-system/src/main/java/me/fjq/security/TokenRefreshService.java
git add iadmin-system/src/main/java/me/fjq/security/JwtTokenService.java
git commit -m "feat: 添加Token刷新服务"
```

---

### Task 3.2: 修改过滤器添加续期逻辑

**Files:**
- Modify: `iadmin-system/src/main/java/me/fjq/security/JwtAuthenticationTokenFilter.java`

**Step 1: 添加TokenRefreshService依赖和HttpServletResponse**

```java
package me.fjq.security;

import lombok.extern.slf4j.Slf4j;
import me.fjq.properties.SecurityProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends GenericFilterBean {

    private final JwtTokenService jwtTokenService;
    private final TokenRefreshService tokenRefreshService;
    private final SecurityProperties properties;

    public JwtAuthenticationTokenFilter(JwtTokenService jwtTokenService,
                                         TokenRefreshService tokenRefreshService,
                                         SecurityProperties properties) {
        this.jwtTokenService = jwtTokenService;
        this.tokenRefreshService = tokenRefreshService;
        this.properties = properties;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 获取token, 并检查登录状态
        jwtTokenService.checkAuthentication(request);

        // 检查是否需要Token续期
        String token = jwtTokenService.getToken(request);
        if (token != null && tokenRefreshService.needsRefresh(token)) {
            String newToken = tokenRefreshService.refreshTokenIfNeeded(token);
            if (newToken != null) {
                // 设置响应头返回新Token
                response.setHeader("X-New-Token", properties.getTokenStartWith() + newToken);
                log.debug("Token已续期，新Token通过响应头返回");
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
```

**Step 2: 编译验证**

Run: `cd iadmin-system && mvn compile -DskipTests -q`
Expected: BUILD SUCCESS

**Step 3: Commit**

```bash
git add iadmin-system/src/main/java/me/fjq/security/JwtAuthenticationTokenFilter.java
git commit -m "feat: 过滤器增加Token自动续期逻辑"
```

---

### Task 3.3: 修改OnlineService支持带TTL的黑名单

**Files:**
- Modify: `iadmin-monitor/src/main/java/me/fjq/monitor/service/OnlineService.java`
- Modify: `iadmin-monitor/src/main/java/me/fjq/monitor/service/impl/OnlineServiceImpl.java`

**Step 1: 在OnlineService接口添加方法**

```java
/**
 * 将Token加入黑名单（带过期时间）
 *
 * @param token Token
 * @param ttlSeconds 过期时间（秒）
 */
void addToBlacklist(String token, long ttlSeconds);
```

**Step 2: 在OnlineServiceImpl实现方法**

```java
@Override
public void addToBlacklist(String token, long ttlSeconds) {
    String key = BLACKLIST_KEY + token;
    redisUtils.set(key, "1", ttlSeconds);
}
```

**Step 3: 编译验证**

Run: `mvn compile -DskipTests -q`
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add iadmin-monitor/src/main/java/me/fjq/monitor/service/OnlineService.java
git add iadmin-monitor/src/main/java/me/fjq/monitor/service/impl/OnlineServiceImpl.java
git commit -m "feat: 黑名单支持带TTL的Token"
```

---

## 阶段4: 前端Token更新支持

### Task 4.1: 修改前端响应拦截器

**Files:**
- Modify: `iadmin-web/src/utils/request.ts`

**Step 1: 在响应拦截器中添加Token更新逻辑**

```typescript
// 在response拦截器中添加:
service.interceptors.response.use(
  (response: AxiosResponse) => {
    // 检查是否有新Token返回
    const newToken = response.headers['x-new-token']
    if (newToken) {
      localStorage.setItem('token', newToken)
      console.log('Token已自动续期')
    }

    // 原有逻辑...
    return response.data
  },
  // ...
)
```

**Step 2: 测试验证**

启动前端和后端，登录后等待一段时间，检查是否自动续期

**Step 3: Commit**

```bash
git add src/utils/request.ts
git commit -m "feat: 前端支持Token自动续期"
```

---

## 验证测试

### 功能测试清单

- [ ] 正常登录流程
- [ ] 正常登出流程
- [ ] Token续期触发（等待1小时后请求）
- [ ] 前端X-New-Token响应头处理
- [ ] 用户信息修改后缓存失效
- [ ] 登录日志异步写入

### 压测验证

```bash
# 运行JMeter 500并发测试
cd stress-test
./scripts/run-test.sh nongui

# 验证指标:
# - 成功率 > 95%
# - 登录接口响应时间 < 200ms
```

---

## 最终提交

```bash
# 确保所有修改已提交
git status

# 合并到主分支
git checkout master
git merge feature/auth-optimization

# 推送
git push origin master
```

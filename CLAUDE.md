# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此代码库中工作时提供指导。

## 项目概述

iAdmin 是一个多模块的 Spring Boot 3.2 + Java 21 后台管理系统。这是一个 V2.0 升级项目，从 Java 8 / Spring Boot 2.2 迁移到现代 Jakarta EE 标准。

**技术栈：**
- Java 21（使用现代特性：模式匹配、switch 表达式、记录类）
- Spring Boot 3.2.5
- Spring Security 6.x + JWT 无状态认证
- MyBatis-Plus 3.5.7
- MySQL + Druid 连接池
- Redis 缓存和会话管理
- SpringDoc OpenAPI 3.0 (Swagger)

## 构建命令

```bash
# 构建整个项目
mvn clean install

# 跳过测试构建（pom.xml 中默认跳过测试）
mvn clean install -DskipTests

# 运行所有测试（测试默认禁用，需要显式启用）
mvn test -DskipTests=false

# 运行单个测试类
mvn test -Dtest=SystemApplicationTests

# 运行单个测试方法
mvn test -Dtest=SystemApplicationTests#testSpecificMethod

# 打包部署
cd iadmin-system && mvn clean package

# 运行应用
java -jar iadmin-system/target/iadmin-system-0.0.1-SNAPSHOT.jar
```

**注意：** Maven Surefire 插件默认配置 `<skip>true</skip>`。运行测试时必须使用 `-DskipTests=false`。

## 模块结构

```
iadmin (根目录)
├── iadmin-common      - 公共工具、注解、异常、基础类
├── iadmin-system      - 主应用（端口 8090），核心业务逻辑
├── iadmin-monitor     - 系统监控（使用 OSHI 库）
├── iadmin-quartz      - 定时任务（Quartz）
└── iadmin-tools       - 代码生成器和工具
```

**依赖关系：** `iadmin-common` 是基础模块，其他所有模块都依赖它。

## 架构设计

### 认证与安全

基于 JWT 的无状态认证流程：

```
请求 → JwtAuthenticationTokenFilter → JWT 验证 → SecurityContextHolder
                                                              ↓
                                                    Spring Security 授权
                                                              ↓
                                                    Controller → @PreAuthorize 检查
```

**核心组件：**
- `JwtAuthenticationTokenFilter` - 拦截所有请求，验证 JWT
- `JwtTokenService` - Token 生成、验证、刷新（4小时过期）
- `UserDetailsServiceImpl` - 从数据库加载用户及权限
- `WebSecurityConfig` - 安全配置（Spring Security 6 风格，使用 SecurityFilterChain）
- Redis 中的 Token 黑名单用于强制登出

**匿名访问：** `/auth/code`、`/auth/login`、Swagger UI（`/swagger-ui.html`）、静态资源

### 数据权限（DataScope）

AOP + MyBatis 拦截器模式实现行级安全：

1. Mapper 方法上的 `@DataScope` 注解（可配置表别名）
2. `DataScopeAspect` - 根据用户角色构造 SQL，存储在 ThreadLocal
3. `DataScopeInterceptor` - MyBatis 拦截器，将 SQL 注入查询

**权限类型（DataScopeType 枚举）：**
- `ALL` (1) - 无过滤
- `CUSTOM` (2) - `IN (dept_id)` 从 `sys_role_dept` 表
- `DEPT` (3) - `= dept_id`
- `DEPT_AND_CHILD` (4) - `FIND_IN_SET` 处理层级部门
- `SELF` (5) - `= user_id`

### 分层架构

```
Controller → Service → Mapper → Database
```

**AOP 横切关注点：**
- `@Log` - 操作日志
- `@DataScope` - 数据权限
- `@RepeatSubmit` - 防止重复提交
- `@Limiter` - 限流

## 配置

**主配置文件：** `iadmin-system/src/main/resources/application.yml`

**关键配置：**
- 服务端口：`8090`
- 数据库：MySQL `localhost:3306/iadmin`（Druid 连接池）
- Redis：`localhost:6379`，database 8
- JWT：4小时过期，BCrypt 密码加密
- MyBatis-Plus：分页启用（最大1000行），逻辑删除（1=已删除，0=正常）
- 文件上传：最大 10MB

**环境配置：**
- `application-dev.yml` - 开发环境
- `application-prod.yml` - 生产环境

## API 文档

访问 Swagger UI：`http://localhost:8090/swagger-ui.html`

API 分组：
- `system` - `/system/**` 接口
- `monitor` - `/monitor/**` 接口
- `generator` - `/generator/**` 接口
- `auth` - `/auth/**`、`/login/**`、`/logout/**` 接口

## Java 21 特性使用

代码库积极使用 Java 21 现代特性：

```java
// instanceof 模式匹配
if (e instanceof BadRequestException bre) {
    return HttpResult.error(bre.getStatus(), bre.getMessage());
}

// switch 表达式（DataScopeAspect.java:181）
String sql = switch (DataScopeType.fromCode(dataScope)) {
    case ALL -> { yield ""; }
    case CUSTOM -> { yield String.format(...); }
    // ...
};
```

## 全局异常处理

`GlobalExceptionHandler` 使用 `@RestControllerAdvice` 统一处理错误响应。

**响应格式：**
```java
HttpResult {
    int status;
    String message;
    Object data;
}
```

处理异常：`BadRequestException`、`JwtTokenException`、`BadCredentialsException`、`IllegalArgumentException`、通用 `Throwable`

## 核心控制器

**认证：**
- `LoginController` - `/auth/login`、`/auth/code`

**系统管理：**
- `SysUserController` - 用户 CRUD
- `SysRoleController` - 角色管理
- `SysRoleDeptController` - 角色部门关联（数据权限）
- `SysMenuController` - 菜单/权限管理
- `SysDictDataController`、`SysDictTypeController` - 字典管理

**审计与监控：**
- `SysOperLogController` - 操作日志
- `SysLogininforController` - 登录历史
- `CacheController` - Redis 缓存管理
- `ServerController`（monitor 模块）- 系统监控（OSHI）

## 特色功能

1. **在线用户管理** - 基于 Redis 的会话跟踪，通过 Token 黑名单强制登出
2. **缓存管理** - CacheController 进行 Redis 缓存操作
3. **系统监控** - 基于 OSHI 的 CPU、内存、JVM、磁盘监控
4. **代码生成器** - 数据库表内省，生成 Entity/Mapper/Service/Controller

## 数据库表

核心表：`sys_user`、`sys_role`（含 `data_scope` 字段）、`sys_role_dept`、`sys_dept`、`sys_menu`、`sys_dict_data`、`sys_dict_type`、`sys_oper_log`、`sys_logininfor`

## 包结构

**基础包：** `me.fjq`

- `annotation/` - 自定义注解
- `aspect/` - AOP 切面（DataScopeAspect）
- `config/` - 配置类
- `security/` - JWT 和 Spring Security 组件
- `system/controller/` - REST 控制器
- `system/entity/` - JPA/MyBatis 实体
- `system/mapper/` - MyBatis Mapper
- `system/service/` - 服务层
- `core/` - 核心类（HttpResult）
- `exception/` - 自定义异常 + GlobalExceptionHandler
- `utils/` - 工具类

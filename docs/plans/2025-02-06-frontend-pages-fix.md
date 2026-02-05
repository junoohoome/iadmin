# 前端页面修复实施计划

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**目标:** 修复前端页面无法打开以及样式问题，确保用户管理、角色管理、菜单管理等所有页面能正常访问和显示

**架构:** 前后端分离项目，前端 Vue 3 + TypeScript + Element Plus，后端 Spring Boot 3.2 + Java 21。通过 JWT 无状态认证，API 基础路径为 `/dev-api` (代理到 `http://127.0.0.1:8080`)

**技术栈:**
- 前端: Vue 3.5, TypeScript 5, Vite 5, Element Plus 2.9, Pinia, Vue Router 4
- 后端: Spring Boot 3.2.5, Java 21, Spring Security 6, MyBatis-Plus 3.5.8
- 构建: Maven (后端), npm/pnpm (前端)

---

## 问题分析

根据代码分析，可能存在的问题：

1. **前后端 API 接口不匹配** - 前端调用的 API 路径与后端 Controller 定义不一致
2. **返回数据格式不一致** - 前端期望的 JSON 结构与后端返回不匹配
3. **权限注解配置问题** - Spring Security 权限控制可能阻止请求
4. **CORS 跨域问题** - 前后端分离可能导致跨域请求失败
5. **静态资源加载问题** - 样式、脚本等资源可能无法正确加载
6. **路由配置问题** - 动态路由可能未正确加载

---

## Task 1: 诊断问题 - 检查前端和后端连接状态

**目标:** 确认前后端是否能正常通信，定位具体问题

**Files:**
- Modify: `iadmin-web/src/utils/request.ts` - 添加调试日志
- Check: `iadmin/iadmin-system/src/main/java/me/fjq/config/WebSecurityConfig.java`

**Step 1: 添加前端请求拦截器日志**

打开 `iadmin-web/src/utils/request.ts`，在请求拦截器中添加日志：

```typescript
// 请求拦截器
service.interceptors.request.use(
  config => {
    // 添加调试日志
    console.log('[Request]', config.method?.toUpperCase(), config.url, config.params || config.data)
    return config
  },
  error => {
    console.error('[Request Error]', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    console.log('[Response]', response.config.url, response.data)
    return response
  },
  error => {
    console.error('[Response Error]', error.config?.url, error.response?.data || error.message)
    return Promise.reject(error)
  }
)
```

**Step 2: 启动前后端服务**

```bash
# 终端 1 - 启动后端
cd /Users/fangjunqiang/MyWorkspace/IdeaProjects/my-project/iadmin-all/iadmin
mvn clean install -DskipTests
cd iadmin-system && mvn spring-boot:run

# 终端 2 - 启动前端
cd /Users/fangjunqiang/MyWorkspace/IdeaProjects/my-project/iadmin-all/iadmin-web
npm run dev
```

**Step 3: 打开浏览器控制台查看日志**

访问 `http://localhost:3000`，登录后尝试访问用户管理页面，查看控制台输出：
- 请求 URL 是否正确
- 请求参数是否正确
- 响应状态码和内容
- 错误信息

**Step 4: 记录问题信息**

将发现的问题记录下来，后续步骤将针对具体问题进行修复。

**Step 5: Commit**

```bash
git add iadmin-web/src/utils/request.ts
git commit -m "debug: add request/response logging for debugging"
```

---

## Task 2: 修复 API 接口路径不匹配问题

**目标:** 确保前端 API 调用路径与后端 Controller 一致

**Files:**
- Check: `iadmin-web/src/api/user.ts`
- Check: `iadmin-web/src/api/role.ts`
- Check: `iadmin-web/src/api/menu.ts`
- Check: `iadmin/iadmin-system/src/main/java/me/fjq/system/controller/SysUserController.java`
- Check: `iadmin/iadmin-system/src/main/java/me/fjq/system/controller/SysRoleController.java`
- Check: `iadmin/iadmin-system/src/main/java/me/fjq/system/controller/SysMenuController.java`

**Step 1: 对比用户管理 API**

前端 (`iadmin-web/src/api/user.ts`):
- GET `/sysUser` - 查询用户列表
- POST `/sysUser` - 新增用户
- PUT `/sysUser` - 修改用户
- DELETE `/sysUser/${idList}` - 删除用户

后端 (`SysUserController.java`):
- GET `/sysUser` - `selectAll()` ✓ 匹配
- POST `/sysUser` - `insert()` ✓ 匹配
- PUT `/sysUser` - `update()` ✓ 匹配
- DELETE `/sysUser` - `delete(@RequestParam("idList"))` ⚠️ 参数方式不匹配

**Step 2: 修复删除用户接口**

后端使用 `@RequestParam` 接收参数，但前端发送的是路径参数。修改后端：

```java
// 文件: iadmin/iadmin-system/src/main/java/me/fjq/system/controller/SysUserController.java

@DeleteMapping("{idList}")
public HttpResult delete(@PathVariable String idList) {
    List<Long> ids = Arrays.stream(idList.split(","))
        .map(Long::valueOf)
        .collect(Collectors.toList());
    return HttpResult.ok(this.sysUserService.removeByIds(ids));
}
```

或者修改前端，使用参数方式：

```typescript
// 文件: iadmin-web/src/api/user.ts
export function delUser(idList: number[]) {
  return request<ApiResponse<void>>({
    url: '/sysUser',
    method: 'delete',
    params: { idList }
  })
}
```

**推荐:** 修改后端以符合 RESTful 风格（路径参数）

**Step 3: 对比角色管理 API**

检查 `iadmin-web/src/api/role.ts` 与 `SysRoleController.java`，确认以下接口：
- GET `/sysRole` - 列表查询
- GET `/sysRole/selectOptions` - 获取选项
- POST `/sysRole` - 新增
- PUT `/sysRole` - 修改
- DELETE `/sysRole/{id}` - 删除

**Step 4: 对比菜单管理 API**

检查 `iadmin-web/src/api/menu.ts` 与 `SysMenuController.java`，确认以下接口：
- GET `/sysMenu/list` - 菜单列表
- GET `/sysMenu/treeSelect` - 菜单树
- POST `/sysMenu` - 新增
- PUT `/sysMenu` - 修改
- DELETE `/sysMenu` - 删除

**Step 5: 测试 API 连通性**

使用 curl 或 Postman 测试每个接口：

```bash
# 测试用户列表（需要先登录获取 token）
curl -X GET "http://localhost:8080/sysUser?page=1&pageSize=10" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 测试角色选项
curl -X GET "http://localhost:8080/sysRole/selectOptions" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 测试菜单列表
curl -X GET "http://localhost:8080/sysMenu/list" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Step 6: Commit**

```bash
git add iadmin/iadmin-system/src/main/java/me/fjq/system/controller/
git commit -m "fix: update API endpoints to match frontend requests"
```

---

## Task 3: 统一返回数据格式

**目标:** 确保后端返回的数据格式与前端期望一致

**Files:**
- Check: `iadmin/iadmin-common/src/main/java/me/fjq/core/HttpResult.java`
- Check: `iadmin-web/src/types/index.ts`

**Step 1: 检查后端 HttpResult 结构**

```java
// 预期结构
public class HttpResult {
    private int status;
    private String message;
    private Object data;
}
```

**Step 2: 检查前端期望的数据结构**

```typescript
// iadmin-web/src/types/index.ts
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
```

**Step 3: 修复字段名不匹配**

如果后端使用 `status` 而前端期望 `code`，修改后端：

```java
// 文件: iadmin/iadmin-common/src/main/java/me/fjq/core/HttpResult.java

@JsonProperty("code")
private int status;

// 或者使用序列化配置
@JsonFormat
public class HttpResult {
    @JsonProperty("code")
    private int status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private Object data;
}
```

或者修改前端适配后端：

```typescript
// 文件: iadmin-web/src/types/index.ts
export interface ApiResponse<T = any> {
  status: number  // 改为 status
  message: string
  data: T
}
```

**Step 4: 确保分页数据结构一致**

后端 MyBatis-Plus 分页返回：

```java
// 需要确保返回结构包含:
{
  "records": [...],
  "total": 100,
  "size": 10,
  "current": 1,
  "pages": 10
}
```

**Step 5: Commit**

```bash
git add iadmin/iadmin-common/src/main/java/me/fjq/core/HttpResult.java
git commit -m "fix: unify response data format between frontend and backend"
```

---

## Task 4: 修复 Spring Security 权限配置

**目标:** 确保前端请求不被 Spring Security 拦截

**Files:**
- Modify: `iadmin/iadmin-system/src/main/java/me/fjq/config/WebSecurityConfig.java`
- Check: `iadmin/iadmin-system/src/main/java/me/fjq/system/controller/SysUserController.java`

**Step 1: 检查权限注解**

后端 Controller 中的权限注解：
```java
@PreAuthorize("@ss.hasPerms('admin,system:user:list')")
```

**Step 2: 确认权限服务存在**

检查是否有 `@ss` 对应的权限评估器：

```bash
# 搜索文件
find iadmin-system -name "*.java" | xargs grep -l "hasPerms"
```

如果不存在，需要创建：

```java
// 文件: iadmin/iadmin-system/src/main/java/me/fjq/security/PermissionService.java

package me.fjq.security;

import me.fjq.security.JwtUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("ss")
public class PermissionService {

    public boolean hasPerms(String perms) {
        if (perms == null || perms.isEmpty()) {
            return false;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
        if (userDetails == null) {
            return false;
        }

        // 检查是否是管理员
        if ("admin".equals(userDetails.getUsername())) {
            return true;
        }

        // 检查权限
        String[] permissionArray = perms.split(",");
        for (String permission : permissionArray) {
            if (userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(permission))) {
                return true;
            }
        }
        return false;
    }
}
```

**Step 3: 更新 SecurityConfig 放行必要的请求**

确保 WebSecurityConfig 正确放行静态资源和 API：

```java
// 文件: iadmin/iadmin-system/src/main/java/me/fjq/config/WebSecurityConfig.java

.authorizeHttpRequests(auth -> auth
    // Swagger 文档
    .requestMatchers("/swagger-ui.html", "/swagger-ui/**",
            "/v3/api-docs/**", "/swagger-resources/**",
            "/webjars/**", "/api-docs/**").permitAll()
    // 文件
    .requestMatchers("/avatar/**", "/file/**").permitAll()
    // 阿里巴巴 druid
    .requestMatchers("/druid/**").permitAll()
    // 系统管理 API (临时放行用于调试)
    .requestMatchers("/sysUser/**", "/sysRole/**", "/sysMenu/**").permitAll()
    // 其他请求需要认证
    .anyRequest().authenticated()
)
```

**注意:** 生产环境应该移除 `/sysUser/**` 等放行，只通过权限注解控制。

**Step 4: Commit**

```bash
git add iadmin/iadmin-system/src/main/java/me/fjq/
git commit -m "fix: add permission service and update security config"
```

---

## Task 5: 修复 CORS 跨域问题

**目标:** 确保前端能正常调用后端 API

**Files:**
- Check: `iadmin/iadmin-system/src/main/java/me/fjq/config/WebMvcConfig.java`

**Step 1: 检查当前 CORS 配置**

确认 `WebMvcConfig.java` 中的 CORS 配置：

```java
@Bean
public CorsFilter corsFilter() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOriginPattern("*");  // 使用 allowedOriginPattern
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
}
```

**Step 2: 确保 CORS 配置正确**

确认：
- 使用 `addAllowedOriginPattern("*")` 而不是 `addAllowedOrigin("*")` (因为 allowCredentials=true)
- 或者使用具体的域名: `addAllowedOriginPattern("http://localhost:3000")`

**Step 3: 测试 CORS**

```bash
curl -X OPTIONS "http://localhost:8080/sysUser" \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET" \
  -v
```

响应头应包含:
```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Credentials: true
```

**Step 4: Commit**

```bash
git add iadmin/iadmin-system/src/main/java/me/fjq/config/WebMvcConfig.java
git commit -m "fix: update CORS configuration for frontend"
```

---

## Task 6: 修复页面样式问题

**目标:** 确保页面样式正确加载和显示

**Files:**
- Check: `iadmin-web/src/styles/index.scss`
- Check: `iadmin-web/src/views/system/user/index.vue`
- Check: `iadmin-web/src/views/system/role/index.vue`
- Check: `iadmin-web/src/views/system/menu/index.vue`

**Step 1: 检查全局样式导入**

确认 `main.ts` 中正确导入样式：

```typescript
// iadmin-web/src/main.ts
import './styles/index.scss'
import 'element-plus/dist/index.css'
```

**Step 2: 检查 Element Plus 按钮图标导入**

确认页面中正确使用图标：

```vue
<!-- iadmin-web/src/views/system/user/index.vue -->
<script setup lang="ts">
import { Search, Plus, Delete } from '@element-plus/icons-vue'
</script>

<template>
  <el-button :icon="Search">搜索</el-button>
  <el-button :icon="Plus">新增</el-button>
  <el-button :icon="Delete">删除</el-button>
</template>
```

**Step 3: 修复 svg-icon 组件**

确保 `svg-icon` 组件正确注册：

```typescript
// iadmin-web/src/main.ts
import SvgIcon from '@/components/SvgIcon/index.vue'

app.component('svg-icon', SvgIcon)
```

**Step 4: 检查布局组件样式**

确认 `app-container` 类有正确的样式：

```scss
// iadmin-web/src/styles/index.scss
.app-container {
  padding: 20px;
}
```

**Step 5: 修复表格宽度问题**

如果表格宽度不正确，添加容器样式：

```vue
<template>
  <div class="app-container">
    <el-table :data="tableData" style="width: 100%">
      <!-- ... -->
    </el-table>
  </div>
</template>
```

**Step 6: Commit**

```bash
git add iadmin-web/src/styles/ iadmin-web/src/main.ts
git commit -m "fix: update global styles and icon imports"
```

---

## Task 7: 修复动态路由加载问题

**目标:** 确保登录后能正确加载动态路由

**Files:**
- Check: `iadmin-web/src/permission.ts`
- Check: `iadmin-web/src/stores/permission.ts`
- Check: `iadmin-web/src/router/index.ts`

**Step 1: 检查路由权限控制**

确认 `permission.ts` 正确处理路由：

```typescript
// iadmin-web/src/permission.ts
import router from './router'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()

  if (userStore.token) {
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      if (!userStore.userId) {
        // 获取用户信息
        await userStore.getUserInfo()
        // 生成动态路由
        const accessRoutes = await permissionStore.generateRoutes()
        // 动态添加路由
        accessRoutes.forEach(route => {
          router.addRoute(route)
        })
        next({ ...to, replace: true })
      } else {
        next()
      }
    }
  } else {
    if (to.path === '/login') {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
    }
  }
})
```

**Step 2: 检查动态路由生成**

确认 `permission.ts` store 中的 `generateRoutes` 方法：

```typescript
// iadmin-web/src/stores/permission.ts
import { getRouters } from '@/api/login'
import { constantRoutes } from '@/router'

export const usePermissionStore = defineStore('permission', {
  state: () => ({
    routes: [],
    addRoutes: []
  }),
  actions: {
    async generateRoutes() {
      const { data } = await getRouters()
      const accessedRoutes = filterAsyncRoutes(data)
      this.addRoutes = accessedRoutes
      this.routes = constantRoutes.concat(accessedRoutes)
      return accessedRoutes
    }
  }
})
```

**Step 3: 检查后端路由接口**

确认后端有 `/getRouters` 接口返回路由数据：

```bash
curl -X GET "http://localhost:8080/getRouters" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Step 4: Commit**

```bash
git add iadmin-web/src/permission.ts iadmin-web/src/stores/
git commit -m "fix: update dynamic route loading logic"
```

---

## Task 8: 验证所有页面功能

**目标:** 确保所有页面能正常打开和操作

**Files:**
- Test: 所有系统管理页面

**Step 1: 启动前后端服务**

```bash
# 后端
cd /Users/fangjunqiang/MyWorkspace/IdeaProjects/my-project/iadmin-all/iadmin
mvn spring-boot:run -pl iadmin-system

# 前端
cd /Users/fangjunqiang/MyWorkspace/IdeaProjects/my-project/iadmin-all/iadmin-web
npm run dev
```

**Step 2: 测试登录功能**

访问 `http://localhost:3000/login`
- 输入用户名: `admin`
- 输入密码: `admin123`
- 点击登录
- 确认能成功登录并跳转到首页

**Step 3: 测试用户管理页面**

访问用户管理页面：
- 确认页面能正常加载
- 确认用户列表能正常显示
- 测试搜索功能
- 测试新增用户
- 测试编辑用户
- 测试删除用户
- 测试状态切换

**Step 4: 测试角色管理页面**

访问角色管理页面：
- 确认页面能正常加载
- 确认角色列表能正常显示
- 测试搜索功能
- 测试新增角色
- 测试编辑角色
- 测试删除角色
- 测试菜单分配功能

**Step 5: 测试菜单管理页面**

访问菜单管理页面：
- 确认页面能正常加载
- 确认菜单树能正常显示
- 测试搜索功能
- 测试新增菜单
- 测试编辑菜单
- 测试删除菜单

**Step 6: 记录测试结果**

创建测试报告，记录每个功能的测试结果：

| 功能 | 状态 | 备注 |
|------|------|------|
| 登录 | ✓/✗ | |
| 用户列表 | ✓/✗ | |
| 用户新增 | ✓/✗ | |
| 用户编辑 | ✓/✗ | |
| 用户删除 | ✓/✗ | |
| 角色列表 | ✓/✗ | |
| 角色新增 | ✓/✗ | |
| 角色编辑 | ✓/✗ | |
| 角色删除 | ✓/✗ | |
| 菜单列表 | ✓/✗ | |
| 菜单新增 | ✓/✗ | |
| 菜单编辑 | ✓/✗ | |
| 菜单删除 | ✓/✗ | |

**Step 7: 修复发现的问题**

根据测试结果，修复发现的任何问题。

**Step 8: Commit**

```bash
git add .
git commit -m "test: complete page functionality verification"
```

---

## Task 9: 清理调试代码

**目标:** 移除调试日志和临时放行配置

**Files:**
- Modify: `iadmin-web/src/utils/request.ts`
- Modify: `iadmin/iadmin-system/src/main/java/me/fjq/config/WebSecurityConfig.java`

**Step 1: 移除前端调试日志**

```typescript
// iadmin-web/src/utils/request.ts
// 移除或注释掉 console.log 调试语句
```

**Step 2: 移除后端临时放行**

```java
// iadmin/iadmin-system/src/main/java/me/fjq/config/WebSecurityConfig.java
// 移除临时的 .requestMatchers("/sysUser/**", "/sysRole/**", "/sysMenu/**").permitAll()
```

**Step 3: Commit**

```bash
git add iadmin-web/src/utils/request.ts iadmin/iadmin-system/src/main/java/me/fjq/config/WebSecurityConfig.java
git commit -m "chore: remove debug code and temporary access rules"
```

---

## 测试指南

### 前端测试

```bash
cd /Users/fangjunqiang/MyWorkspace/IdeaProjects/my-project/iadmin-all/iadmin-web
npm run dev      # 开发模式
npm run build    # 生产构建
```

### 后端测试

```bash
cd /Users/fangjunqiang/MyWorkspace/IdeaProjects/my-project/iadmin-all/iadmin
mvn clean install -DskipTests
mvn spring-boot:run -pl iadmin-system
```

### API 测试

```bash
# 获取 token
TOKEN=$(curl -s -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | jq -r '.data')

# 测试用户列表
curl -X GET "http://localhost:8080/sysUser?page=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN"

# 测试角色列表
curl -X GET "http://localhost:8080/sysRole?page=1&pageSize=10" \
  -H "Authorization: Bearer $TOKEN"

# 测试菜单列表
curl -X GET "http://localhost:8080/sysMenu/list" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 注意事项

1. **开发环境端口:**
   - 后端: `8080`
   - 前端: `3000`

2. **默认账号:**
   - 用户名: `admin`
   - 密码: `admin123`

3. **API 基础路径:**
   - 前端代理: `/dev-api` → `http://127.0.0.1:8080`

4. **数据库:**
   - 确保 MySQL 已启动
   - 确保数据库 `iadmin` 已创建
   - 确保已执行初始化 SQL

5. **Redis:**
   - 确保 Redis 已启动
   - 默认端口: `6379`
   - database: `8`

---

## 依赖的技能/文档

- @superpowers:systematic-debugging - 用于调试问题
- @superpowers:verification-before-completion - 完成前验证
- @superpowers:receiving-code-review - 接收代码审查反馈

---

**计划完成时间估计:** 2-4 小时

**风险等级:** 中等 (涉及前后端联调)

**下一步:** 确认执行方式（Subagent-Driven 或 Parallel Session）

# iAdmin 后端优化实施计划

> 生成日期: 2026-02-16
> 扫描范围: 安全性、性能、代码质量、架构

---

## 第一阶段：紧急安全问题（预计 1-2 小时）

### 1.1 添加 Controller 权限控制

**涉及文件：**
- `SysRoleController.java`
- `SysMenuController.java`
- `SysDeptController.java`
- `SysDictTypeController.java`
- `SysDictDataController.java`
- `SysUserController.java` (补充缺失的)
- `SysOnlineUserController.java`

**修改内容：**
```java
// 列表查询
@PreAuthorize("@ss.hasPerms('system:role:list')")
@GetMapping
public HttpResult selectAll(...) { ... }

// 新增
@PreAuthorize("@ss.hasPerms('system:role:add')")
@PostMapping
public HttpResult insert(...) { ... }

// 修改
@PreAuthorize("@ss.hasPerms('system:role:edit')")
@PutMapping
public HttpResult update(...) { ... }

// 删除
@PreAuthorize("@ss.hasPerms('system:role:del')")
@DeleteMapping
public HttpResult delete(...) { ... }
```

**权限标识规范：**
| 模块 | 权限前缀 |
|------|----------|
| 用户管理 | `system:user:*` |
| 角色管理 | `system:role:*` |
| 菜单管理 | `system:menu:*` |
| 部门管理 | `system:dept:*` |
| 字典类型 | `system:dict:*` |
| 字典数据 | `system:dict:*` |
| 操作日志 | `monitor:operlog:*` |
| 登录日志 | `monitor:loginlog:*` |
| 在线用户 | `monitor:online:*` |
| 缓存管理 | `monitor:cache:*` |

---

### 1.2 修复密码字段泄露

**文件：** `SysUser.java`

```java
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
private String password;
```

---

### 1.3 限制测试接口

**文件：** `LoginController.java`

```java
@Profile("dev")  // 添加此注解
@PostMapping(value = "testLogin")
public HttpResult testLogin(...) { ... }
```

---

### 1.4 收紧 CORS 配置

**文件：** `WebMvcConfig.java`

```java
// 方案1：使用配置文件
@Value("${cors.allowed-origins:http://localhost:3000}")
private String[] allowedOrigins;

// 方案2：生产环境禁用通配符
if ("prod".equals(activeProfile)) {
    config.setAllowedOrigins(Arrays.asList(allowedOrigins));
} else {
    config.addAllowedOriginPattern("*");
}
```

---

## 第二阶段：性能优化（预计 2-3 小时）

### 2.1 修复 N+1 插入问题

**文件：** `SysRoleServiceImpl.java`

```java
// 修改前
for (Long menuId : menuIds) {
    sysRoleMenuMapper.insert(roleMenu);
}

// 修改后
List<SysRoleMenu> roleMenuList = menuIds.stream()
    .map(menuId -> {
        SysRoleMenu rm = new SysRoleMenu();
        rm.setRoleId(roleId);
        rm.setMenuId(menuId);
        return rm;
    })
    .collect(Collectors.toList());
roleMenuService.saveBatch(roleMenuList);
```

---

### 2.2 添加字典缓存

**文件：** `SysDictDataServiceImpl.java`

```java
@Cacheable(value = "dict", key = "#dictType")
public List<SysDictData> listByDictType(String dictType) {
    return list(new LambdaQueryWrapper<SysDictData>()
        .eq(SysDictData::getDictType, dictType)
        .orderByAsc(SysDictData::getDictSort));
}

@CacheEvict(value = "dict", key = "#sysDictData.dictType")
public boolean saveWithCache(SysDictData sysDictData) {
    return save(sysDictData);
}
```

**配置：** `application.yml`

```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1小时
```

---

### 2.3 优化 Redis SCAN

**文件：** `CacheController.java`

```java
ScanOptions options = ScanOptions.scanOptions()
    .match(pattern)
    .count(100)  // 限制每次扫描数量
    .build();
```

---

## 第三阶段：代码质量（预计 2-3 小时）

### 3.1 补充 @Log 注解

**涉及文件：**
- `SysMenuController.java`
- `SysDeptController.java`
- `SysDictTypeController.java`
- `SysDictDataController.java`

**示例：**
```java
@Log(title = "菜单管理", businessType = 1)  // 1=新增
@PostMapping
public HttpResult insert(@RequestBody SysMenu sysMenu) { ... }

@Log(title = "菜单管理", businessType = 2)  // 2=修改
@PutMapping
public HttpResult update(@RequestBody SysMenu sysMenu) { ... }

@Log(title = "菜单管理", businessType = 3)  // 3=删除
@DeleteMapping("{id}")
public HttpResult delete(@PathVariable Long id) { ... }
```

---

### 3.2 添加事务注解

**文件：** `SysUserServiceImpl.java`, `SysMenuServiceImpl.java`

```java
@Service
@Transactional(rollbackFor = Exception.class)
public class SysUserServiceImpl ... { ... }
```

---

### 3.3 完善异常处理

**文件：** `GlobalExceptionHandler.java`

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public HttpResult handleValidationException(MethodArgumentNotValidException e) {
    String message = e.getBindingResult().getAllErrors().stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .collect(Collectors.joining(", "));
    return HttpResult.error(HttpStatus.BAD_REQUEST.value(), message);
}

@ExceptionHandler(BindException.class)
public HttpResult handleBindException(BindException e) {
    String message = e.getBindingResult().getAllErrors().stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .collect(Collectors.joining(", "));
    return HttpResult.error(HttpStatus.BAD_REQUEST.value(), message);
}
```

---

### 3.4 提取魔法数字为常量

**新建文件：** `MenuConstants.java`

```java
public class MenuConstants {
    public static final Long ROOT_PARENT_ID = 0L;
    public static final String VISIBLE_SHOW = "0";
    public static final String VISIBLE_HIDDEN = "1";
    public static final String MENU_TYPE_DIR = "M";
    public static final String MENU_TYPE_MENU = "C";
    public static final String MENU_TYPE_BUTTON = "F";
}
```

---

## 第四阶段：功能完善（预计 3-4 小时）

### 4.1 实现文件上传

**文件：** `SysUserController.java`

```java
@Value("${upload.path:/tmp/uploads}")
private String uploadPath;

@PostMapping("profile/avatar")
public HttpResult<String> uploadAvatar(@RequestParam("avatarFile") MultipartFile file) {
    // 1. 校验文件类型和大小
    if (!file.getContentType().startsWith("image/")) {
        return HttpResult.error("只能上传图片文件");
    }
    if (file.getSize() > 2 * 1024 * 1024) {
        return HttpResult.error("文件大小不能超过2MB");
    }

    // 2. 生成文件名
    String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
    String fileName = UUID.randomUUID().toString() + ext;

    // 3. 保存文件
    File dest = new File(uploadPath + "/avatar/" + fileName);
    dest.getParentFile().mkdirs();
    file.transferTo(dest);

    // 4. 更新数据库
    // ...

    return HttpResult.ok("/avatar/" + fileName);
}
```

---

### 4.2 实现字典缓存刷新

**文件：** `SysDictDataController.java`

```java
@CacheEvict(value = "dict", allEntries = true)
@PreAuthorize("@ss.hasPerms('system:dict:edit')")
@PostMapping("refreshCache")
public HttpResult refreshCache() {
    return HttpResult.ok("缓存已刷新");
}
```

---

### 4.3 添加数据库索引

**新建文件：** `sql/add_indexes.sql`

```sql
-- 用户表索引
CREATE INDEX idx_user_status ON sys_user(status, del_flag);

-- 角色表索引
CREATE INDEX idx_role_del_flag ON sys_role(del_flag);

-- 菜单表索引
CREATE INDEX idx_menu_parent_id ON sys_menu(parent_id);
```

---

## 验收标准

### 第一阶段验收
- [ ] 所有 Controller 增删改接口都有权限控制
- [ ] API 响应中不包含 password 字段
- [ ] 生产环境无法访问 /auth/testLogin
- [ ] CORS 只允许配置的域名

### 第二阶段验收
- [ ] 角色权限分配使用批量插入
- [ ] 字典数据使用 Redis 缓存
- [ ] 缓存管理页面不卡顿

### 第三阶段验收
- [ ] 所有增删改操作都有日志记录
- [ ] 多表操作有事务控制
- [ ] 参数校验失败返回友好提示

### 第四阶段验收
- [ ] 头像上传功能可用
- [ ] 字典缓存刷新功能可用
- [ ] 数据库慢查询减少

---

## 时间安排建议

| 阶段 | 预计时间 | 优先级 |
|------|----------|--------|
| 第一阶段 | 1-2 小时 | 紧急 |
| 第二阶段 | 2-3 小时 | 高 |
| 第三阶段 | 2-3 小时 | 中 |
| 第四阶段 | 3-4 小时 | 低 |

**总计：8-12 小时**

# JVM 启动参数配置指南

## 适用场景

| 场景 | 服务器配置 | 堆内存 | 说明 |
|------|-----------|--------|------|
| 开发环境 | 4核8G | 2G | 本地开发调试 |
| 测试环境 | 4核8G | 4G | 功能测试 |
| 生产环境（小型） | 4核8G | 4G | 低并发场景 |
| 生产环境（标准） | 8核16G | 8G | 中等并发场景 |
| 生产环境（高配） | 16核32G | 16G | 高并发场景 |

---

## 启动脚本

### 开发/测试环境（4核8G）

```bash
#!/bin/bash
# startup-dev.sh

java -jar iadmin-system.jar \
  -Xms2g \
  -Xmx2g \
  -XX:+UseZGC \
  -XX:MaxGCPauseMillis=10 \
  -XX:+AlwaysPreTouch \
  -XX:+UseStringDeduplication \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.profiles.active=dev
```

### 生产环境 - 小型（4核8G）

```bash
#!/bin/bash
# startup-prod-small.sh

java -jar iadmin-system.jar \
  -Xms4g \
  -Xmx4g \
  -XX:+UseZGC \
  -XX:MaxGCPauseMillis=10 \
  -XX:+AlwaysPreTouch \
  -XX:+UseStringDeduplication \
  -XX:+ParallelRefProcEnabled \
  -XX:+UseCompressedClassPointers \
  -XX:+UseCompressedOops \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.profiles.active=prod \
  -Dserver.port=8090 \
  -DTOMCAT_MAX_THREADS=200 \
  -DTOMCAT_MIN_SPARE=50 \
  -DTOMCAT_MAX_CONNECTIONS=5000 \
  -DDRUID_MAX_ACTIVE=150 \
  -DREDIS_MAX_ACTIVE=80
```

### 生产环境 - 标准（8核16G）

```bash
#!/bin/bash
# startup-prod-standard.sh

java -jar iadmin-system.jar \
  -Xms8g \
  -Xmx8g \
  -XX:+UseZGC \
  -XX:MaxGCPauseMillis=10 \
  -XX:ZCollectionInterval=5 \
  -XX:+UnlockExperimentalVMOptions \
  -XX:+AlwaysPreTouch \
  -XX:+UseStringDeduplication \
  -XX:+ParallelRefProcEnabled \
  -XX:+UseCompressedClassPointers \
  -XX:+UseCompressedOops \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.profiles.active=prod \
  -Dserver.port=8090 \
  -DTOMCAT_MAX_THREADS=300 \
  -DTOMCAT_MIN_SPARE=80 \
  -DTOMCAT_MAX_CONNECTIONS=8000 \
  -DDRUID_MAX_ACTIVE=300 \
  -DDRUID_MIN_IDLE=50 \
  -DREDIS_MAX_ACTIVE=150
```

### 生产环境 - 高配（16核32G）

```bash
#!/bin/bash
# startup-prod-high.sh

java -jar iadmin-system.jar \
  -Xms16g \
  -Xmx16g \
  -XX:+UseZGC \
  -XX:MaxGCPauseMillis=10 \
  -XX:ZCollectionInterval=3 \
  -XX:+UnlockExperimentalVMOptions \
  -XX:SoftMaxHeapSize=14g \
  -XX:+AlwaysPreTouch \
  -XX:+UseStringDeduplication \
  -XX:+ParallelRefProcEnabled \
  -XX:+UseCompressedClassPointers \
  -XX:+UseCompressedOops \
  -XX:MaxDirectMemorySize=2g \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.profiles.active=prod \
  -Dserver.port=8090 \
  -DTOMCAT_MAX_THREADS=500 \
  -DTOMCAT_MIN_SPARE=100 \
  -DTOMCAT_MAX_CONNECTIONS=10000 \
  -DDRUID_MAX_ACTIVE=500 \
  -DDRUID_MIN_IDLE=80 \
  -DDRUID_INITIAL_SIZE=80 \
  -DREDIS_MAX_ACTIVE=200
```

---

## 参数说明

### 内存配置

| 参数 | 说明 |
|------|------|
| `-Xms` | 初始堆内存，建议与 `-Xmx` 相同，避免动态扩容 |
| `-Xmx` | 最大堆内存，通常为物理内存的 50% |
| `-XX:MaxDirectMemorySize` | 直接内存大小（NIO 使用） |

### GC 配置

| 参数 | 说明 |
|------|------|
| `-XX:+UseZGC` | 使用 ZGC 垃圾回收器（Java 21 推荐） |
| `-XX:MaxGCPauseMillis` | 目标 GC 暂停时间（毫秒） |
| `-XX:ZCollectionInterval` | ZGC 主动回收间隔（秒） |
| `-XX:+AlwaysPreTouch` | 启动时预分配内存 |
| `-XX:+UseStringDeduplication` | 字符串去重，减少内存占用 |
| `-XX:+ParallelRefProcEnabled` | 并行处理引用对象 |

### 性能优化

| 参数 | 说明 |
|------|------|
| `-XX:+UseCompressedClassPointers` | 压缩类指针（节省内存） |
| `-XX:+UseCompressedOops` | 压缩对象指针（节省内存） |
| `-Djava.security.egd=file:/dev/./urandom` | 使用非阻塞随机数生成器 |

---

## Docker 部署示例

```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY iadmin-system.jar app.jar

# 默认使用标准配置
ENV JAVA_OPTS="-Xms8g -Xmx8g -XX:+UseZGC -XX:MaxGCPauseMillis=10 -XX:+AlwaysPreTouch -XX:+UseStringDeduplication"
ENV SPRING_PROFILES_ACTIVE="prod"
ENV TOMCAT_MAX_THREADS="300"
ENV DRUID_MAX_ACTIVE="300"

EXPOSE 8090

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE -DTOMCAT_MAX_THREADS=$TOMCAT_MAX_THREADS -DDRUID_MAX_ACTIVE=$DRUID_MAX_ACTIVE -jar app.jar"]
```

---

## 调优建议

### 1. 堆内存设置原则

- 初始堆（`-Xms`）= 最大堆（`-Xmx`）
- 堆内存 = 物理内存 × 50%（留一半给操作系统和其他进程）
- 年轻代大小让 JVM 自动调整

### 2. GC 选择

- **Java 21 推荐使用 ZGC**：低延迟、高吞吐
- G1GC 作为备选（兼容性考虑）
- 避免使用 CMS（已废弃）

### 3. 监控指标

- GC 频率和暂停时间
- 堆内存使用率
- CPU 使用率
- 线程数和线程状态

### 4. 故障排查

```bash
# 查看进程 PID
jps -l

# 生成堆转储
jmap -dump:format=b,file=heap.hprof <pid>

# 查看线程栈
jstack <pid> > thread.txt

# 使用 Arthas 诊断
java -jar arthas-boot.jar
```

---

## 环境变量速查表

| 环境变量 | 默认值 | 说明 |
|----------|--------|------|
| `TOMCAT_MAX_THREADS` | 200 | Tomcat 最大线程数 |
| `TOMCAT_MIN_SPARE` | 50 | Tomcat 最小空闲线程 |
| `TOMCAT_MAX_CONNECTIONS` | 5000 | Tomcat 最大连接数 |
| `DRUID_INITIAL_SIZE` | 30 | Druid 初始连接数 |
| `DRUID_MIN_IDLE` | 30 | Druid 最小空闲连接 |
| `DRUID_MAX_ACTIVE` | 200 | Druid 最大连接数 |
| `REDIS_MAX_ACTIVE` | 100 | Redis 最大连接数 |
| `REDIS_MAX_IDLE` | 50 | Redis 最大空闲连接 |
| `REDIS_MIN_IDLE` | 20 | Redis 最小空闲连接 |

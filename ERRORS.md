# 错误记录

> 项目：情侣空间 (couple-backend / couple-frontend)
> 每个错误记录现象、根因、修正措施，按时间倒序排列。

---

## 错误 #1：Minio 文件上传成功后前端仍然显示"等待上传中"

**日期**：2026-05-19

### 现象

1. 在发布动态页面上传图片/视频，进度条卡在 90%，发布按钮一直显示"等待上传完成..."
2. 检查 Minio 存储桶，文件实际上已经上传成功
3. 前端没有报错 toast，状态始终不更新

### 根因

两个问题叠加导致：

**根因 A — 后端 MinioClient 构建方式不稳健**

`MinioConfig.java` 中将完整的 endpoint URL（如 `http://192.168.65.134:9000`）直接传给 `MinioClient.builder().endpoint()`。SDK 内部使用 `okhttp3.HttpUrl.parse()` 解析时，对 IP 地址 URL 的处理可能存在边界情况，导致 HTTP 连接建立不稳定。

**根因 B — 前端 Vue 响应式未触发**

`PostView.vue` 的 `uploadSingle()` 函数直接修改数组内对象的属性（`item.uploading = false`、`item.url = res.url`）。Vue 3 的 Proxy 响应式系统在某些场景下对深层对象属性的直接赋值无法触发视图更新，导致 `computed` 属性（`anyUploading`、`allUploaded`）不重新计算。

### 修正

**后端 `MinioConfig.java`**（文件：`couple-backend/src/main/java/com/couple/config/MinioConfig.java`）

```java
// 修改前
MinioClient.builder()
    .endpoint(endpoint)  // "http://192.168.65.134:9000" 整串传入
    .credentials(accessKey, secretKey)
    .build();

// 修改后
URL url = new URL(endpoint);
boolean secure = "https".equals(url.getProtocol());
String host = url.getHost();
int port = url.getPort() == -1 ? (secure ? 443 : 9000) : url.getPort();

OkHttpClient httpClient = new OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build();

MinioClient.builder()
    .endpoint(host, port, secure)  // 显式指定 host/port/secure
    .credentials(accessKey, secretKey)
    .httpClient(httpClient)        // 自定义超时
    .build();
```

**前端 `PostView.vue`**（文件：`couple-frontend/src/views/PostView.vue`）

```typescript
// 修改前：直接修改对象属性
async function uploadSingle(item: FileItem) {
  item.uploading = true
  item.progress = 0
  // ...await upload...
  item.progress = 100
  item.url = res.url
  item.uploading = false
}

// 修改后：通过索引替换整个对象，强制触发响应式
function updateItem(id: number, patch: Partial<FileItem>) {
  const idx = fileItems.value.findIndex(f => f.id === id)
  if (idx !== -1) {
    fileItems.value[idx] = { ...fileItems.value[idx], ...patch }
  }
}

async function uploadSingle(item: FileItem) {
  updateItem(item.id, { uploading: true, progress: 0, error: undefined })
  // ...await upload...
  updateItem(item.id, { progress: 100, url: res.url, uploading: false })
}
```

### 教训

1. 使用 Minio Java SDK 时，优先用 `endpoint(host, port, secure)` 显式三参数形式，避免 SDK 内部 URL 解析的不确定性
2. Vue 3 中对 `ref` 数组内的对象做属性修改时，用**对象替换**而非**属性赋值**，确保计算属性重新求值
3. 文件上传类问题排查顺序：先确认文件是否到达存储（Minio 控制台查看），再排查前后端状态同步

---

## 错误 #2：YAML 中文注释导致应用启动失败

**日期**：2026-05-19

### 现象

Spring Boot 启动时报错：`org.yaml.snakeyaml.error.YAMLException: java.nio.charset.MalformedInputException: Input length = 1`

### 根因

`application.yml` 中 `mock-ip` 行末尾有中文注释 `# 本地测试用（南京），部署后删除或留空`，SnakeYAML 解析时文件编码与注释中的中文字符冲突。

### 修正

删除 `application.yml` 中的中文注释。

---

## 错误 #3：`application-prod.yml` 在 Docker 构建时被编译进 Jar，URL 换行导致 YAML 解析失败

**日期**：2026-05-20

### 现象

后端容器反复报错 `could not find expected ':'`，指向 `ia/Shanghai` 行，实际是 `serverTimezone=Asia/Shanghai` 被终端意外换行。

### 根因

1. `application-prod.yml` 中 JDBC URL 太长，粘贴时被终端断行
2. 即使服务器端文件修复，旧版坏文件已编译进 Jar 包

### 修正

放弃 `application-prod.yml`，改用 `docker-compose.yml` 中 `environment` 环境变量覆盖（`SPRING_DATASOURCE_URL`、`MYSQL_HOST` 等），同时删除 `SPRING_PROFILES_ACTIVE: prod` 避免加载坏文件。

---

## 错误 #4：MinIO 启动失败 — 密码太短

**日期**：2026-05-19

### 现象

MinIO 容器 `unhealthy`，日志：`MINIO_ROOT_PASSWORD length at least 8 characters`

### 根因

`root123` 只有 7 位，MinIO 要求至少 8 位。

### 修正

改为 `root1234`（8 位以上）。

---

## 错误 #5：Docker Hub 镜像拉取超时

**日期**：2026-05-19

### 现象

`docker compose up` 报 `dial tcp 4.78.139.50:443: connect: connection refused`

### 根因

国内服务器无法直接访问 Docker Hub。

### 修正

1. `/etc/docker/daemon.json` 配置镜像加速器
2. Dockerfile 基础镜像加国内源前缀 `docker.m.daocloud.io/library/`

---

## 错误 #6：JWT 密钥太短导致后端无法启动

**日期**：2026-05-20

### 现象

`WeakKeyException: The specified key byte array is 128 bits which is not secure enough`

### 根因

JWT HMAC-SHA 算法要求密钥 >= 256 位（32 字符）。

### 修正

`JWT_SECRET` 改为 40+ 字符的长字符串。

---

## 错误 #7：MySQL 连接拒绝 — 用了 localhost 而非 Docker 服务名

**日期**：2026-05-20

### 现象

注册/登录返回 500，后端日志：`java.net.ConnectException: Connection refused`

### 根因

Jar 包内默认 `application.yml` 中 MySQL URL 是 `localhost:3306`，Docker 容器内 localhost 指向自身而非 MySQL 容器。

### 修正

通过环境变量 `SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/couple_db?...` 覆盖数据源地址。

---

## 错误 #8：Nginx 反复重启 — upstream 主机名不存在

**日期**：2026-05-20

### 现象

Nginx 容器反复重启，日志：`host not found in upstream "backend"`

### 根因

`nginx.conf` 中 `proxy_pass http://backend:8080` 用了 `backend`，但容器名是 `couple-backend`。

### 修正

1. 将 `backend` 全部改为 `couple-backend`
2. 最终方案：放弃 Docker 内 Nginx，宿主机直接装 Nginx 做反向代理

---

## 错误 #9：内存耗尽导致服务器卡死

**日期**：2026-05-20

### 现象

`free -h` 显示 1.6G 内存用尽，SSH 无响应，所有命令卡住。

### 根因

1.6G 内存同时跑 MySQL + MinIO + Java + npm 构建，总用量接近 2G。

### 修正

1. 阿里云控制台强制重启
2. 增加 2G swap
3. Nginx 改为宿主机直装，避免同时构建前后端
4. MySQL `innodb_buffer_pool_size` 降至 128M

---

## 错误 #10：上传的图片无法在前端显示

**日期**：2026-05-20

### 现象

MinIO 中文件存在，但前端图片裂图/空白。

### 根因

`MINIO_ENDPOINT=http://minio:9000` 是 Docker 内部地址，浏览器无法解析。

### 修正

改为公网地址 `http://106.15.36.179:9000`，同时阿里云防火墙放通 9000 端口。

---

## 错误 #11：GitHub 代码无法直接拉取

**日期**：2026-05-19

### 现象

`git clone/pull` 超时：`GnuTLS recv error (-110)`

### 根因

国内服务器访问 GitHub 不稳定。

### 修正

使用 ghproxy 代理：`git pull https://mirror.ghproxy.com/https://github.com/qianshb/couple_space.git main`

---

## 错误 #12：docker-compose.yml 在服务器上反复编辑导致缩进混乱

**日期**：2026-05-20

### 现象

反复出现 `yaml: mapping values are not allowed`、`did not find expected key` 等 YAML 解析错误。

### 根因

在 nano / echo / tee 之间反复修改，空格和缩进不一致。

### 修正

在本地编辑验证无误后 commit 到 Git，服务器 pull 拉取。**不要在服务器上反复编辑 YAML 文件**。

---

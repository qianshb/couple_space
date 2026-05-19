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

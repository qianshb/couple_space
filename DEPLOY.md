# 情侣空间 — 生产环境部署全流程

> 适用：阿里云轻量应用服务器 Ubuntu 22.04+（2核2G 最低，推荐 2核4G）
> 部署方式：Docker Compose 编排 + 宿主机 Nginx 反向代理

---

## 一、架构总览

```
浏览器 (http://你的IP)
    │
    ▼
┌──────────────────────┐
│  Nginx (宿主机)       │  ← 80 端口，静态文件 + /api/ 反向代理
│  /etc/nginx/sites-   │
│  enabled/couple      │
└──────┬───────────────┘
       │ /api/
       ▼
┌──────────────────────┐
│  Spring Boot 后端     │  ← Docker 容器 couple-backend :8080
│  Java 21             │
└──────┬───────┬───────┘
       │       │
       ▼       ▼
┌──────────┐ ┌──────────┐
│ MySQL    │ │ MinIO    │
│ :3306    │ │ :9000    │
│ Docker   │ │ Docker   │
└──────────┘ └──────────┘
```

---

## 二、前置准备

### 2.1 服务器选购

- **平台**：阿里云 → 轻量应用服务器
- **镜像**：系统镜像 → Ubuntu 22.04
- **配置**：最低 2核2G（当前使用 2核1.6G）
- **网络**：3Mbps+

### 2.2 安全组 / 防火墙放通端口

| 端口 | 用途 | 必须 |
|------|------|------|
| 22 | SSH | ✅ |
| 80 | 网站前端 | ✅ |
| 443 | HTTPS（配置 SSL 后） | 推荐 |
| 8080 | 后端 API 调试 | 可选 |
| 9000 | MinIO 文件存储 | ✅ |
| 9001 | MinIO 管理后台 | 推荐 |
| 3306 | MySQL（Navicat 直连） | 可选 |

### 2.3 SSH 连接服务器

```bash
ssh root@你的服务器公网IP
```

首次登录会提示改密码。

---

## 三、环境安装

### 3.1 安装 Docker

```bash
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
```

退出重新 SSH 登录使权限生效。

### 3.2 安装 Docker Compose 插件

```bash
sudo apt update
sudo apt install -y docker-compose-v2
# 如果与旧版 docker-compose-plugin 冲突：
sudo dpkg --force-all -r docker-compose-plugin
sudo apt install -y docker-compose-v2
```

### 3.3 配置 Docker 镜像加速（国内必须）

```bash
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json << 'EOF'
{
    "registry-mirrors": [
        "https://mirror.ccs.tencentyun.com",
        "https://docker.m.daocloud.io"
    ]
}
EOF
sudo systemctl daemon-reload
sudo systemctl restart docker
```

### 3.4 安装 Nginx + Git

```bash
sudo apt install -y nginx git
```

### 3.5 增加 Swap（防止内存溢出）

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
free -h  # 验证 Swap 出现
```

---

## 四、部署项目

### 4.1 拉取代码

```bash
git clone https://mirror.ghproxy.com/https://github.com/qianshb/couple_space.git
cd couple_space
```

之后每次更新：
```bash
cd ~/couple_space
git pull https://mirror.ghproxy.com/https://github.com/qianshb/couple_space.git main
```

### 4.2 修改生产配置

确认 `docker-compose.yml` 中 `MINIO_ENDPOINT` 已改为你的**服务器公网 IP**：

```yaml
MINIO_ENDPOINT: http://你的公网IP:9000
```

### 4.3 构建并启动

```bash
cd ~/couple_space
sudo docker compose up -d --build
```

首次构建需下载 Maven 依赖，约 10-20 分钟。之后重启只需：
```bash
sudo docker compose up -d
```

### 4.4 启动后验证

```bash
sudo docker compose ps
```

四个服务都应显示 `Up` 或 `healthy`：
| 服务名 | 端口 |
|--------|------|
| couple-mysql | 3306 |
| couple-minio | 9000, 9001 |
| couple-backend | 8080 |
| couple-nginx | 80 |

### 4.5 确认前端静态文件

如果 Docker 构建了 nginx 镜像，从镜像中提取前端静态文件：

```bash
sudo docker create --name temp-nginx couple_space-nginx:latest
sudo mkdir -p /var/www/couple
sudo docker cp temp-nginx:/usr/share/nginx/html/. /var/www/couple/
sudo docker rm temp-nginx
ls /var/www/couple/  # 应该看到 index.html、assets/
```

### 4.6 配置宿主机 Nginx

```bash
sudo tee /etc/nginx/sites-available/couple << 'EOF'
server {
    listen 80;
    server_name _;
    root /var/www/couple;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
EOF

sudo rm -f /etc/nginx/sites-enabled/default
sudo ln -sf /etc/nginx/sites-available/couple /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl restart nginx
```

### 4.7 浏览器验证

打开 `http://你的服务器IP`，注册/登录测试。

---

## 五、常用运维命令

### 5.1 Docker 容器管理

```bash
# 查看所有容器状态
sudo docker compose ps

# 查看后端日志
sudo docker logs couple-backend --tail 50

# 重启单个服务
sudo docker compose restart backend

# 完全重建（改代码后）
sudo docker compose build backend --no-cache
sudo docker compose up -d backend

# 全部停止
sudo docker compose down

# 全部启动
sudo docker compose up -d
```

### 5.2 数据库操作

```bash
# 进入 MySQL 命令行
sudo docker exec -it couple-mysql mysql -uroot -proot123 couple_db

# 导出一条 SQL
sudo docker exec couple-mysql mysqldump -uroot -proot123 couple_db > backup.sql

# 查看用户数据
sudo docker exec couple-mysql mysql -uroot -proot123 couple_db -e "SELECT id, username, city FROM users;"
```

### 5.3 MinIO 管理

浏览器访问 `http://你的IP:9001`，用户名 `root`，密码 `root1234`。

### 5.4 系统监控

```bash
free -h                           # 内存
df -h                             # 磁盘
sudo docker stats --no-stream     # 容器资源
```

---

## 六、已知问题与修复速查

| 问题 | 修复 |
|------|------|
| 图片上传成功但前端不显示 | `MINIO_ENDPOINT` 改为公网 IP，防火墙放通 9000 |
| 后端启动报 JWT 错误 | `JWT_SECRET` 改成 32 字符以上 |
| MySQL 连接拒绝 | `SPRING_DATASOURCE_URL` 中 host 用 `mysql` 而非 `localhost` |
| Docker Hub 拉镜像超时 | 配置 `registry-mirrors` 镜像加速 |
| 服务器卡死 | 阿里云强制重启，然后加 2G swap |
| YAML 解析错误 | 不要在服务器上编辑 YAML，在本地编辑后 push |
| GitHub 克隆失败 | 加 `https://mirror.ghproxy.com/` 代理前缀 |

---

## 七、后续优化建议

1. **升级服务器配置**：2核4G 以上，避免 OOM
2. **绑定域名 + SSL**：配置 HTTPS，用 Let's Encrypt 免费证书
3. **数据库定期备份**：crontab 定时 `mysqldump` + 上传到 OSS
4. **日志管理**：配置 Docker 日志轮转，避免占满磁盘
5. **CI/CD**：配置 GitHub Actions 自动构建并部署

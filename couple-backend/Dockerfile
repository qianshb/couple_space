# ============================================
# 后端 Dockerfile — 多阶段构建
# ============================================

# ---- Stage 1: 构建 ----
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# ---- Stage 2: 运行 ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 下载 GeoLite2 数据库
RUN mkdir -p /data/geoip && \
    wget -q -O /data/geoip/GeoLite2-City.mmdb \
    "https://mirror.ghproxy.com/https://github.com/P3TERX/GeoLite.mmdb/releases/latest/download/GeoLite2-City.mmdb" || \
    wget -q -O /data/geoip/GeoLite2-City.mmdb \
    "https://raw.githubusercontent.com/P3TERX/GeoLite.mmdb/download/GeoLite2-City.mmdb" || \
    echo "GeoIP download failed, location will show as unknown"

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# ============================================
# PawMatch 后端 Dockerfile（多阶段构建）
# ============================================

# ---- 阶段 1：构建 ----
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# 先复制 pom 利用 Docker 缓存
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# 复制源码并打包（跳过测试以加速）
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---- 阶段 2：运行时 ----
FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="PawMatch Team"
LABEL version="1.0.0"

# 时区与 JVM 参数
ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC" \
    SPRING_PROFILES_ACTIVE=prod

WORKDIR /app

# 创建非 root 用户
RUN addgroup -S pawmatch && adduser -S pawmatch -G pawmatch

# 复制打包好的 jar（重命名为 app.jar）
COPY --from=builder /build/target/pawmatch-1.0.0.jar /app/app.jar

# 时区数据
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/${TZ} /etc/localtime && \
    echo "${TZ}" > /etc/timezone

# 上传 / 临时目录给 pawmatch 用户
RUN mkdir -p /app/logs /app/tmp && \
    chown -R pawmatch:pawmatch /app

USER pawmatch

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
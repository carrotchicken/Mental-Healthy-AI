# ============================================
# Spring Boot Backend Dockerfile (多阶段构建)
# ============================================
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
# 预下载依赖（利用 Docker 缓存层）
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 将 example 配置复制为正式配置（敏感值由环境变量覆盖）
COPY --from=builder /build/src/main/resources/application-example.yml ./application.yml
COPY --from=builder /build/target/*.jar app.jar

# 启动预热脚本：先开简易 HTTP 扛健康检查，等 Spring Boot 就绪后再交接
COPY startup.sh .
RUN chmod +x startup.sh

EXPOSE 8081

ENTRYPOINT ["/app/startup.sh"]

#!/bin/sh
# ============================================
# 启动脚本：直接启动 Spring Boot
# CloudBase 健康检查会等待 Tomcat 就绪
# ============================================

echo "[startup] 启动 Spring Boot..."
exec java -jar /app/app.jar --spring.config.location=file:/app/application.yml

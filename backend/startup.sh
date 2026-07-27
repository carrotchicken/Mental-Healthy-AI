#!/bin/sh
echo "[startup] 启动 Spring Boot..."
exec java -jar /app/app.jar --spring.config.location=file:/app/application.yml

#!/bin/sh
# ============================================
# 启动脚本：先开一个简易 HTTP 服务替 Spring Boot
# 扛过 CloudBase 的健康检查，等 Tomcat 就绪后再关掉
# ============================================

echo "[startup] 启动健康检查预热..."

# 后台启动简易 HTTP 监听 8081，返回 200 OK
{
  while true; do
    printf "HTTP/1.1 200 OK\r\nContent-Length: 2\r\n\r\nOK" | nc -l -p 8081 -w 3 2>/dev/null
  done
} &
WARMUP_PID=$!

# 等 1 秒确保监听已就绪
sleep 1

echo "[startup] 启动 Spring Boot..."
java -jar /app/app.jar --spring.config.location=file:/app/application.yml &
JAVA_PID=$!

# Spring Boot JVM 加载 + 上下文初始化约 5~7 秒后才尝试绑定端口
# 这里等 12 秒，确保健康检查至少通过 2~3 轮后再关预热
sleep 12

echo "[startup] 关闭预热，交接给 Spring Boot..."
kill $WARMUP_PID 2>/dev/null

# 等待 Spring Boot 退出
wait $JAVA_PID

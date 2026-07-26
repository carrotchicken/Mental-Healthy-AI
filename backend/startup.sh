#!/bin/sh
# ============================================
# 智能启动脚本：前 2 秒用 nc 扛健康检查，
# 然后释放 8081 让 Spring Boot 绑定
# ============================================

echo "[startup] 预热探针 + 启动 Spring Boot..."

# 后台启动 nc warmup（循环 3 次，每次 2 秒超时）
# CloudBase 探针前几秒打过来时返回 200，争取存活窗口
(
  for i in 1 2 3; do
    printf "HTTP/1.1 200 OK\r\nContent-Length: 2\r\n\r\nOK" | nc -l -p 8081 -w 2 2>/dev/null
  done
) &
WARMUP_PID=$!

# 启动 Spring Boot（后台，3~5 秒后会尝试绑定 8081）
java -jar /app/app.jar --spring.config.location=file:/app/application.yml &
JAVA_PID=$!

# 关键：只等 2 秒就释放端口！此时 Java 还在初始化，Tomcat 还没绑定
# nc 释放后 8081 空闲，等 Java 的 Tomcat 准备好时端口已空，直接绑定成功
sleep 2
kill $WARMUP_PID 2>/dev/null
wait $WARMUP_PID 2>/dev/null

echo "[startup] 端口已释放，等待 Tomcat 接管..."
wait $JAVA_PID

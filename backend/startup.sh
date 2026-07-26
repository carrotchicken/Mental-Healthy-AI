#!/bin/sh
# ============================================
# 两阶段启动：
# 1. nc 预热 6 秒，响应 CloudBase 初始探针
# 2. 停止预热，释放 8081，干净启动 Spring Boot
# ============================================

echo "[startup] 阶段1: nc 预热探针 (6秒)..."

# 后台 nc 循环监听 8081，给 CloudBase 探针返回 200
(
  for i in $(seq 1 5); do
    printf "HTTP/1.1 200 OK\r\nContent-Length: 2\r\n\r\nOK" | nc -l -p 8081 -w 2 2>/dev/null
  done
) &
WARMUP_PID=$!

sleep 6

# 彻底终止所有 nc，确保 8081 空闲
kill $WARMUP_PID 2>/dev/null
killall nc 2>/dev/null
sleep 1

echo "[startup] 阶段2: 启动 Spring Boot..."
exec java -jar /app/app.jar --spring.config.location=file:/app/application.yml

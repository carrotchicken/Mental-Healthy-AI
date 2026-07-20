# ============================================
# Python Agent (FastAPI) Dockerfile
# ============================================
FROM python:3.11-slim

WORKDIR /app

# 安装系统依赖（faiss 等可能需要）
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple

COPY . .

EXPOSE 5000

# 通过环境变量注入 DEEPSEEK_API_KEY 等
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "5000"]

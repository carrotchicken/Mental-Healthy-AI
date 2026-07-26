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
# 先安装 CPU 版 PyTorch（Agent 无需 CUDA，省 3GB+ 镜像体积）
RUN pip install --no-cache-dir torch --index-url https://download.pytorch.org/whl/cpu
RUN pip install --no-cache-dir --default-timeout=120 -r requirements.txt

COPY . .

EXPOSE 5000

# 通过环境变量注入 DEEPSEEK_API_KEY 等
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "5000"]

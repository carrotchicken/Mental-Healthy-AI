# ============================================
# Python Agent (FastAPI) Dockerfile
# ============================================
FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 5000

# 通过环境变量注入 DEEPSEEK_API_KEY 等
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "5000"]

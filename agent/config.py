import os
from pathlib import Path
from dotenv import load_dotenv

# 始终加载 agent 目录下的 .env，不受运行目录影响
load_dotenv(Path(__file__).parent / ".env")

DEEPSEEK_API_KEY = os.getenv("DEEPSEEK_API_KEY", "")
DEEPSEEK_BASE_URL = os.getenv("DEEPSEEK_BASE_URL", "https://api.deepseek.com")
DEEPSEEK_MODEL = os.getenv("DEEPSEEK_MODEL", "deepseek-chat")

AGENT_HOST = "0.0.0.0"
AGENT_PORT = 5000
JAVA_BACKEND_URL = "http://localhost:8081"

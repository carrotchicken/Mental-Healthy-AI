"""
AI Mental Health Assistant - Agent Service
FastAPI application with JSON Lines streaming for internal communication.
RAG: FAISS + Sentence-Transformers with persistent index on disk.
"""

import json
import logging
from typing import List, Optional
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException
from fastapi.responses import StreamingResponse
from pydantic import BaseModel

import config
from services.agent_service import agent_chat_service
from services.emotion_service import emotion_service
from services.diary_service import diary_service
from services.rag_service import rag_service

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


# ========== 启动时加载 FAISS 索引 ==========
@asynccontextmanager
async def lifespan(app: FastAPI):
    """启动时尝试从磁盘加载已有索引"""
    logger.info("正在加载 FAISS 索引...")
    loaded = rag_service.load()
    if loaded:
        logger.info(f"FAISS 索引加载成功: {rag_service.total_vectors} 个向量, {rag_service.total_articles} 篇文章")
    else:
        logger.info("未找到已有索引，等待 Spring Boot 推送知识库数据进行重建")
    yield


app = FastAPI(title="AI Mental Health Agent", version="1.0.0", lifespan=lifespan)


class ChatMessage(BaseModel):
    senderType: int
    content: str

class ChatRequest(BaseModel):
    sessionId: str
    messages: List[ChatMessage] = []
    userMessage: str

class EmotionRequest(BaseModel):
    messages: List[ChatMessage]

class DiaryRequest(BaseModel):
    dominantEmotion: str = ""
    moodScore: Optional[int] = None
    emotionTriggers: str = ""
    diaryContent: str = ""
    sleepQuality: Optional[int] = None
    stressLevel: Optional[int] = None


# ========== RAG 知识库文章模型 ==========
class RAGArticle(BaseModel):
    id: int
    title: str
    content: str

class RAGRebuildRequest(BaseModel):
    articles: List[RAGArticle]


@app.get("/api/agent/health")
async def health():
    return {"status": "ok", "service": "AI Mental Health Agent"}


@app.post("/api/agent/chat")
async def agent_chat(request: ChatRequest):
    messages_dict = [{"senderType": m.senderType, "content": m.content} for m in request.messages]

    async def generate_lines():
        full_response = ""
        try:
            async for chunk in agent_chat_service.stream_chat(messages_dict, request.userMessage):
                full_response += chunk
                line = json.dumps({
                    "code": "200",
                    "data": {"content": chunk}
                }, ensure_ascii=False)
                yield line + "\n"

            final = json.dumps({
                "code": "200",
                "data": {"content": "", "done": True, "fullResponse": full_response}
            }, ensure_ascii=False)
            yield final + "\n"

        except Exception as e:
            logger.error(f"Chat stream error: {e}")
            error_line = json.dumps({
                "code": "500",
                "data": {"content": f"[AI error: {str(e)}]", "done": True, "fullResponse": ""}
            }, ensure_ascii=False)
            yield error_line + "\n"

    return StreamingResponse(
        generate_lines(),
        media_type="application/x-ndjson",
        headers={"Cache-Control": "no-cache", "X-Accel-Buffering": "no"}
    )


@app.post("/api/agent/emotion/analyze")
async def agent_emotion_analyze(request: EmotionRequest):
    messages_dict = [{"senderType": m.senderType, "content": m.content} for m in request.messages]
    try:
        result = await emotion_service.analyze_emotion(messages_dict)
        return {"code": "200", "data": result}
    except Exception as e:
        logger.error(f"Emotion analysis error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/agent/diary/analyze")
async def agent_diary_analyze(request: DiaryRequest):
    diary_dict = request.model_dump()
    try:
        analysis = await diary_service.analyze_diary(diary_dict)
        return {"code": "200", "data": {"analysis": analysis}}
    except Exception as e:
        logger.error(f"Diary analysis error: {e}")
        raise HTTPException(status_code=500, detail=str(e))


# ========== RAG 索引管理端点 ==========

@app.post("/api/agent/rag/rebuild")
async def rag_rebuild(request: RAGRebuildRequest):
    """全量重建 FAISS 索引。Spring Boot 在知识库文章增删改后调用。"""
    articles = [a.model_dump() for a in request.articles]
    try:
        total = rag_service.rebuild_index(articles)
        logger.info(f"RAG 索引重建完成: {total} 个向量")
        return {"code": "200", "data": {"total_vectors": total, "total_articles": len(articles)}}
    except Exception as e:
        logger.error(f"RAG 索引重建失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/agent/rag/article/add")
async def rag_article_add(article: RAGArticle):
    """增量添加单篇文章到索引"""
    try:
        rag_service.add_article(article.id, article.title, article.content)
        return {"code": "200", "data": rag_service.get_stats()}
    except Exception as e:
        logger.error(f"RAG 添加文章失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.delete("/api/agent/rag/article/{article_id}")
async def rag_article_remove(article_id: int):
    """按 ID 删除文章的所有分块"""
    try:
        rag_service.remove_article(article_id)
        return {"code": "200", "data": rag_service.get_stats()}
    except Exception as e:
        logger.error(f"RAG 删除文章失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.put("/api/agent/rag/article/{article_id}")
async def rag_article_update(article_id: int, article: RAGArticle):
    """更新单篇文章（先删后加）"""
    try:
        rag_service.update_article(article_id, article.title, article.content)
        return {"code": "200", "data": rag_service.get_stats()}
    except Exception as e:
        logger.error(f"RAG 更新文章失败: {e}")
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/agent/rag/stats")
async def rag_stats():
    """查询索引状态"""
    return {"code": "200", "data": rag_service.get_stats()}


@app.post("/api/agent/rag/search")
async def rag_search(query: str, top_k: int = 3):
    """直接语义检索（调试用，生产由 function calling 调用）"""
    results = rag_service.search(query, top_k)
    return {"code": "200", "data": results}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app:app", host=config.AGENT_HOST, port=config.AGENT_PORT, reload=True, log_level="info")

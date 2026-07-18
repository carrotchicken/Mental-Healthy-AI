"""
RAG 服务 —— 管理 FAISS 向量索引的完整生命周期。

核心职责：
1. 文本分块：滑动窗口（512 字符 / 128 字符重叠）
2. 向量化：Sentence-Transformers (paraphrase-multilingual-MiniLM-L12-v2, 384 维）
3. 索引管理：FAISS IndexIDMap(IndexFlatIP)，支持按文章 ID 增删
4. 持久化：写入磁盘 / 启动时加载
5. 语义检索：Top-K 相似片段查询
"""

import os
import json
import logging
from typing import List, Dict, Optional
from dataclasses import dataclass

import numpy as np
import faiss
from sentence_transformers import SentenceTransformer

logger = logging.getLogger(__name__)

# 数据目录：存放 FAISS 索引文件和文档元数据
DATA_DIR = os.path.join(os.path.dirname(os.path.dirname(__file__)), "data")
INDEX_PATH = os.path.join(DATA_DIR, "faiss.index")
META_PATH = os.path.join(DATA_DIR, "chunks_meta.json")


@dataclass
class TextChunk:
    """文本分块"""
    chunk_id: int          # 分块唯一 ID
    article_id: int        # 来源文章 ID
    article_title: str     # 来源文章标题
    content: str           # 分块文本内容
    char_start: int        # 在原文章中的起始字符位置
    char_end: int          # 在原文章中的结束字符位置


class RAGService:
    """RAG 服务：封装 FAISS 索引的创建、加载、检索和更新"""

    # 分块参数
    CHUNK_SIZE = 512       # 每个块最多 512 个字符
    CHUNK_OVERLAP = 128    # 相邻块之间重叠 128 个字符（25%）

    # 检索参数
    DEFAULT_TOP_K = 3

    def __init__(self, model_name: str = "paraphrase-multilingual-MiniLM-L12-v2"):
        self.model_name = model_name
        self.model: Optional[SentenceTransformer] = None
        self.index: Optional[faiss.Index] = None
        self.chunks: List[TextChunk] = []
        self._next_chunk_id = 0

        os.makedirs(DATA_DIR, exist_ok=True)

    # ========== 模型懒加载（避免启动时卡住） ==========

    def _ensure_model(self):
        if self.model is None:
            logger.info(f"加载 Sentence-Transformers 模型: {self.model_name}")
            self.model = SentenceTransformer(self.model_name)
            logger.info("模型加载完成")

    # ========== 文本分块 ==========

    def _split_text(self, text: str, article_id: int, article_title: str) -> List[TextChunk]:
        """
        滑动窗口分块。
        窗口大小 512 字符，步长 = 512 - 128 = 384 字符（即重叠 128）。
        """
        chunks = []
        text_len = len(text)
        if text_len == 0:
            return chunks

        start = 0
        while start < text_len:
            end = min(start + self.CHUNK_SIZE, text_len)
            chunk_text = text[start:end]
            chunk = TextChunk(
                chunk_id=self._next_chunk_id,
                article_id=article_id,
                article_title=article_title,
                content=chunk_text,
                char_start=start,
                char_end=end,
            )
            chunks.append(chunk)
            self._next_chunk_id += 1

            if end >= text_len:
                break
            # 步长 = CHUNK_SIZE - CHUNK_OVERLAP
            start += (self.CHUNK_SIZE - self.CHUNK_OVERLAP)

        return chunks

    # ========== 索引构建 ==========

    def rebuild_index(self, articles: List[Dict]) -> int:
        """
        全量重建 FAISS 索引。

        参数 articles: [{"id": 1, "title": "文章标题", "content": "文章正文"}, ...]
        返回：索引中的总块数
        """
        self._ensure_model()

        # 1. 分块
        all_chunks: List[TextChunk] = []
        self._next_chunk_id = 0
        for article in articles:
            chunks = self._split_text(
                text=article.get("content", ""),
                article_id=article.get("id", 0),
                article_title=article.get("title", ""),
            )
            all_chunks.extend(chunks)

        logger.info(f"分块完成：{len(articles)} 篇文章 → {len(all_chunks)} 个文本块")

        if not all_chunks:
            logger.warning("没有可索引的内容，创建空索引")
            self.chunks = []
            self.index = None
            return 0

        # 2. 向量化
        texts = [c.content for c in all_chunks]
        embeddings = self.model.encode(texts, show_progress_bar=True, normalize_embeddings=True)
        embeddings = np.array(embeddings).astype('float32')

        # 3. 创建 FAISS 索引
        # IndexIDMap 包装 IndexFlatIP，支持自定义 ID 和按 ID 删除
        dim = embeddings.shape[1]  # 384
        base_index = faiss.IndexFlatIP(dim)  # 内积相似度（归一化后等价于余弦相似度）
        self.index = faiss.IndexIDMap(base_index)

        chunk_ids = np.array([c.chunk_id for c in all_chunks], dtype='int64')
        self.index.add_with_ids(embeddings, chunk_ids)
        self.chunks = all_chunks

        # 4. 持久化到磁盘
        self._save()

        logger.info(f"索引构建完成：{self.index.ntotal} 个向量, 维度 {dim}")
        return self.index.ntotal

    # ========== 增量操作 ==========

    def add_article(self, article_id: int, title: str, content: str):
        """新增单篇文章到索引"""
        if self.index is None:
            logger.warning("索引未初始化，无法增量添加")
            return

        self._ensure_model()
        chunks = self._split_text(content, article_id, title)
        if not chunks:
            return

        texts = [c.content for c in chunks]
        embeddings = self.model.encode(texts, normalize_embeddings=True)
        embeddings = np.array(embeddings).astype('float32')
        chunk_ids = np.array([c.chunk_id for c in chunks], dtype='int64')

        self.index.add_with_ids(embeddings, chunk_ids)
        self.chunks.extend(chunks)
        self._save()
        logger.info(f"文章 {article_id} 添加完成：+{len(chunks)} 块，索引总量 {self.index.ntotal}")

    def remove_article(self, article_id: int):
        """按文章 ID 删除所有相关分块"""
        if self.index is None:
            return

        # 找到属于该文章的所有 chunk_id
        ids_to_remove = [
            c.chunk_id for c in self.chunks if c.article_id == article_id
        ]
        if not ids_to_remove:
            return

        id_array = np.array(ids_to_remove, dtype='int64')
        self.index.remove_ids(id_array)

        # 更新内存中的 chunks 列表
        self.chunks = [c for c in self.chunks if c.article_id != article_id]
        self._save()
        logger.info(f"文章 {article_id} 删除完成：-{len(ids_to_remove)} 块，索引总量 {self.index.ntotal}")

    def update_article(self, article_id: int, title: str, content: str):
        """更新文章：先删旧块，再加新块"""
        self.remove_article(article_id)
        self.add_article(article_id, title, content)

    # ========== 语义检索 ==========

    def search(self, query: str, top_k: int = DEFAULT_TOP_K) -> List[Dict]:
        """
        语义检索。
        返回 Top-K 相关片段，每个元素格式：
        {"title": "文章标题", "snippet": "片段文本", "score": 0.85}
        """
        if self.index is None or self.index.ntotal == 0:
            logger.warning("索引为空，返回空结果")
            return []

        self._ensure_model()

        # 查询向量化
        query_vec = self.model.encode([query], normalize_embeddings=True)
        query_vec = np.array(query_vec).astype('float32')

        # FAISS 搜索
        distances, indices = self.index.search(query_vec, min(top_k, self.index.ntotal))

        results = []
        for dist, idx in zip(distances[0], indices[0]):
            if idx < 0 or idx >= len(self.chunks):
                continue
            chunk = self.chunks[idx]
            results.append({
                "title": chunk.article_title,
                "snippet": chunk.content,
                "score": round(float(dist), 4),
            })

        return results

    # ========== 持久化 ==========

    def _save(self):
        """保存索引到磁盘"""
        if self.index is not None:
            faiss.write_index(self.index, INDEX_PATH)
            logger.info(f"FAISS 索引已保存: {INDEX_PATH} ({self.index.ntotal} 条)")

        # 保存分块元数据
        meta = [{
            "chunk_id": c.chunk_id,
            "article_id": c.article_id,
            "article_title": c.article_title,
            "content": c.content,
            "char_start": c.char_start,
            "char_end": c.char_end,
        } for c in self.chunks]
        with open(META_PATH, "w", encoding="utf-8") as f:
            json.dump({"next_chunk_id": self._next_chunk_id, "chunks": meta}, f, ensure_ascii=False, indent=2)
        logger.info(f"元数据已保存: {META_PATH} ({len(meta)} 条)")

    def load(self) -> bool:
        """从磁盘加载索引。成功返回 True，失败或无文件返回 False。"""
        if not os.path.exists(INDEX_PATH) or not os.path.exists(META_PATH):
            logger.info("未找到持久化索引文件，将从空索引开始")
            return False

        try:
            self._ensure_model()

            # 加载 FAISS 索引
            self.index = faiss.read_index(INDEX_PATH)
            logger.info(f"FAISS 索引已加载: {INDEX_PATH} ({self.index.ntotal} 条)")

            # 加载分块元数据
            with open(META_PATH, "r", encoding="utf-8") as f:
                data = json.load(f)
            self._next_chunk_id = data.get("next_chunk_id", 0)
            self.chunks = [
                TextChunk(
                    chunk_id=m["chunk_id"],
                    article_id=m["article_id"],
                    article_title=m["article_title"],
                    content=m["content"],
                    char_start=m["char_start"],
                    char_end=m["char_end"],
                )
                for m in data["chunks"]
            ]
            logger.info(f"元数据已加载: {META_PATH} ({len(self.chunks)} 条)")
            return True
        except Exception as e:
            logger.error(f"加载索引失败: {e}")
            self.index = None
            self.chunks = []
            return False

    # ========== 状态查询 ==========

    @property
    def is_ready(self) -> bool:
        return self.index is not None and self.index.ntotal > 0

    @property
    def total_vectors(self) -> int:
        return self.index.ntotal if self.index else 0

    @property
    def total_articles(self) -> int:
        article_ids = set(c.article_id for c in self.chunks)
        return len(article_ids)

    def get_stats(self) -> Dict:
        return {
            "ready": self.is_ready,
            "total_vectors": self.total_vectors,
            "total_chunks": len(self.chunks),
            "total_articles": self.total_articles,
            "model": self.model_name,
            "index_path": INDEX_PATH,
        }


# 全局单例
rag_service = RAGService()

from langchain_openai import ChatOpenAI
from langchain_core.messages import HumanMessage, SystemMessage

import config
from prompts.diary_prompt import DIARY_SYSTEM_PROMPT, DIARY_USER_TEMPLATE


class DiaryService:
    """Analyzes emotion diary entries using DeepSeek LLM."""

    def __init__(self):
        self.llm = ChatOpenAI(
            model=config.DEEPSEEK_MODEL,
            api_key=config.DEEPSEEK_API_KEY,
            base_url=config.DEEPSEEK_BASE_URL,
            temperature=0.6,
            max_tokens=512,
        )

    async def analyze_diary(self, diary_data: dict) -> str:
        """
        Analyze a diary entry and return AI-generated insights.
        Returns a plain text analysis string.
        """
        user_prompt = DIARY_USER_TEMPLATE.format(
            dominant_emotion=diary_data.get("dominantEmotion", "未指定"),
            mood_score=str(diary_data.get("moodScore", 5)),
            emotion_triggers=diary_data.get("emotionTriggers", "未指定"),
            diary_content=diary_data.get("diaryContent", ""),
            sleep_quality=str(diary_data.get("sleepQuality", "未记录")),
            stress_level=str(diary_data.get("stressLevel", "未记录")),
        )

        messages = [
            SystemMessage(content=DIARY_SYSTEM_PROMPT),
            HumanMessage(content=user_prompt),
        ]

        try:
            response = await self.llm.ainvoke(messages)
            return response.content.strip() if response.content else ""
        except Exception as e:
            return f"[AI分析生成失败: {str(e)}]"


# Singleton
diary_service = DiaryService()

import json
import re
from typing import Dict, List

from langchain_openai import ChatOpenAI
from langchain_core.messages import HumanMessage, SystemMessage

import config
from prompts.emotion_prompt import EMOTION_SYSTEM_PROMPT, EMOTION_USER_TEMPLATE


class EmotionService:
    """Analyzes conversation emotion using DeepSeek LLM."""

    def __init__(self):
        self.llm = ChatOpenAI(
            model=config.DEEPSEEK_MODEL,
            api_key=config.DEEPSEEK_API_KEY,
            base_url=config.DEEPSEEK_BASE_URL,
            temperature=0.3,
            max_tokens=512,
        )

    def _format_conversation(self, messages: List[Dict]) -> str:
        """Format messages into a readable conversation string."""
        lines = []
        for msg in messages:
            sender_type = msg.get("senderType", 1)
            content = msg.get("content", "")
            role = "用户" if sender_type == 1 else "AI助手"
            lines.append(f"{role}: {content}")
        return "\n".join(lines)

    def _parse_json_response(self, text: str) -> Dict:
        """Parse JSON from LLM response, handling markdown code blocks."""
        # Remove markdown code block markers if present
        text = text.strip()
        if text.startswith("```"):
            text = re.sub(r"^```(?:json)?\s*", "", text)
            text = re.sub(r"\s*```$", "", text)

        try:
            return json.loads(text)
        except json.JSONDecodeError:
            # Fallback: return a default analysis
            return {
                "primaryEmotion": "平静",
                "emotionScore": 50,
                "riskLevel": 0,
                "riskDescription": "当前情绪状态正常",
                "isNegative": False,
                "suggestion": "保持积极的心态",
                "improvementSuggestions": [
                    "保持规律作息",
                    "适当运动",
                    "与朋友交流"
                ]
            }

    async def analyze_emotion(self, messages: List[Dict]) -> Dict:
        """
        Analyze the emotional state based on conversation messages.
        Returns a dict matching the frontend EmotionAnalysis type.
        """
        conversation = self._format_conversation(messages)
        user_prompt = EMOTION_USER_TEMPLATE.format(conversation=conversation)

        llm_messages = [
            SystemMessage(content=EMOTION_SYSTEM_PROMPT),
            HumanMessage(content=user_prompt),
        ]

        try:
            response = await self.llm.ainvoke(llm_messages)
            result = self._parse_json_response(response.content)
            return result
        except Exception as e:
            return {
                "primaryEmotion": "平静",
                "emotionScore": 50,
                "riskLevel": 0,
                "riskDescription": f"分析暂不可用: {str(e)}",
                "isNegative": False,
                "suggestion": "请稍后再试",
                "improvementSuggestions": []
            }


# Singleton
emotion_service = EmotionService()

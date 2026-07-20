package com.amha.agent;

import com.amha.entity.ConsultationMessage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.*;

/**
 * Mock实现：使用模板回复模拟AI对话。
 * 后续替换为真实LLM Agent服务时，只需实现ChatAgent接口即可。
 */
@Component
public class MockChatAgent implements ChatAgent {

    private static final List<String> GREETING_RESPONSES = List.of(
            "你好！很高兴能和你聊聊。",
            "我在这里，随时倾听你的分享。",
            "感谢你的信任，请告诉我你最近的情况。"
    );

    private static final List<String> GENERAL_RESPONSES = List.of(
            "我能理解你的感受。有时候说出自己的想法本身就是一种疗愈。",
            "感谢你的分享。你提到的这些确实值得关注，我们可以一起分析一下。",
            "每个人都会遇到困难的时候，重要的是你愿意面对它。",
            "你的觉察力很强，能意识到这一点本身就是进步。",
            "让我们试着换个角度来看这个问题，也许会有新的发现。"
    );

    @Override
    public Flux<String> streamResponse(Long sessionId, String userMessage,
                                       List<ConsultationMessage> history) {
        String response;
        if (history == null || history.size() <= 1) {
            response = GREETING_RESPONSES.get(new Random().nextInt(GREETING_RESPONSES.size()));
        } else {
            response = buildContextAwareResponse(userMessage);
        }

        // 模拟逐字流式输出，每个字符间隔30ms
        return Flux.fromStream(response.chars().mapToObj(c -> String.valueOf((char) c)))
                .delayElements(Duration.ofMillis(30));
    }

    @Override
    public Map<String, Object> analyzeEmotion(Long sessionId,
                                               List<ConsultationMessage> messages) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("primaryEmotion", "平静");
        result.put("emotionScore", 60);
        result.put("riskLevel", 0);
        result.put("riskDescription", "当前情绪状态稳定，未检测到明显风险信号");
        result.put("isNegative", false);
        result.put("suggestion", "保持积极心态，适当运动和社交有助于维持良好的情绪状态");
        result.put("improvementSuggestions", List.of(
                "每天花10分钟进行正念冥想",
                "保持规律的作息时间",
                "与朋友或家人保持联系"
        ));
        return result;
    }

    private String buildContextAwareResponse(String userMessage) {
        if (userMessage.contains("焦虑") || userMessage.contains("紧张") || userMessage.contains("担心")) {
            return "焦虑是我们在面对不确定性时的正常反应。我建议你可以尝试深呼吸练习：吸气4秒，屏住4秒，然后缓慢呼气6秒。同时，试着把让你焦虑的事情写下来，这能帮助你理清思路。需要我陪你一起分析具体的困扰吗？";
        }
        if (userMessage.contains("压力") || userMessage.contains("累") || userMessage.contains("忙")) {
            return "听起来你最近承受了不少压力。适度的压力可以推动我们前进，但过度压力需要及时调节。建议你尝试'番茄工作法'：专注25分钟后休息5分钟。记得给自己留出放松的时间，这不是浪费时间，而是在为自己充电。";
        }
        if (userMessage.contains("失眠") || userMessage.contains("睡觉") || userMessage.contains("睡眠")) {
            return "睡眠问题确实会影响我们的整体状态。建议从建立规律的睡眠习惯开始：固定入睡和起床时间，睡前1小时远离电子屏幕。你也可以试试'4-7-8呼吸法'帮助放松入睡。如果持续困扰，建议咨询专业医生。";
        }
        return GENERAL_RESPONSES.get(new Random().nextInt(GENERAL_RESPONSES.size()));
    }
}

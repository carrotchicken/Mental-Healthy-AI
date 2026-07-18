"""
RAG 种子脚本 —— 用示例知识库文章构建 FAISS 索引

用法：
    python -m scripts.seed_rag

或直接调用 rebuild 端点：
    curl -X POST http://localhost:5000/api/agent/rag/rebuild \
      -H "Content-Type: application/json" \
      -d '{"articles": [...]}'
"""

import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from services.rag_service import rag_service

SAMPLE_ARTICLES = [
    {
        "id": 1,
        "title": "广泛性焦虑障碍的认知行为治疗",
        "content": (
            "广泛性焦虑障碍（GAD）是一种常见的心理健康问题，表现为持续、过度的担忧和紧张。"
            "认知行为治疗（CBT）是 GAD 的循证一线治疗方法。"
            "CBT 治疗 GAD 包括以下几个核心模块：\n\n"
            "1. 认知重构：识别并挑战灾难化思维。患者常将日常事件夸大理解为威胁，治疗师引导患者用更"
            "平衡的方式重新评估情境，用证据检验担忧的合理性。\n\n"
            "2. 渐进式肌肉放松训练：通过系统地紧张和放松身体各肌群，降低生理唤醒水平。每日练习 10-15 分钟"
            "可显著降低肌肉紧张度。\n\n"
            "3. 暴露疗法：逐步面对被回避的情境或刺激。从不引起严重焦虑的低等级情境开始，逐级进阶。\n\n"
            "研究表明 CBT 对 GAD 的有效率达 60-70%。治疗通常持续 12-20 周。"
        )
    },
    {
        "id": 2,
        "title": "睡眠卫生与失眠的非药物干预",
        "content": (
            "失眠是现代社会最常见的睡眠障碍，约 30% 的成年人有失眠症状。"
            "失眠的认知行为治疗（CBT-I）是非药物干预的首选方案。\n\n"
            "1. 刺激控制：床只用来睡觉，不在床上玩手机或工作。如果 20 分钟内无法入睡，起床到另一个房间"
            "做放松活动，困了再回床。\n\n"
            "2. 睡眠限制：计算平均实际睡眠时间，将卧床时间限制在该时长加 30 分钟。\n\n"
            "3. 认知重构：纠正'今晚肯定睡不着'等灾难化信念。6-7 小时对大多数人已足够。\n\n"
            "4. 睡眠卫生：睡前 1 小时避免电子屏幕蓝光；卧室温度 18-22°C；下午 2 点后不摄入咖啡因。\n\n"
            "CBT-I 的有效率达 70-80%，优于安眠药且无副作用和依赖性。"
        )
    },
    {
        "id": 3,
        "title": "正念减压 MBSR 入门指南",
        "content": (
            "正念减压（MBSR）由 Jon Kabat-Zinn 博士于 1979 年创立。\n\n"
            "核心练习：\n"
            "1. 身体扫描：从脚趾开始依次注意身体各部位的感觉，不加评判地观察。\n"
            "2. 正念呼吸：将注意力锚定在呼吸的自然节律上，注意力游走时温柔地带回。\n"
            "3. 正念行走：感受脚底与地面的接触、身体重心的转移。\n\n"
            "科学研究表明，连续 8 周每天练习 10-15 分钟，焦虑评分可降低 30-40%，"
            "皮质醇水平下降，前额叶皮层活动增强。"
        )
    },
    {
        "id": 4,
        "title": "抑郁症的识别与自助策略",
        "content": (
            "抑郁症影响约 3.8% 的世界人口。核心特征是持续至少两周的情绪低落和兴趣丧失。\n\n"
            "自助策略：\n"
            "1. 行为激活：即使没有动力，也按计划执行曾让你有成就感的活动。从极小步骤开始。\n"
            "2. 社交连接：每天至少联系一个人，哪怕只是一条消息。\n"
            "3. 规律生活节奏：固定起床、用餐、运动和就寝时间。\n"
            "4. 体育锻炼：每周 150 分钟中等强度运动，效果与轻度抗抑郁药相当。\n\n"
            "重要：如症状持续超过两周且影响日常功能，务必寻求专业帮助。"
        )
    },
    {
        "id": 5,
        "title": "社交焦虑的认知行为模型与应对",
        "content": (
            "社交焦虑障碍不只是'害羞'，而是对社交情境的强烈恐惧，担心被负面评价。\n\n"
            "CBT 应对策略：\n"
            "- 注意力训练：将注意力从内部感受转向外部对话内容\n"
            "- 行为实验：检验'如果我表现得自然，别人会嘲笑我'等信念\n"
            "- 逐步暴露：从低恐惧情境到高恐惧情境渐进练习\n"
            "- 放弃安全行为：在暴露练习中不使用安全行为，体验焦虑自然消退"
        )
    },
]

if __name__ == "__main__":
    print(f"正在用 {len(SAMPLE_ARTICLES)} 篇示例文章构建 RAG 索引...")
    total = rag_service.rebuild_index(SAMPLE_ARTICLES)
    stats = rag_service.get_stats()
    print(f"\n索引构建完成:")
    print(f"  总向量数: {stats['total_vectors']}")
    print(f"  总块数:   {stats['total_chunks']}")
    print(f"  总文章数: {stats['total_articles']}")
    print(f"  模型:     {stats['model']}")
    print(f"  索引路径: {stats['index_path']}")
    print()

    # 测试检索
    tests = ["如何缓解焦虑", "失眠了怎么办", "正念练习有什么好处"]
    for q in tests:
        print(f"查询: {q}")
        results = rag_service.search(q, top_k=2)
        for r in results:
            print(f"  [{r['score']:.3f}] {r['title']}: {r['snippet'][:60]}...")
        print()

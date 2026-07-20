package com.amha.config;

import com.amha.entity.*;
import com.amha.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final KnowledgeCategoryMapper knowledgeCategoryMapper;
    private final KnowledgeArticleMapper knowledgeArticleMapper;
    private final ConsultationSessionMapper consultationSessionMapper;
    private final ConsultationMessageMapper consultationMessageMapper;
    private final EmotionDiaryMapper emotionDiaryMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        if (hasData()) {
            log.info("数据已存在，跳过初始化");
            return;
        }
        log.info("开始初始化数据...");
        initUsers();
        initCategories();
        initArticles();
        initDiaries();
        initConsultations();
        log.info("数据初始化完成");
    }

    private boolean hasData() {
        return userMapper.selectCount(new LambdaQueryWrapper<>()) > 0;
    }

    private void initUsers() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setEmail("admin@amha.com");
        admin.setNickname("系统管理员");
        admin.setGender(0);
        admin.setUserType(2);
        admin.setStatus(1);
        userMapper.insert(admin);

        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("test123"));
        testUser.setEmail("test@amha.com");
        testUser.setNickname("测试用户");
        testUser.setGender(1);
        testUser.setUserType(1);
        testUser.setStatus(1);
        userMapper.insert(testUser);

        log.info("用户数据初始化完成");
    }

    private void initCategories() {
        String[][] categories = {
                {"情绪管理", "关于情绪识别、表达和调节的心理学知识", "1"},
                {"压力应对", "压力管理技巧与放松方法", "2"},
                {"人际关系", "人际沟通、亲密关系与社交技巧", "3"},
                {"自我成长", "自我认知、个人发展与心理健康", "4"},
                {"睡眠健康", "睡眠科学、失眠应对与睡眠卫生", "5"},
        };
        for (String[] c : categories) {
            KnowledgeCategory category = new KnowledgeCategory();
            category.setCategoryName(c[0]);
            category.setDescription(c[1]);
            category.setSortOrder(Integer.parseInt(c[2]));
            category.setStatus(1);
            knowledgeCategoryMapper.insert(category);
        }
        log.info("知识分类初始化完成");
    }

    private void initArticles() {
        insertArticle(1L, "如何识别和管理日常焦虑情绪",
                "<h2>什么是焦虑</h2><p>焦虑是人类面对压力时的正常反应，适度的焦虑有助于我们保持警觉。但当焦虑过度或持续时间过长时，就会影响日常生活。</p><h2>识别焦虑信号</h2><p>身体信号：心跳加速、出汗、肌肉紧张、呼吸急促。</p><p>心理信号：过度担忧、注意力不集中、易怒。</p><h2>管理焦虑的方法</h2><p>1. 深呼吸练习：4-7-8呼吸法<br>2. 正念冥想：每天10分钟<br>3. 认知重构：挑战负面想法</p>",
                "了解焦虑的成因与应对策略，学会在日常生活中有效管理焦虑情绪。", "焦虑,情绪管理,正念",
                1L, "系统管理员", "2026-06-01 10:00:00", 128);

        insertArticle(2L, "职场压力应对指南",
                "<h2>认识职场压力</h2><p>职场压力是现代人最常见的压力来源之一，适度的压力能提升工作效率，但过度压力会导致职业倦怠。</p><h2>实用的减压技巧</h2><p>1. 时间管理矩阵：区分重要/紧急<br>2. 番茄工作法：25分钟专注+5分钟休息<br>3. 倾诉与社交支持：不要独自承受</p>",
                "面对职场中的各种压力，学习科学有效的应对方法，保持工作与生活的平衡。", "压力,职场,时间管理",
                1L, "系统管理员", "2026-06-05 14:00:00", 95);

        insertArticle(3L, "建立健康的人际边界",
                "<h2>什么是人际边界</h2><p>人际边界是个人在与他人互动中设定的心理和情感界限。健康的人际边界是良好关系的基础。</p><h2>如何设立边界</h2><p>1. 明确表达需求：用「我」开头的句子<br>2. 学会说「不」：拒绝不等于伤害<br>3. 尊重自己的感受：你的需求同样重要</p>",
                "学习在人际关系中建立和维护健康的心理边界，保护自己的情感空间。", "人际关系,边界,自我关怀",
                1L, "系统管理员", "2026-06-10 09:00:00", 76);

        insertArticle(4L, "自我接纳：与自己和解的旅程",
                "<h2>什么是自我接纳</h2><p>自我接纳是指能够客观地认识和接受自己的全部，包括优点和缺点。这是心理健康的重要基石。</p><h2>培养自我接纳的方法</h2><p>1. 停止自我批评：用鼓励代替责备<br>2. 练习自我同情：像对待朋友一样对待自己<br>3. 记录成就日记：关注自己的进步</p>",
                "探索自我接纳的重要性，学习如何与不完美的自己和解，走向内心的平和。", "自我接纳,自我成长,心理健康",
                1L, "系统管理员", "2026-06-15 16:00:00", 210);

        insertArticle(5L, "改善睡眠质量的科学方法",
                "<h2>睡眠的重要性</h2><p>充足的睡眠是心理健康的基础，长期睡眠不足会增加焦虑和抑郁的风险。</p><h2>改善睡眠的实用技巧</h2><p>1. 建立规律的作息：固定入睡和起床时间<br>2. 创造舒适的睡眠环境：凉爽、黑暗、安静<br>3. 限制屏幕时间：睡前1小时远离电子设备<br>4. 放松训练：渐进式肌肉放松</p>",
                "了解睡眠与心理健康的密切关系，掌握科学有效的睡眠改善方法。", "睡眠,失眠,放松",
                1L, "系统管理员", "2026-06-20 11:00:00", 153);

        log.info("知识文章初始化完成");
    }

    private void insertArticle(Long categoryId, String title, String content, String summary,
                                String tags, Long authorId, String authorName, String publishedAt, int readCount) {
        KnowledgeArticle article = new KnowledgeArticle();
        article.setCategoryId(categoryId);
        article.setTitle(title);
        article.setContent(content);
        article.setSummary(summary);
        article.setTags(tags);
        article.setAuthorId(authorId);
        article.setAuthorName(authorName);
        article.setStatus(1);
        article.setPublishedAt(LocalDateTime.parse(publishedAt.replace(" ", "T")));
        article.setReadCount(readCount);
        knowledgeArticleMapper.insert(article);
    }

    private void initDiaries() {
        Long userId = 2L; // testuser
        insertDiary(userId, "2026-06-23", 7, "开心", "今天项目顺利上线，团队一起庆祝",
                "今天是个好日子，努力了两个月的项目终于成功上线了。同事们一起吃了顿饭，感觉团队的凝聚力更强了。", 4, 3);
        insertDiary(userId, "2026-06-22", 6, "平静", "正常的工作日，没有特别的事",
                "今天是比较普通的一天，按部就班地完成了工作任务。午休时看了一会儿书，感觉内心很宁静。", 4, 2);
        insertDiary(userId, "2026-06-21", 4, "焦虑", "明天有个重要的汇报，担心准备不充分",
                "一整天都在为明天的汇报做准备，总感觉自己准备得不够充分。手心一直在出汗，心跳也比平时快。", 2, 5);
        insertDiary(userId, "2026-06-20", 5, "疲惫", "连续加班三天，身体和精神都很疲惫",
                "这周工作量特别大，已经连续加班三天了。今天回到家已经完全不想动。", 3, 4);
        insertDiary(userId, "2026-06-19", 8, "高兴", "收到了期待已久的录取通知书",
                "太开心了！今天收到了理想学校的研究生录取通知书。父母知道后也很高兴。", 5, 2);
        insertDiary(userId, "2026-06-18", 3, "悲伤", "和好朋友发生了争执",
                "今天和最好的朋友因为一件小事吵了一架，说了很多伤人的话。现在回想起来感觉很后悔。", 3, 4);
        insertDiary(userId, "2026-06-17", 6, "困惑", "面对职业选择感到很迷茫",
                "最近一直在思考自己的职业发展方向，觉得自己现在的工作虽然稳定但缺乏挑战。", 4, 3);

        log.info("情绪日记初始化完成");
    }

    private void insertDiary(Long userId, String date, int moodScore, String emotion,
                              String triggers, String content, int sleepQuality, int stressLevel) {
        EmotionDiary diary = new EmotionDiary();
        diary.setUserId(userId);
        diary.setDiaryDate(LocalDate.parse(date));
        diary.setMoodScore(moodScore);
        diary.setDominantEmotion(emotion);
        diary.setEmotionTriggers(triggers);
        diary.setDiaryContent(content);
        diary.setSleepQuality(sleepQuality);
        diary.setStressLevel(stressLevel);
        diary.setHasAiEmotionAnalysis(1);
        diary.setAiAnalysisStatus("COMPLETED");
        diary.setAiEmotionAnalysis("情绪分析完成");
        emotionDiaryMapper.insert(diary);
    }

    private void initConsultations() {
        Long userId = 2L;

        // Session 1
        ConsultationSession session1 = new ConsultationSession();
        session1.setUserId(userId);
        session1.setSessionTitle("关于焦虑情绪的咨询");
        session1.setStatus("ACTIVE");
        session1.setStartedAt(LocalDateTime.parse("2026-06-23T14:00:00"));
        session1.setMessageCount(6);
        session1.setLastMessageContent("不用客气，有任何问题随时找我聊聊。");
        session1.setLastMessageTime(LocalDateTime.parse("2026-06-23T14:25:00"));
        consultationSessionMapper.insert(session1);

        insertMessage(session1.getId(), 1, "最近我总是感到很焦虑，特别是在工作压力大的时候，该怎么办？", "2026-06-23T14:00:00");
        insertMessage(session1.getId(), 2, "我能理解你的感受。焦虑是我们面对压力时的正常反应。首先，试着做一个深呼吸——吸气4秒，屏住4秒，然后缓慢呼气6秒。", "2026-06-23T14:02:00");
        insertMessage(session1.getId(), 1, "试了一下，确实感觉好一点了。但是这种焦虑总是反复出现。", "2026-06-23T14:10:00");
        insertMessage(session1.getId(), 2, "焦虑反复出现是很常见的。我们可以一起分析一下：你的焦虑通常在什么情况下出现？记录下这些触发因素有助于找到应对策略。", "2026-06-23T14:15:00");
        insertMessage(session1.getId(), 1, "主要是在面对截止日期和重要会议的时候。我会担心自己做不好。", "2026-06-23T14:20:00");
        insertMessage(session1.getId(), 2, "这其实是'预期焦虑'的表现。你可以尝试把大任务分解成小步骤，每完成一步就给自己一个肯定。同时接纳'不完美'——没有人能做到100分。", "2026-06-23T14:25:00");

        // Session 2
        ConsultationSession session2 = new ConsultationSession();
        session2.setUserId(userId);
        session2.setSessionTitle("睡眠问题咨询");
        session2.setStatus("ACTIVE");
        session2.setStartedAt(LocalDateTime.parse("2026-06-22T21:00:00"));
        session2.setMessageCount(4);
        session2.setLastMessageContent("试着今晚就开始练习呼吸放松法吧。");
        session2.setLastMessageTime(LocalDateTime.parse("2026-06-22T21:18:00"));
        consultationSessionMapper.insert(session2);

        insertMessage(session2.getId(), 1, "我最近睡眠质量很差，躺床上要很久才能睡着。", "2026-06-22T21:00:00");
        insertMessage(session2.getId(), 2, "睡眠问题确实让人困扰。我们可以从睡眠卫生开始改善：睡前1小时远离手机和电脑，保持卧室凉爽黑暗。", "2026-06-22T21:05:00");
        insertMessage(session2.getId(), 1, "我睡前总是忍不住刷手机，有时候一刷就是半小时。", "2026-06-22T21:10:00");
        insertMessage(session2.getId(), 2, "这是很多人共同的困扰。试着今晚就开始练习呼吸放松法吧。把手机放在另一个房间充电，用轻音乐替代刷手机。", "2026-06-22T21:18:00");

        log.info("咨询会话初始化完成");
    }

    private void insertMessage(Long sessionId, int senderType, String content, String createdAt) {
        ConsultationMessage msg = new ConsultationMessage();
        msg.setSessionId(sessionId);
        msg.setSenderType(senderType);
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.parse(createdAt));
        consultationMessageMapper.insert(msg);
    }
}

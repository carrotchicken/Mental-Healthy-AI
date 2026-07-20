// 注册提交数据
export interface RegisterFormQuery {
	username: string
	email: string
	nickname?: string
	phone?: string
	password: string
	confirmPassword: string
	gender: number
	userType: number
}

// 创建新会话响应数据
export interface StartSession {
	expiryTime: number
	initialMessage: string
	messageCount: number
	sessionId: string
	startTime: number
	status: string
	userHash: number
}

// 分页查询历史咨询会话数据
export interface HistorySession {
	id: number
	userId: number
	userNickname: string
	sessionTitle: string
	startedAt: string
	durationMinutes: number
	messageCount: number
	lastMessageContent: string
	lastMessageTime: string
}

// 会话消息数据(单条)
export interface Message {
	id: number
	sessionId: number
	senderType: number
	senderTypeDesc?: string
	messageType?: number
	messageTypeDesc?: string
	content: string
	createdAt: string
	contentLength?: number
	contentPreview?: string
	isError?: boolean
}

// 会话情绪分析响应数据
export interface EmotionAnalysis {
	primaryEmotion: string
	emotionScore: number
	riskLevel: number
	riskDescription: string
	isNegative: boolean
	suggestion: string
	improvementSuggestions: string[]
}

// 创建情绪日志提交参数
export interface EmotionDiaryQuery {
	diaryContent: string
	diaryDate: string
	dominantEmotion: string
	emotionTriggers: string
	moodScore: number
	sleepQuality: number | null
	stressLevel: number | null
}

// 情绪日记数据（含AI分析）
export interface EmotionDiaryVO {
	id: number
	userId: number
	username: string
	nickname: string
	diaryDate: string
	moodScore: number
	dominantEmotion: string
	emotionTriggers: string
	diaryContent: string
	diaryContentPreview: string
	sleepQuality: number
	stressLevel: number
	aiEmotionAnalysis: string
	aiAnalysisUpdatedAt: string
	hasAiEmotionAnalysis: boolean
	aiAnalysisStatus: string
	contentLength: number
	createdAt: string
	updatedAt: string
}

// 查询知识库文章列表提交参数
export interface KnowledgeArticleQuery {
	currentPage: number
	pageSize: number
	sortDirection: string
	sortField: string
}

// 查询知识库文章列表相应数据
export interface KnowledgeArticle {
	id: number
	categoryId: number
	categoryName: string
	title: string
	summary: string
	coverImage: string
	tags: string
	authorName: string
	readCount: number
	status: number
	statusText: string
	isFavorited: boolean
	favoriteCount: number
	publishedAt: string
	createdAt: string
	updatedAt: string
}

// 知识文章详情
export interface KnowledgeArticleDetail {
	id: string
	categoryId: number
	categoryName: string
	title: string
	summary: string
	content: string
	coverImage: string
	tags: string
	tagArray: string[]
	authorId: number
	authorName: string
	readCount: number
	status: number
	statusText: string
	isFavorited: false
	publishedAt: string
	createdAt: string
	updatedAt: string
}

// 列表响应数据
export interface ListData<T = HistorySession | KnowledgeArticle> {
	records: T[]
	current: number
	page: number
	size: number
	total: number
}

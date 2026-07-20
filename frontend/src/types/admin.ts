// 搜索表单子项配置
export interface FormItemConfig {
	comp: 'el-input' | 'el-select'
	prop: string
	label: string
	placeholder: string
	options?: { label: string; value: string | number }[]
	col?: { xs: number; sm: number; md: number; lg: number; xl: number }
}

// 登录提交参数
export interface LoginForm {
	username: string
	password: string
}

// 登录响应数据
export interface LoginResponse {
	roleType: string
	token: string
	userInfo: {
		id: number
		username: string
		email: string
		nickname: string
		phone: string
		gender: number
		genderDisplayName: string
		userType: number
		userTypeDisplayName: string
		status: number
		statusDisplayName: string
		displayName: string
		createdAt: string
		updatedAt: string
	}
}

// 文章分类查询参数
export interface SearchFormData {
	title?: string
	categoryId?: string
	status?: string
	authorName?: string
	currentPage?: number
	pageSize?: number
	userId?: string
	modelScoreRange?: string
}

// 文章分类响应数据
export interface KnowledgeArticleList {
	id: number
	categoryName: string
	description: string
	sortOrder: string
	status: string
	statusText: string
	articleCount: number
	createdAt: string
	updatedAt: string
}

// 文章新增/修改参数
export interface KnowledgeArticleData {
	title: string
	content: string
	coverImage: string
	categoryId: number
	summary: string
	tags: string
	id?: string
}

// 文章数据
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
	isFavorite: boolean
	favoriteCount: number
	publishedAt: string
	createdAt: string
	updatedAt: string
}

// 咨询会话查询参数
export interface ConsultationSearchParams {
	currentPage?: number
	size?: number
	total?: number
	emotionTag?: string
}

// 咨询会话数据
export interface ConsultationSessionItem {
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

// 咨询会话详情数据
export interface ConsultationMessage {
	content: string
	contentLength: number
	contentPreview: string
	createdAt: string
	id: number
	messageType: number
	messageTypeDesc: string
	senderType: number
	senderTypeDesc: string
	sessionId: number
}

// 情绪日志查询参数
export interface EmotionLogSearchParams {
	current?: string
	currentPage?: number
	dominantEmotion?: string
	emotionTag?: string
	maxMoodScore?: string
	minMoodScore?: string
	modelScoreRange?: string
	size?: number
	total?: number
	userId?: string
}

// 情绪日志数据
export interface EmotionLogItem {
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
	createdAt: string
	updatedAt: string
	aiEmotionAnalysis: string
	aiAnalysisUpdatedAt: string
	hasAiEmotionAnalysis: boolean
	aiAnalysisStatus: string
	contentLength: number
}

// 列表响应数据
export interface ListData<T = KnowledgeArticle | ConsultationSessionItem | EmotionLogItem> {
	records: T[]
	current: number
	page: number
	size: number
	total: number
}

// 综合数据分析响应数据
export interface DashboardData<T = any> {
	systemOverview: {
		totalUsers: number
		activeUsers: number
		totalDiaries: number
		totalSessions: number
		avgMoodScore: number
		todayNewUsers: number
		todayNewDiaries: number
		todayNewSessions: number
	}
	emotionHeatmap: {
		gridData: T[][]
		emotionDistribution: Record<string, number>
		peakEmotionTime: string
		dateRange: string
	}
	emotionTrend: {
		date: string
		avgMoodScore: number
		recordCount: number
		positiveRatio: number
		negativeRatio: number
		dominantEmotion:
			| '平静'
			| '开心'
			| '快乐'
			| '疲惫'
			| '焦虑'
			| '高兴'
			| '无数据'
	}[]
	consultationStats: {
		totalSessions: number
		avgDurationMinutes: number
		dailyTrend: { date: string; sessionCount: number; userCount: number }[]
	}
	userActivity: {
		date: string
		activeUsers: number
		newUsers: number
		diaryUsers: number
		consultationUsers: number
	}[]
}

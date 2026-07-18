import request from '@/utils/request'
import type {
	LoginForm,
	LoginResponse,
	SearchFormData,
	KnowledgeArticleList,
	KnowledgeArticleData,
	ConsultationSearchParams,
	ConsultationMessage,
	KnowledgeArticle,
	ConsultationSessionItem,
	EmotionLogItem,
	EmotionLogSearchParams,
	ListData,
	DashboardData,
} from '@/types/admin'

export function login(data: LoginForm): Promise<LoginResponse> {
	return request.post<LoginResponse>('/user/login', data)
}

export function getCategoryTree(): Promise<KnowledgeArticleList[]> {
	return request.get<KnowledgeArticleList[]>('/knowledge/category/tree')
}

export function getArticlePage(
	params: SearchFormData,
): Promise<ListData<KnowledgeArticle>> {
	return request.get('/knowledge/article/page', {
		params: {
			...params,
			currentPage: params.currentPage || 1,
			pageSize: params.pageSize || (params as any).size || 10,
		},
	})
}

export function uploadFile(
	file: File,
	businessId: string,
): Promise<{ filePath: string }> {
	const formData = new FormData()
	formData.append('file', file)
	formData.append('businessType', 'ARTICLE')
	formData.append('businessId', businessId)
	formData.append('businessField', 'cover')

	return request.post('/file/upload', formData, {
		headers: { 'Content-Type': 'multipart/form-data' },
	})
}

export function createArticle(data: KnowledgeArticleData): Promise<void> {
	return request.post('/knowledge/article', data)
}

export function getArticleById(id: number): Promise<KnowledgeArticleData> {
	return request.get(`/knowledge/article/${id}`)
}

export function updateArticle(
	id: string,
	data: KnowledgeArticleData,
): Promise<void> {
	return request.put(`/knowledge/article/${id}`, data)
}

export function changeArticleStatus(id: number, status: number): Promise<void> {
	return request.put(`/knowledge/article/${id}/status`, { status })
}

export function deleteArticle(id: number): Promise<void> {
	return request.delete(`/knowledge/article/${id}`)
}

export function getConsultationSessionPage(
	params: ConsultationSearchParams,
): Promise<ListData<ConsultationSessionItem>> {
	return request.get('/psychological-chat/sessions', {
		params: { pageNum: params.currentPage || 1, pageSize: params.size || 10 },
	})
}

export function getConsultationSessionDetail(
	sessionId: number,
): Promise<ConsultationMessage[]> {
	return request.get(`/psychological-chat/sessions/${sessionId}/messages`)
}

export function getEmotionlogPage(
	params: EmotionLogSearchParams,
): Promise<ListData<EmotionLogItem>> {
	const queryParams: Record<string, any> = {
		currentPage: params.currentPage || 1,
		size: params.size || 10,
	}
	if (params.userId) queryParams.userId = params.userId
	if (params.dominantEmotion) queryParams.dominantEmotion = params.dominantEmotion
	if (params.modelScoreRange) {
		const [min, max] = params.modelScoreRange.split('-')
		queryParams.minMoodScore = Number(min)
		queryParams.maxMoodScore = Number(max)
	}
	return request.get('/emotion-diary/admin/page', { params: queryParams })
}

export function deleteEmotionLog(id: number): Promise<void> {
	return request.delete(`/emotion-diary/admin/${id}`)
}

export function getDashboardData(): Promise<DashboardData> {
	return request.get('/data-analytics/overview')
}

export function logout(): Promise<void> {
	return request.post('/user/logout')
}

<template>
	<div class="emotionDiary-container">
		<div class="header-section">
			<div class="header-content">
				<el-image :src="iconUrl" style="width: 60px; height: 60px"></el-image>
				<h1>情绪日记</h1>
			</div>
		</div>

		<div class="content">
			<!-- 提交成功后的AI分析卡片 -->
			<div class="diary-card ai-analysis-card" v-if="showAiResult">
				<div class="title">🤖 AI情感分析</div>
				<div class="analysis-loading" v-if="aiResultLoading">
					<el-icon class="is-loading"><Loading /></el-icon>
					<span>AI正在分析你的情绪日记，请稍候...</span>
				</div>
				<div class="analysis-content" v-else-if="aiAnalysisText">
					<MarkdownRenderer :content="aiAnalysisText" :is-ai-message="true" />
				</div>
				<div class="analysis-error" v-else>
					<p>AI分析暂时不可用，请稍后再试。你的日记已经保存成功。</p>
				</div>
			</div>

			<!-- 日记填写表单 -->
			<div class="diary-card">
				<div class="title">今日情绪评分</div>
				<div class="section">
					<p>您今天的整体情绪状态如何？（1-10分）</p>
					<div class="rate">
						<el-rate
							v-model="diaryForm.moodScore"
							:text="emotionStatus"
							show-texts
							:max="10"
							size="large" />
					</div>
				</div>
			</div>

			<div class="diary-card">
				<div class="title">主要情绪</div>
				<div class="emotion-grid">
					<div
						class="emotion-card"
						v-for="emotion in emotionOptions"
						:key="emotion.name"
						@click="selectEmotion(emotion.name)"
						:class="{ selected: emotion.name === diaryForm.dominantEmotion }">
						<el-image :src="emotion.url" style="width: 50px; height: 50px" />
						<div class="emotion-name">{{ emotion.name }}</div>
					</div>
				</div>
			</div>

			<div class="diary-card">
				<div class="title">详细记录</div>
				<div class="detail-form">
					<div class="form-group">
						<div class="form-label">情绪触发因素</div>
						<el-input
							v-model="diaryForm.emotionTriggers"
							type="textarea"
							placeholder="今天什么事情触发了你的情绪？"
							:rows="3"
							max-length="1000"
							show-word-limit />
					</div>
					<div class="form-group">
						<div class="form-label">今日感想</div>
						<el-input
							v-model="diaryForm.diaryContent"
							type="textarea"
							placeholder="写下你今天的感受、想法或者任何想记录的内容..."
							:rows="5"
							max-length="2000"
							show-word-limit />
					</div>
					<div class="life-indicators">
						<div class="indicator-group">
							<div class="form-label">睡眠质量</div>
							<el-select v-model="diaryForm.sleepQuality" placeholder="请选择">
								<el-option label="很差" :value="1"></el-option>
								<el-option label="较差" :value="2"></el-option>
								<el-option label="一般" :value="3"></el-option>
								<el-option label="良好" :value="4"></el-option>
								<el-option label="优秀" :value="5"></el-option>
							</el-select>
						</div>
						<div class="indicator-group">
							<div class="form-label">压力水平</div>
							<el-select v-model="diaryForm.stressLevel" placeholder="请选择">
								<el-option label="很低" :value="1"></el-option>
								<el-option label="较低" :value="2"></el-option>
								<el-option label="中等" :value="3"></el-option>
								<el-option label="较高" :value="4"></el-option>
								<el-option label="很高" :value="5"></el-option>
							</el-select>
						</div>
					</div>
					<div class="action-buttons">
						<el-button @click="resetDiaryForm">重置</el-button>
						<el-button type="primary" @click="submitDiaryEntry" :loading="submitting">
							提交记录
						</el-button>
					</div>
				</div>
			</div>

			<!-- 历史日记列表 -->
			<div class="diary-card" v-if="diaryHistory.length > 0">
				<div class="title">📖 历史日记</div>
				<div class="history-list">
					<div class="history-item" v-for="diary in diaryHistory" :key="diary.id">
						<div class="history-header">
							<div class="history-date">{{ diary.diaryDate }}</div>
							<div class="history-emotion">
								<span class="emotion-tag">{{ diary.dominantEmotion }}</span>
								<span class="mood-score">{{ diary.moodScore }}/10</span>
							</div>
							<div class="analysis-status" v-if="diary.hasAiEmotionAnalysis">
								<el-tag type="success" size="small">已分析</el-tag>
							</div>
							<div class="analysis-status" v-else-if="diary.aiAnalysisStatus === 'PENDING'">
								<el-tag type="warning" size="small">分析中</el-tag>
							</div>
						</div>
						<div class="history-content">
							<div class="content-preview">{{ diary.diaryContentPreview || diary.diaryContent }}</div>
							<div class="ai-analysis" v-if="diary.aiEmotionAnalysis">
								<div class="analysis-label">💭 AI分析</div>
								<div class="analysis-text">{{ diary.aiEmotionAnalysis }}</div>
							</div>
						</div>
						<div class="history-meta">
							<span v-if="diary.sleepQuality">睡眠: {{ ['','很差','较差','一般','良好','优秀'][diary.sleepQuality] }}</span>
							<span v-if="diary.stressLevel">压力: {{ ['','很低','较低','中等','较高','很高'][diary.stressLevel] }}</span>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import type { EmotionDiaryQuery, EmotionDiaryVO } from '@/types/user'
import { dayjs, ElMessage } from 'element-plus'
import { submitEmotionDiary, getMyDiaries } from '@/api/user'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'

const iconUrl = new URL('@/assets/like.png', import.meta.url).href

const emotionStatus = [
	'绝望崩溃', '消沉抑郁', '焦虑烦躁', '低落不悦', '平静淡然',
	'轻松惬意', '愉悦舒心', '欢欣满足', '兴奋欣喜', '极致幸福',
]

const emotionOptions = [
	{ name: '开心', url: new URL('@/assets/开心.png', import.meta.url).href },
	{ name: '平静', url: new URL('@/assets/平静.png', import.meta.url).href },
	{ name: '焦虑', url: new URL('@/assets/焦虑.png', import.meta.url).href },
	{ name: '悲伤', url: new URL('@/assets/悲伤.png', import.meta.url).href },
	{ name: '兴奋', url: new URL('@/assets/兴奋.png', import.meta.url).href },
	{ name: '疲惫', url: new URL('@/assets/疲惫.png', import.meta.url).href },
	{ name: '惊讶', url: new URL('@/assets/惊讶.png', import.meta.url).href },
	{ name: '困惑', url: new URL('@/assets/困惑.png', import.meta.url).href },
]

const diaryForm = reactive<EmotionDiaryQuery>({
	diaryContent: '',
	diaryDate: dayjs().format('YYYY-MM-DD'),
	dominantEmotion: '',
	emotionTriggers: '',
	moodScore: 0,
	sleepQuality: null,
	stressLevel: null,
})

const submitting = ref(false)
const showAiResult = ref(false)
const aiResultLoading = ref(false)
const aiAnalysisText = ref('')

const diaryHistory = ref<EmotionDiaryVO[]>([])

const selectEmotion = (emotionName: string) => {
	diaryForm.dominantEmotion = emotionName
}

const resetDiaryForm = () => {
	diaryForm.diaryContent = ''
	diaryForm.dominantEmotion = ''
	diaryForm.emotionTriggers = ''
	diaryForm.moodScore = 0
	diaryForm.sleepQuality = null
	diaryForm.stressLevel = null
	showAiResult.value = false
	aiAnalysisText.value = ''
}

const submitDiaryEntry = async () => {
	if (!diaryForm.moodScore) { ElMessage.error('请选择情绪评分'); return }
	if (!diaryForm.dominantEmotion) { ElMessage.error('请选择主要情绪'); return }
	if (!diaryForm.emotionTriggers) { ElMessage.error('请填写情绪触发因素'); return }
	if (!diaryForm.diaryContent) { ElMessage.error('请填写今日感想'); return }
	if (!diaryForm.sleepQuality) { ElMessage.error('请选择睡眠质量'); return }
	if (!diaryForm.stressLevel) { ElMessage.error('请选择压力水平'); return }

	submitting.value = true
	showAiResult.value = true
	aiResultLoading.value = true
	aiAnalysisText.value = ''

	try {
		await submitEmotionDiary(diaryForm)
		ElMessage.success('情绪日记提交成功，AI正在分析中...')

		// 轮询等待AI分析完成（最多等待15秒）
		let attempts = 0
		const maxAttempts = 5
		const pollInterval = 3000

		while (attempts < maxAttempts) {
			await new Promise(resolve => setTimeout(resolve, pollInterval))
			const diaries = await getMyDiaries()
			diaryHistory.value = diaries
			// 找到最新的日记
			if (diaries.length > 0) {
				const latest = diaries[0]
				if (latest.hasAiEmotionAnalysis && latest.aiEmotionAnalysis) {
					aiAnalysisText.value = latest.aiEmotionAnalysis
					aiResultLoading.value = false
					break
				} else if (latest.aiAnalysisStatus === 'FAILED') {
					aiResultLoading.value = false
					ElMessage.warning('AI分析暂不可用，请稍后再试')
					break
				}
			}
			attempts++
		}
		if (aiResultLoading.value) {
			aiResultLoading.value = false
			// 可能分析还在进行中
			ElMessage.info('AI分析可能需要一些时间，请稍后在历史记录中查看')
		}
		resetDiaryForm()
	} catch (error) {
		ElMessage.error('提交失败，请稍后再试')
		showAiResult.value = false
	} finally {
		submitting.value = false
	}
}

const loadDiaryHistory = async () => {
	try {
		const diaries = await getMyDiaries()
		diaryHistory.value = diaries
	} catch {
		// 静默失败
	}
}

onMounted(() => {
	loadDiaryHistory()
})
</script>

<style scoped lang="scss">
.emotionDiary-container {
	background: var(--sketch-paper);
	min-height: calc(100vh - 60px);

	.header-section {
		background: #f0a8b8;
		color: white;
		padding: 40px 48px;
		border-bottom: 3px solid #333;
		.header-content {
			display: flex;
			align-items: center;
			gap: 16px;
			h1 { font-family: var(--font-handwrite-cn); font-size: 34px; font-weight: 400; }
		}
	}

	.content {
		margin: 0 auto;
		width: 980px;
		padding: 20px;

		.diary-card {
			margin-bottom: 20px;
			background: #fff;
			border-radius: 4px 20px 20px 8px;
			padding: 24px;
			border: 2px solid #333;
			box-shadow: 2px 3px 0 rgba(0,0,0,0.08);

			.title {
				margin-bottom: 20px;
				font-family: var(--font-handwrite);
				font-size: 22px;
				font-weight: 400;
				color: var(--sketch-ink);
			}

			.section {
				margin-bottom: 20px;
				p { font-size: 15px; color: var(--sketch-ink-light); margin-bottom: 15px; }
			}

			.emotion-grid {
				display: flex;
				flex-wrap: wrap;
				gap: 10px;
				.emotion-card {
					padding: 14px;
					border: 2px solid #333;
					border-radius: 4px 16px 16px 6px;
					text-align: center;
					cursor: pointer;
					background: #fdfaf3;
					box-shadow: 1px 2px 0 rgba(0,0,0,0.06);
					transition: all 0.3s cubic-bezier(0.34,1.56,0.64,1);
					.emotion-name {
						margin-top: 10px;
						padding: 0 36px;
						font-family: var(--font-handwrite);
						color: var(--sketch-ink);
						font-size: 14px;
					}
					&:hover { transform: translateY(-3px) rotate(-1deg); box-shadow: 2px 4px 0 rgba(0,0,0,0.1); }
					&.selected {
						border-color: #f0a8b8;
						background: #fff5f2;
						transform: translateY(-4px) rotate(-2deg);
						box-shadow: 3px 5px 0 rgba(240,168,184,0.25);
					}
				}
			}

			.detail-form {
				.form-label { margin: 12px 0 8px; font-family: var(--font-handwrite); font-size: 16px; color: var(--sketch-ink); }
				.life-indicators {
					display: flex;
					gap: 20px;
					.indicator-group { flex: 1; }
				}
				.action-buttons { margin-top: 30px; display: flex; gap: 12px; }
			}
		}

		.ai-analysis-card {
			border-left: 4px solid #f0a8b8;
			background: #fffaf7;

			.analysis-loading {
				display: flex;
				align-items: center;
				gap: 10px;
				padding: 20px;
				color: #6b7280;
				font-family: var(--font-handwrite);
				font-size: 15px;
			}

			.analysis-content {
				padding: 10px 0;
				line-height: 1.8;
				color: var(--sketch-ink);
			}

			.analysis-error {
				padding: 20px;
				color: #9ca3af;
				text-align: center;
			}
		}

		.history-list {
			display: flex;
			flex-direction: column;
			gap: 16px;

			.history-item {
				border: 2px solid #e0d8d0;
				border-radius: 4px 16px 16px 6px;
				padding: 16px;
				background: #fdfaf3;
				transition: all 0.2s;
				&:hover { border-color: #333; box-shadow: 2px 3px 0 rgba(0,0,0,0.06); }

				.history-header {
					display: flex;
					align-items: center;
					gap: 16px;
					margin-bottom: 12px;
					flex-wrap: wrap;

					.history-date { font-family: var(--font-handwrite); font-size: 16px; color: var(--sketch-ink); }
					.history-emotion {
						display: flex;
						align-items: center;
						gap: 8px;
						.emotion-tag {
							background: var(--sketch-yellow);
							color: #92400e;
							padding: 2px 10px;
							border-radius: 4px 10px 10px 4px;
							border: 1px solid #e8d5a0;
							font-family: var(--font-handwrite);
							font-size: 13px;
						}
						.mood-score { color: #6b7280; font-size: 13px; }
					}
					.analysis-status { margin-left: auto; }
				}

				.history-content {
					.content-preview {
						color: var(--sketch-ink-light);
						font-size: 14px;
						line-height: 1.6;
						margin-bottom: 10px;
					}
					.ai-analysis {
						background: #fffaf7;
						border-radius: 4px 12px 12px 4px;
						border: 1px solid #e8d8d0;
						padding: 12px;
						.analysis-label { font-size: 13px; font-weight: 600; color: #d4687c; margin-bottom: 6px; }
						.analysis-text { font-size: 13px; color: var(--sketch-ink); line-height: 1.6; }
					}
				}

				.history-meta {
					margin-top: 10px;
					display: flex;
					gap: 16px;
					span { font-size: 12px; color: #9ca3af; font-family: var(--font-handwrite); }
				}
			}
		}
	}
}
</style>

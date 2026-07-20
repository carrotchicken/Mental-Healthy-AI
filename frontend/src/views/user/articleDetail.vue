<template>
	<div class="articleDetail-container">
		<div class="header-section">
			<div class="header-content">
				<el-image :src="iconUrl" style="width: 60px; height: 60px" />
				<h1>知识文章详情</h1>
			</div>
		</div>
		<div class="content">
			<div class="diary-card">
				<p class="title">文章信息</p>
				<div class="sub-title">
					<el-tag class="category-tag" size="large">{{
						articleDetail?.categoryName
					}}</el-tag>
					<div class="flex-box">
						<el-icon><List /></el-icon>
						<span>{{
							dayjs(articleDetail?.updatedAt).format('YYYY-MM-DD')
						}}</span>
					</div>
				</div>
				<h1 class="article-title">{{ articleDetail?.title }}</h1>
				<div class="summary-content" v-if="articleDetail?.summary">
					<p>{{ articleDetail?.summary }}</p>
				</div>
				<div class="flex-box" :style="{ marginTop: '20px' }">
					<div class="item flex-box">
						<el-icon><Avatar /></el-icon>
						<span>{{ articleDetail?.authorName }}</span>
					</div>
					<div class="item flex-box">
						<el-icon><Platform /></el-icon>
						<span>{{ articleDetail?.readCount }}次阅读</span>
					</div>
				</div>
			</div>
			<div class="diary-card">
				<div class="title">正文内容</div>
				<div
					class="content-wrapper"
					v-html="formatContent(articleDetail?.content)"></div>
				<div
					class="tags-content"
					v-if="
						articleDetail?.tagArray && articleDetail.tagArray.length
					">
					<h4 class="tags-title">相关标签</h4>
					<div class="tags-list">
						<el-tag
							v-for="tag in articleDetail?.tagArray"
							:key="tag"
							type="info"
							effect="light"
							class="tag-item"
							>{{ tag }}</el-tag
						>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { KnowledgeArticleDetail } from '@/types/user'
import { getKnowledgeArticleDetail } from '@/api/user'
import dayjs from 'dayjs'

const iconUrl = new URL('@/assets/book.png', import.meta.url).href

const props = defineProps<{
	id: string
}>()

const articleDetail = ref<KnowledgeArticleDetail>()

const getArticleDetail = async () => {
	const data = await getKnowledgeArticleDetail(props.id)
	articleDetail.value = data
}

const formatContent = (content: string | undefined) => {
	if (!content) return ''

	// 基本的HTML清理和格式化
	let formatted = content
		.replace(/\n/g, '<br>')
		.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
		.replace(/\*(.*?)\*/g, '<em>$1</em>')

	return formatted
}

onMounted(() => {
	getArticleDetail()
})
</script>

<style scoped lang="scss">
.articleDetail-container {
	background: var(--sketch-paper);
	min-height: calc(100vh - 60px);
	.flex-box {
		display: flex;
		align-items: center;
		.item {
			margin-right: 20px;
			span { margin-left: 5px; }
		}
	}
	.header-section {
		background: #7eb8da;
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
				margin-bottom: 15px;
				font-family: var(--font-handwrite);
				font-size: 22px;
				font-weight: 400;
				color: var(--sketch-ink);
			}
			.sub-title {
				margin-top: 20px;
				display: flex;
				align-items: center;
				.category-tag { margin-right: 20px; }
			}
			.article-title {
				font-family: var(--font-handwrite-cn);
				font-size: 28px;
				font-weight: 400;
				color: var(--sketch-ink);
				margin-top: 30px;
				margin-bottom: 10px;
			}
			.summary-content {
				background: #f0f4ff;
				border-left: 4px solid #7eb8da;
				padding: 12px 16px;
				border-radius: 0 10px 10px 0;
			}
			.content-wrapper {
				font-size: 15px;
				color: var(--sketch-ink-light);
				line-height: 1.9;
				:deep(p) { margin-bottom: 10px; }
				:deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
					margin: 15px 0 10px;
					font-family: var(--font-handwrite);
					color: var(--sketch-ink);
				}
				:deep(h2) {
					font-size: 18px;
					border-bottom: 2px dashed #d0c8c0;
					padding-bottom: 5px;
				}
				:deep(h3) { font-size: 14px; }
				:deep(ul), :deep(ol) {
					padding-left: 15px;
					margin-bottom: 10px;
				}
				:deep(li) { list-style: disc; margin-bottom: 5px; }
			}
			.tags-content {
				margin-top: 20px;
				padding-top: 15px;
				border-top: 2px dashed #d0c8c0;
				.tags-title {
					margin-bottom: 10px;
					font-family: var(--font-handwrite);
					font-size: 15px;
					color: var(--sketch-ink);
				}
				.tags-list {
					display: flex;
					flex-wrap: wrap;
					gap: 10px;
				}
			}
		}
	}
}
</style>

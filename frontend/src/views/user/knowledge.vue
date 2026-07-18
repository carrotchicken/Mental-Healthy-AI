<template>
	<div class="knowledge-container">
		<div class="header-section">
			<div class="header-content">
				<el-image :src="iconUrl" style="width: 60px; height: 60px" />
				<h1>心理健康知识库</h1>
			</div>
		</div>
		<div class="content">
			<div class="recommend-section">
				<div class="section-title">推荐阅读</div>
				<div class="recommend-list">
					<div
						v-for="item in recommendList"
						:key="item.id"
						class="recommend-item"
						@click="goToArticle(item.id)">
						<h4>{{ item.title }}</h4>
						<p class="read-count">
							<el-icon><Histogram /></el-icon>
							阅读量{{ item.readCount }}
						</p>
					</div>
				</div>
			</div>
			<div class="article-list">
				<div
					class="article-item"
					v-for="item in articleList"
					:key="item.id"
					@click="goToArticle(item.id)">
					<el-image
						:src="
							item.coverImage ?
								'http://159.75.169.224:1235' + item.coverImage
							:	'https://file.itndedu.com/psychology_ai.png'
						"
						style="width: 240px; height: 150px">
					</el-image>
					<div class="info">
						<div class="title">
							<h3>{{ item.title }}</h3>
							<el-tag Plain type="primary">{{
								item.categoryName
							}}</el-tag>
						</div>
						<div :style="{ marginTop: '10px' }">
							<div class="flex-box">
								<el-icon><Avatar /></el-icon>
								<span>{{ item.authorName }}</span>
							</div>
							<div class="flex-box">
								<el-icon><List /></el-icon>
								<span>{{
									dayjs(item.updatedAt).format('YYYY-MM-DD')
								}}</span>
							</div>
						</div>
						<div :style="{ marginTop: '20px' }">
							<div class="flex-box">
								<el-icon><Platform /></el-icon>
								<span>观看人数：{{ item.readCount }}</span>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="pagination-wrapper">
		<el-pagination
			style="margin-top: 25px"
			v-model:current-page="pagination.currentPage"
			v-model:page-size="pagination.pageSize"
				:total="Number(pagination.total)"
				layout="prev, pager, next"
				@current-change="handlePageChange"
			@size-change="getPageList" />
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import type { KnowledgeArticleQuery, KnowledgeArticle } from '@/types/user'
import { getKnowledgeArticlesList } from '@/api/user'

import { useRouter } from 'vue-router'

const iconUrl = new URL('@/assets/book.png', import.meta.url).href

const router = useRouter()

const pagination = ref({
	currentPage: 1,
	pageSize: 10,
	total: 0,
})

const recommendList = ref<KnowledgeArticle[]>([])

const articleList = ref<KnowledgeArticle[]>([])

const getRecommendArticles = async () => {
	const params: KnowledgeArticleQuery = {
		currentPage: 1,
		pageSize: 5,
		sortDirection: 'desc',
		sortField: 'readCount',
	}
	const data = await getKnowledgeArticlesList(params)
	recommendList.value = data.records
}

const getPageList = async () => {
	const params: KnowledgeArticleQuery = {
		sortDirection: 'desc',
		sortField: 'publishedAt',
		...pagination.value,
	}
	const data = await getKnowledgeArticlesList(params)
	articleList.value = data.records
	pagination.value.total = data.total
}

const handlePageChange = (page: number) => {
	pagination.value.currentPage = page
	getPageList()
}

const goToArticle = async (id: number) => {
	router.push(`/knowledge/article/${id}`)
}

onMounted(() => {
	getRecommendArticles()
	getPageList()
})
</script>

<style scoped lang="scss">
.knowledge-container {
	background: var(--sketch-paper);
	min-height: calc(100vh - 60px);
	.flex-box {
		display: flex;
		align-items: center;
		span { margin-left: 10px; }
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
		display: flex;
		gap: 20px;
		margin: 0 auto;
		width: 1200px;
		padding: 20px;
		.recommend-section {
			width: 280px;
			background: #fff;
			border-radius: 4px 20px 20px 8px;
			border: 2px solid #333;
			box-shadow: 2px 3px 0 rgba(0,0,0,0.08);
			padding: 20px;
			.section-title {
				font-family: var(--font-handwrite);
				font-size: 20px;
				color: var(--sketch-ink);
				margin-bottom: 16px;
				display: flex;
				align-items: center;
				gap: 8px;
			}
			.recommend-list {
				display: flex;
				flex-direction: column;
				gap: 14px;
				.recommend-item {
					border-left: 4px solid #7eb8da;
					background: #fffaf7;
					border-radius: 0 10px 10px 0;
					padding: 12px 14px;
					cursor: pointer;
					transition: all 0.3s cubic-bezier(0.34,1.56,0.64,1);
					h4 { font-family: var(--font-handwrite); font-size: 15px; color: var(--sketch-ink); margin-bottom: 8px; }
					.read-count {
						font-size: 12px;
						color: #9ca3af;
						display: flex;
						align-items: center;
						gap: 6px;
					}
					&:hover {
						transform: translateX(4px) rotate(0.5deg);
						box-shadow: 2px 3px 0 rgba(0,0,0,0.08);
					}
				}
			}
		}
		.article-list {
			flex: 1;
			.article-item {
				background: #fff;
				border-radius: 4px 18px 18px 8px;
				border: 2px solid #333;
				box-shadow: 2px 3px 0 rgba(0,0,0,0.08);
				padding: 16px;
				margin-bottom: 20px;
				display: flex;
				cursor: pointer;
				transition: all 0.3s cubic-bezier(0.34,1.56,0.64,1);
				&:hover {
					transform: translateY(-3px) rotate(-0.5deg);
					box-shadow: 3px 5px 0 rgba(0,0,0,0.12);
				}
				.info {
					margin-left: 20px;
					flex: 1;
					.title {
						display: flex;
						align-items: center;
						gap: 10px;
						h3 { font-family: var(--font-handwrite); font-size: 18px; color: var(--sketch-ink); }
					}
				}
			}
		}
	}
	.pagination-wrapper {
		display: flex;
		justify-content: center;
		padding-bottom: 30px;
	}
}
</style>

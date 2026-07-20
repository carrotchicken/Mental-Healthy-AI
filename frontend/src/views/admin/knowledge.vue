<template>
	<div>
		<PageHead title="知识文章">
			<template #buttons>
				<el-button type="primary" @click="handleEdit(null)"
					>新增</el-button
				>
			</template>
		</PageHead>
		<TableSearch :formItem="formItem" @search="handleSearch" />
		<el-table :data="tableData" style="width: 100%; margin-top: 25px">
			<el-table-column
				prop="title"
				label="文章标题"
				width="300"
				fixed="left">
				<template #default="scope">
					<el-icon><time /></el-icon>
					<span>{{ scope.row.title }}</span>
				</template>
			</el-table-column>
			<el-table-column prop="categoryName" label="分类">
				<template #default="scope">
					<el-icon><time /></el-icon>
					<span>{{ categoryMap[scope.row.categoryId] }}</span>
				</template>
			</el-table-column>
			<el-table-column prop="authorName" label="作者" width="150" />
			<el-table-column prop="readCount" label="阅读量" width="150" />
			<el-table-column prop="updatedAt" label="发布时间" width="150" />
			<el-table-column label="操作" width="240" fixed="right">
				<template #default="scope">
					<el-button
						@click="handleEdit(scope.row)"
						text
						type="primary"
						>编辑</el-button
					>
					<el-button
						v-if="scope.row.status === 0 || scope.row.status === 2"
						@click="handlePublish(scope.row)"
						text
						type="success"
						>发布</el-button
					>
					<el-button
						v-if="scope.row.status === 1"
						@click="handleUnpublish(scope.row)"
						text
						type="warning"
						>下架</el-button
					>
					<el-button
						@click="handleDelete(scope.row)"
						text
						type="danger"
						>删除</el-button
					>
				</template>
			</el-table-column>
		</el-table>
		<el-pagination
			style="margin-top: 25px"
			v-model:current-page="pagination.currentPage"
			v-model:page-size="pagination.size"
			:total="Number(pagination.total)"
			layout="prev, pager, next, total"
			@current-change="handleSearch"
			@size-change="handleSearch"></el-pagination>
		<ArticleDialog
			v-model:modelValue="dialogVisible"
			:categories="categories"
			:articleData="articleData"
			@success="handleChange" />
	</div>
</template>
<script setup lang="ts">
import type {
	FormItemConfig,
	SearchFormData,
	KnowledgeArticleList,
	KnowledgeArticle,
	KnowledgeArticleData,
} from '@/types/admin'
import {
	getCategoryTree,
	getArticlePage,
	getArticleById,
	changeArticleStatus,
	deleteArticle,
} from '@/api/admin'
import { onMounted, reactive, ref } from 'vue'
import ArticleDialog from '@/components/ArticleDialog.vue'
import { ElMessageBox } from 'element-plus'

const formItem: FormItemConfig[] = reactive([
	{
		comp: 'el-input',
		prop: 'title',
		label: '文章标题',
		placeholder: '请输入文章标题',
	},
	{
		comp: 'el-select',
		prop: 'categoryId',
		label: '文章分类',
		placeholder: '请选择文章分类',
	},
	{
		comp: 'el-select',
		prop: 'status',
		label: '文章状态',
		placeholder: '请选择文章状态',
		options: [
			{ label: '草稿', value: 0 },
			{ label: '已发布', value: 1 },
			{ label: '已下架', value: 2 },
		],
	},
])

// 分类映射
const categoryMap = reactive<Record<number, string>>({})

// 分类列表
const categories = ref<{ label: string; value: number }[]>([])

// 文章数据
const tableData = ref<KnowledgeArticle[]>([])

onMounted(async () => {
	const data: KnowledgeArticleList[] = await getCategoryTree()
	categories.value = data.map((item: KnowledgeArticleList) => {
		categoryMap[item.id] = item.categoryName
		return { label: item.categoryName, value: item.id }
	})
	formItem[1].options = categories.value
	handleSearch()
})

const pagination = reactive({ currentPage: 1, size: 3, total: 0 })

const handleSearch = async (formData?: SearchFormData) => {
	const params = { ...formData, ...pagination } as SearchFormData
	const { records, total } = await getArticlePage(params)
	tableData.value = records as KnowledgeArticle[]
	pagination.total = total
}

const handleChange = () => {
	handleSearch()
}

// 新增和编辑
const dialogVisible = ref<boolean>(false)

// 编辑文章数据
const articleData = ref<KnowledgeArticleData | null>(null)

const handleEdit = async (row: KnowledgeArticle | null) => {
	if (!row || !row.id) {
		articleData.value = null
		dialogVisible.value = true
	} else {
		try {
			articleData.value = await getArticleById(row.id)
			dialogVisible.value = true
		} catch (error) {
			console.error('获取文章详情失败:', error)
			return
		}
	}
}

const handlePublish = (row: KnowledgeArticle) => {
	ElMessageBox.confirm(`确认发布文章《${row.title}》吗？`, '确认', {
		confirmButtonText: '确认',
		cancelButtonText: '取消',
		type: 'info',
	}).then(async () => {
		try {
			await changeArticleStatus(row.id, row.status === 1 ? 2 : 1)
			ElMessageBox.alert(
				`文章《${row.title}》已${row.status === 1 ? '下架' : '发布'}成功！`,
				'成功',
				{ confirmButtonText: '确定', type: 'success' },
			)
			handleSearch()
		} catch (error) {
			console.error('修改文章状态失败:', error)
			return
		}
	})
}

const handleUnpublish = (row: KnowledgeArticle) => {
	ElMessageBox.confirm(`确认下架文章《${row.title}》吗？`, '确认', {
		confirmButtonText: '确认',
		cancelButtonText: '取消',
		type: 'warning',
	}).then(async () => {
		try {
			await changeArticleStatus(row.id, row.status === 1 ? 2 : 1)
			ElMessageBox.alert(
				`文章《${row.title}》已${row.status === 1 ? '下架' : '发布'}成功！`,
				'成功',
				{ confirmButtonText: '确定', type: 'success' },
			)
			handleSearch()
		} catch (error) {
			console.error('修改文章状态失败:', error)
			return
		}
	})
}

const handleDelete = (row: KnowledgeArticle) => {
	ElMessageBox.confirm(`确认删除文章《${row.title}》吗？`, '确认', {
		confirmButtonText: '确认',
		cancelButtonText: '取消',
		type: 'warning',
	}).then(async () => {
		try {
			await deleteArticle(row.id)
			ElMessageBox.alert(`文章《${row.title}》已删除成功！`, '成功', {
				confirmButtonText: '确定',
				type: 'success',
			})
			handleSearch()
		} catch (error) {
			console.error('删除文章失败:', error)
			return
		}
	})
}
</script>

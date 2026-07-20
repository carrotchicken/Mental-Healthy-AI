<template>
	<el-dialog
		v-model="dialogVisible"
		:title="isEdit ? '编辑文章' : '文章详情'"
		width="50%"
		:close-on-click-model="false">
		<el-form
			:model="formData"
			:rules="rules"
			ref="formRef"
			label-width="120px">
			<el-form-item label="文章标题" prop="title">
				<el-input
					v-model="formData.title"
					placeholder="请输入文章标题"
					maxlength="200" />
			</el-form-item>
			<el-form-item label="文章分类" prop="categoryId">
				<el-select
					v-model="formData.categoryId"
					placeholder="请选择文章分类">
					<el-option
						v-for="category in props.categories"
						:key="category.value"
						:label="category.label"
						:value="category.value" />
				</el-select>
			</el-form-item>
			<el-form-item label="文章摘要" prop="summary">
				<el-input
					type="textarea"
					v-model="formData.summary"
					placeholder="请输入文章摘要(可选)"
					maxlength="1000"
					show-word-limit
					:rows="4" />
			</el-form-item>
			<el-form-item label="标签" prop="tags">
				<el-select
					v-model="formData.tags"
					placeholder="请输入文章标签"
					multiple
					filterable
					allow-create
					style="width: 100%">
					<el-option
						v-for="tag in commonTags"
						:key="tag"
						:label="tag"
						:value="tag" />
				</el-select>
			</el-form-item>
			<el-form-item label="封面图片">
				<div class="cover-upload">
					<el-upload
						class="avatar-uploader"
						:action="''"
						:before-upload="beforeUpload"
						:http-request="handleUploadRequest"
						:show-file-list="false"
						accept="image/*">
						<div v-if="!imgUrl" class="cover-placeholder">
							<el-icon><Upload /></el-icon>
							<p>点击上传封面图片</p>
						</div>
						<el-image
							v-else
							:src="imgUrl"
							fit="cover"
							class="cover-preview"
							style="width: 200; height: 120px" />
					</el-upload>
					<div v-if="imgUrl" class="cover-remove">
						<el-button
							type="danger"
							size="small"
							@click="((imgUrl = ''), (formData.coverImage = ''))"
							>移除图片</el-button
						>
					</div>
				</div>
			</el-form-item>
			<el-form-item label="文章内容" prop="content">
				<RichTextEditor
					v-model="formData.content"
					placeholder="请输入文章内容,支持富文本格式"
					:maxCharCount="5000"
					@change="handleContentChange"
					@created="handleEditorCreated"
					min-height="400px" />
			</el-form-item>
		</el-form>
		<div v-if="btnPreview">
			<h3>内容预览</h3>
			<div v-html="formData.content"></div>
		</div>
		<template #footer>
			<el-button @click="btnPreview = !btnPreview">{{
				btnPreview ? '隐藏预览' : '预览效果'
			}}</el-button>
			<el-button @click="handleClose">取消</el-button>
			<el-button
				type="primary"
				@click="handleSubmit()"
				:loading="loading"
				>{{ isEdit ? '更新文章' : '创建文章' }}</el-button
			>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, reactive, nextTick, watch } from 'vue'
import type { KnowledgeArticleData } from '@/types/admin'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { uploadFile, createArticle, updateArticle } from '@/api/admin'
import { fileBaseUrl } from '@/config'

const formRef = ref<FormInstance>()

const dialogVisible = defineModel<boolean>()

const props = withDefaults(
	defineProps<{
		categories: { label: string; value: number }[]
		articleData: KnowledgeArticleData | null
	}>(),
	{ categories: () => [], articleData: null },
)

const emit = defineEmits(['success'])

const formData = reactive<KnowledgeArticleData>({
	title: '',
	content: '',
	coverImage: '',
	categoryId: 4,
	summary: '',
	tags: '',
})

const rules = reactive({
	title: [
		{ required: true, message: '请输入文章标题', trigger: 'blur' },
		{ min: 1, max: 200, message: '长度必须在 1-200 之间', trigger: 'blur' },
	],
	categoryId: [
		{ required: true, message: '请选择文章分类', trigger: 'change' },
	],
	content: [
		{ required: true, message: '请输入文章内容', trigger: 'blur' },
		{ max: 5000, message: '长度必须在 5000 字以内', trigger: 'blur' },
	],
})

const commonTags = [
	'情绪管理',
	'焦虑',
	'抑郁',
	'压力',
	'睡眠',
	'冥想',
	'正念',
	'放松',
	'心理健康',
	'自我成长',
	'人际关系',
	'工作压力',
	'学习方法',
	'生活技巧',
]

const imgUrl = ref<string>('')
const beforeUpload = (file: File) => {
	const isImage = file.type.startsWith('image/')
	const isLt5M = file.size / 1024 / 1024 < 5

	if (!isImage) {
		ElMessage.error('只能上传图片文件！')
		return false
	}
	if (!isLt5M) {
		ElMessage.error('图片大小不能超过 5MB！')
		return false
	}
	return true
}

const businessId = ref<string | undefined>()

const handleUploadRequest = async ({ file }: { file: File }) => {
	businessId.value = crypto.randomUUID() // 生成唯一的业务ID
	const fileRes = await uploadFile(file, businessId.value)
	imgUrl.value = fileBaseUrl + fileRes.filePath
	formData.coverImage = fileRes.filePath
}

const handleContentChange = (data: { html: string; text: string }) => {
	formData.content = data.html
}

const editorInstance = ref<any>(null)
const handleEditorCreated = (editor: any) => {
	editorInstance.value = editor
	if (formData.content && editor) {
		nextTick(() => {
			editor.setHtml(formData.content)
		})
	}
}

const btnPreview = ref<boolean>(false)

const loading = ref<boolean>(false)

const handleClose = () => {
	dialogVisible.value = false
	businessId.value = undefined
	imgUrl.value = ''
	formData.tags = ''
	formRef.value?.resetFields()
}

const handleSubmit = () => {
	if (!formRef) return
	formRef.value?.validate(async valid => {
		if (valid) {
			loading.value = true
			formData.tags =
				Array.isArray(formData.tags) ?
					formData.tags.join(',')
				:	formData.tags.toString()
			try {
				if (isEdit.value) {
					if (!formData.id) {
						ElMessage.error('文章ID缺失，无法更新')
						return
					}
					await updateArticle(formData.id, formData)
					ElMessage.success('文章更新成功')
				} else {
					await createArticle(formData)
					ElMessage.success('文章创建成功')
				}
				handleClose()
			} catch (error) {
				ElMessage.error('文章创建失败，请重试')
			} finally {
				loading.value = false
				emit('success')
			}
		} else {
			ElMessage.error('请完善表单信息')
		}
	})
}

const isEdit = computed(() => !!props.articleData?.id)

watch(
	() => props.articleData,
	newVal => {
		if (newVal) {
			nextTick(() => {
				Object.assign(formData, newVal)
				businessId.value = newVal.id
				imgUrl.value = fileBaseUrl + newVal.coverImage
			})
		}
	},
)
</script>

<style scoped lang="scss">
.cover-placeholder {
	width: 200px;
	height: 120px;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	color: #8b949e;
	background: #f6f8fa;
}
</style>

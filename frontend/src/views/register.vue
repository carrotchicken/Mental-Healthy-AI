<template>
	<div class="container">
		<div class="title">
			<div class="title-text">
				<h2>创建您的账户</h2>
				<p>请填写注册信息</p>
			</div>
		</div>
		<div class="form-container">
			<el-form
				label-position="top"
				:model="formData"
				:rules="rules"
				ref="formRef">
				<el-form-item label="用户名或邮箱" prop="username">
					<el-input
						v-model="formData.username"
						placeholder="请输入用户名或邮箱"
						size="large" />
				</el-form-item>
				<el-form-item label="邮箱" prop="email">
					<el-input
						v-model="formData.email"
						placeholder="请输入邮箱"
						size="large" />
				</el-form-item>
				<el-form-item label="昵称" prop="nickname">
					<el-input
						v-model="formData.nickname"
						placeholder="请输入昵称（可选）"
						size="large" />
				</el-form-item>
				<el-form-item label="手机号" prop="phone">
					<el-input
						v-model="formData.phone"
						placeholder="请输入手机号（可选）"
						readonly
						@focus="
							(e: FocusEvent) =>
								(e.target as HTMLInputElement).removeAttribute(
									'readonly',
								)
						"
						size="large" />
				</el-form-item>
				<el-form-item label="密码" prop="password">
					<el-input
						v-model="formData.password"
						type="password"
						placeholder="请输入密码"
						readonly
						@focus="
							(e: FocusEvent) =>
								(e.target as HTMLInputElement).removeAttribute(
									'readonly',
								)
						"
						size="large"
						show-password />
				</el-form-item>
				<el-form-item label="确认密码" prop="confirmPassword">
					<el-input
						v-model="formData.confirmPassword"
						type="password"
						placeholder="请再次输入密码"
						size="large"
						show-password />
				</el-form-item>
				<el-form-item>
					<el-button
						type="primary"
						size="large"
						class="btn"
						@click="submitForm(formRef)"
						>注册</el-button
					>
				</el-form-item>
			</el-form>
		</div>
	</div>
</template>

<script setup lang="ts">
import type { RegisterFormQuery } from '@/types/user'
import { register } from '@/api/user'
import { ref, reactive } from 'vue'
import type { FormRules, FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()

const formData = reactive<RegisterFormQuery>({
	username: '',
	email: '',
	nickname: '',
	phone: '',
	password: '',
	confirmPassword: '',
	gender: 0,
	userType: 1,
})

const rules: FormRules<RegisterFormQuery> = {
	username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
	email: [
		{ required: true, message: '请输入邮箱', trigger: 'blur' },
		{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
	],
	password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
	confirmPassword: [
		{ required: true, message: '请确认密码', trigger: 'blur' },
		{
			validator: (_, value, callback) => {
				if (value !== formData.password) {
					callback(new Error('两次输入的密码不一致'))
				} else {
					callback()
				}
			},
			trigger: 'blur',
		},
	],
}

const formRef = ref<FormInstance>()
const submitForm = async (formEl: FormInstance | undefined) => {
	if (!formEl) return
	formEl.validate(async valid => {
		if (!valid) return
		try {
			await register(formData)
			ElMessage.success('注册成功')
			setTimeout(() => {
				router.push('/auth/login')
			}, 1000)
		} catch {
			// 错误已在拦截器中处理
		}
	})
}
</script>

<style scoped lang="scss">
.container {
	width: 384px;
	.title {
		.title-text {
			text-align: center;
			h2 {
				font-family: var(--font-handwrite-cn);
				font-size: 38px;
				font-weight: 400;
				margin-bottom: 10px;
				color: var(--sketch-ink);
			}
			p {
				font-family: var(--font-handwrite);
				font-size: 16px;
				color: #9ca3af;
			}
		}
	}
	.form-container {
		margin-top: 30px;
		background: #fff;
		border: 2px solid #333;
		border-radius: 6px 24px 24px 8px;
		padding: 28px 24px;
		box-shadow: 3px 4px 0 rgba(0,0,0,0.08);
		.btn {
			margin-top: 32px;
			width: 100%;
			font-family: var(--font-handwrite);
			font-size: 18px;
		}
		.footer {
			padding: 20px 0 0;
			text-align: center;
		}
	}
}
</style>

<template>
	<div class="container">
		<div class="title">
			<div class="back-home">
				<el-icon><Back /></el-icon>
				<span @click="$router.push('/')">返回首页</span>
			</div>
			<div class="title-text">
				<h2>登陆您的账户</h2>
				<p>请输入您的登录信息</p>
			</div>
			<div class="form-container">
		<el-form
			ref="formRef"
			:model="formData"
			:rules="rules"
			class="login-form"
			@submit.prevent
			label-position="top">
					<el-form-item label="用户名或邮箱" prop="username">
						<el-input
							v-model="formData.username"
							placeholder="请输入用户名或邮箱"
							@keyup.enter="
								$event.target
									.closest('.el-form-item')
									.nextElementSibling?.querySelector('input')
									?.focus()
							"
							size="large" />
					</el-form-item>
					<el-form-item label="密码" prop="password">
						<el-input
							v-model="formData.password"
							type="password"
							placeholder="请输入密码"
							@keyup.enter="submitForm(formRef)"
							size="large"
							show-password />
					</el-form-item>
					<el-button
						class="btn"
						size="large"
						type="primary"
						@click="submitForm(formRef)"
						>登录</el-button
					>
				</el-form>
				<div class="footer">
					<el-button
						type="text"
						@click="$router.push('/auth/register')"
						>没有账户？立即注册</el-button
					>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { login } from '@/api/admin'
import type { LoginForm } from '@/types/admin'
import { ElMessage } from 'element-plus'

const formRef = ref<FormInstance>()

const formData = reactive<LoginForm>({ username: '', password: '' })

const rules: FormRules<LoginForm> = {
	username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
	password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const submitForm = async (formEl: FormInstance | undefined) => {
	if (!formEl) return
	await formEl.validate(async (valid, fields) => {
		if (valid) {
		login(formData).then(data => {
			if (!data.token) {
				return ElMessage.error('登录失败，请检查用户名和密码')
			}
			localStorage.setItem('token', data.token)
			localStorage.setItem('userInfo', JSON.stringify(data.userInfo))
			ElMessage.success('登录成功')
			setTimeout(() => {
				if (data.userInfo.userType === 1) {
					window.location.href = '/'
				} else if (data.userInfo.userType === 2) {
					window.location.href = '/back/dashboard'
				}
			}, 1000)
		}).catch(() => {
			// 错误已由拦截器统一提示
			})
		} else {
			console.log('error submit!', fields)
		}
	})
}
</script>

<style scoped lang="scss">
.container {
	width: 384px;
	.title {
		.back-home {
			margin-bottom: 60px;
			font-family: var(--font-handwrite);
			font-size: 15px;
			color: var(--sketch-ink-light);
			cursor: pointer;
			display: flex;
			align-items: center;
			gap: 4px;
		}
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
				margin-top: 20px;
				text-align: center;
			}
		}
	}
}
</style>

<template>
	<div class="frontend-layout">
		<div class="navbar-container">
			<div class="brand-section">
				<el-image :src="iconUrl" style="width: 50px; height: 50px" />
				<h1 class="brand-name">AI心理健康助手</h1>
			</div>
			<div class="nav-section">
				<router-link to="/" class="nav-link">首页</router-link>
				<router-link
					to="/consultation"
					class="nav-link"
					v-if="isLoggedIn"
					>AI咨询</router-link
				>
				<router-link
					to="/emotion-diary"
					class="nav-link"
					v-if="isLoggedIn"
					>情绪日记</router-link
				>
				<router-link to="/knowledge" class="nav-link"
					>知识库</router-link
				>
				<el-button @click="handleLogout" class="logout-btn" v-if="isLoggedIn"
					>退出登录</el-button
				>
				<template v-else>
					<router-link to="/auth/login" class="nav-link">登录</router-link>
					<router-link to="/auth/register" class="nav-link"
						><el-button type="primary">注册</el-button></router-link
					>
				</template>
			</div>
		</div>
		<div class="main-container">
			<router-view />
		</div>
		<div class="footer-container">
			<div class="footer-bottom">
				<p>© 2026 AI心理健康助手. All rights reserved.</p>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { logout } from '@/api/admin'

const iconUrl = new URL('@/assets/机器人.png', import.meta.url).href

const isLoggedIn = ref<boolean>(false)

const handleLogout = async() => {
	await logout()
	localStorage.removeItem('token')
	localStorage.removeItem('userInfo')
	isLoggedIn.value = false
	location.href = '/auth/login'
}

onMounted(() => {
	isLoggedIn.value = !!localStorage.getItem('token')
})
</script>

<style scoped lang="scss">
.frontend-layout {
	display: flex;
	flex-direction: column;
	min-height: 100vh;
	background-color: #fff;
	.navbar-container {
		width: 100%;
		max-width: 1400px;
		height: 100%;
		margin: 0 auto;
		padding: 10px 30px;
		display: flex;
		align-items: center;
		justify-content: space-between;
		.brand-section {
			display: flex;
			align-items: center;
			.brand-name {
				font-family: var(--font-handwrite-cn);
				margin-left: 10px;
				font-size: 26px;
				font-weight: 400;
				color: #333;
			}
		}
		.nav-section {
			display: flex;
			align-items: center;
			gap: 40px;
			.nav-link {
				color: #4b5563;
				font-size: 16px;
				font-weight: 500;
				&:hover {
					color: #4a90e2;
				}
			}
		}
	}

	.main-container {
		flex: 1;
		display: flex;
		flex-direction: column;
	}

	.footer-container {
		background: #1f2937;
		color: white;
		padding: 15px 0;
		margin-top: auto;
		.footer-bottom {
			max-width: 1200px;
			margin: 0 auto;
			padding: 0 10px;
			text-align: center;
		}
	}
}
</style>

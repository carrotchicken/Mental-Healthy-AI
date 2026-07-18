<template>
	<div class="navbar">
		<div class="flex-box">
			<el-button @click="handleCollapse">
				<el-icon><Expand /></el-icon>
			</el-button>
			<p class="page-title">{{ route.meta.title }}</p>
		</div>
		<div class="flex-box">
			<el-dropdown trigger="click" @command="handleCommand">
				<div class="flex-box">
					<el-image :src="userImg" fit="cover" class="user-avatar" />
					<p class="user-name">admin</p>
					<el-icon><ArrowDown /></el-icon>
				</div>
				<template #dropdown>
					<el-dropdown-menu>
						<el-dropdown-item command="layout"
							>退出登录</el-dropdown-item
						>
					</el-dropdown-menu>
				</template>
			</el-dropdown>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ElMessageBox } from 'element-plus'
import { useAdminStore } from '@/stores/appStore'
import userImg from '@/assets/user.jpg'
import { useRoute, useRouter } from 'vue-router'
import { logout } from '@/api/admin'

const route = useRoute()
const router = useRouter()

const handleCommand = (command: string) => {
	if (command === 'layout') {
		ElMessageBox.confirm('确定要退出登录吗？', '提示', {
			confirmButtonText: '确定',
			cancelButtonText: '取消',
			type: 'warning',
		}).then(async () => {
			await logout()
			localStorage.removeItem('token')
			localStorage.removeItem('userInfo')
			router.push('/auth/login')
		})
	}
}

const handleCollapse = () => {
	useAdminStore().toggleCollapse()
}
</script>

<style scoped lang="scss">
.navbar {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 0 15px;
	height: 100%;
	background-color: #fff;
	border-bottom: 1px solid #e5e7eb;
	box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
	.flex-box {
		display: flex;
		align-items: center;
		justify-content: center;
	}
	.user-avatar {
		width: 32px;
		height: 32px;
		border-radius: 50%;
		overflow: hidden;
		margin-right: 10px;
	}
	.page-title {
		font-size: 26px;
		font-weight: bold;
		color: #1f2927;
		margin-left: 20px;
	}
}
</style>

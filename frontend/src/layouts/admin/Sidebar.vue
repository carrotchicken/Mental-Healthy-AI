<template>
	<el-aside :width="isCollapse ? '64px' : '264px'">
		<el-menu
			default-active="dashboard"
			class="el-menu-vertical-demo"
			:collapse="isCollapse"
			:collapse-transition="false">
			<div class="brand">
				<el-image
					style="width: 50px; height: 50px"
					:src="iconUrl"
					alt="logo" />
				<div v-show="!isCollapse" class="info-card">
					<h1 class="brand-title">心理健康AI助手</h1>
					<p class="brand-subtitle">管理后台</p>
				</div>
			</div>
			<el-menu-item
				v-for="item in router.options.routes[1].children"
				:key="item.path"
				:index="item.name"
				@click="selectMenu">
				<el-icon><component :is="item.meta?.icon" /></el-icon>
				<template #title>{{ item.meta?.title }}</template>
			</el-menu-item>
		</el-menu>
	</el-aside>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import iconUrl from '@/assets/机器人.png'
import { useAdminStore } from '@/stores/appStore'
import { computed } from 'vue'

const router = useRouter()

const isCollapse = computed(() => useAdminStore().isCollapse)

const selectMenu = (key: { index: string }) => {
	const currentRoute = router.options.routes[1]
	router.push(currentRoute.path + '/' + key.index)
}
</script>

<style scoped lang="scss">
.el-menu-vertical-demo {
	height: 100%;
	.brand {
		display: flex;
		justify-self: center;
		align-self: center;
		padding: 10px;
		background-color: #fff;
		border-bottom: 1px solid #e5e7eb;
		.info-card {
			margin-left: 10px;
			.brand-title {
				font-family: var(--font-handwrite-cn);
				font-size: 20px;
				font-weight: 400;
				margin-bottom: 5px;
				color: #333;
			}
			.brand-subtitle {
				font-size: 13px;
				color: #9ca3af;
			}
		}
	}
}
</style>

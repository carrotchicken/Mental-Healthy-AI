<template>
	<div class="backend-layout">
		<el-container class="main-container">
			<Sidebar />
			<el-container>
				<el-header>
					<Navbar />
				</el-header>
				<el-main class="main-content">
					<router-view class="content-container" />
				</el-main>
			</el-container>
		</el-container>
	</div>
</template>

<script setup lang="ts">
import Sidebar from '@/layouts/admin/Sidebar.vue';
import Navbar from '@/layouts/admin/Navbar.vue';
</script>

<style scoped lang="scss">
.backend-layout {
	height: 100vh;
	.el-header {
		height: 74px !important;
	}
	.main-container {
		height: 100%;
		background: var(--sketch-paper);
		.content-container {
			padding: 20px;
			background: transparent;
			min-height: calc(100% - 74px);
		}
	}
}
</style>

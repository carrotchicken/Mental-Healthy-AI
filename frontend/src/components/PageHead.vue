<template>
	<div class="page-head">
		<p class="page-title">{{ title }}</p>
		<div class="action-btn">
			<slot name="buttons"></slot>
		</div>
	</div>
</template>

<script setup lang="ts">
defineProps<{
	title: string
}>()
</script>

<style scoped lang="scss">
.page-head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 20px;
	.page-title {
		font-size: 24px;
		color: #2d3748;
	}
}
</style>

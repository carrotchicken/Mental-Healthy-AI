<template>
	<div class="home-container">
		<!-- 网格纸纹理 -->
		<div class="grid-paper"></div>

		<div class="content">
			<div class="text">
				<h2 class="title">
					一次温暖的对话<br />
					<span class="highlight">化孤独为慰藉</span>
				</h2>
				<p class="description">
					每个深夜，每个焦虑的时刻，我们都在这里。<br />不必独自承受，让心与心的连接温暖您的每一天
				</p>
				<div class="hero-actions">
					<button class="sketch-card sketch-card--primary" @click="router.push('/consultation')">
						<span class="sketch-card__icon">💬</span>
						<span class="sketch-card__text">开始倾诉，获得陪伴</span>
						<svg class="sketch-arrow" width="40" height="16"><path d="M2,8 Q20,2 38,8" fill="none" stroke="#5a7d6e" stroke-width="2"/></svg>
					</button>
					<button class="sketch-card sketch-card--secondary" @click="router.push('/emotion-diary')">
						<span class="sketch-card__icon">✍️</span>
						<span class="sketch-card__text">记录心情，释放感情</span>
						<svg class="sketch-arrow" width="40" height="16"><path d="M2,8 Q20,2 38,8" fill="none" stroke="#7d6b5a" stroke-width="2"/></svg>
					</button>
				</div>
			</div>

			<!-- 手绘便签卡片 -->
			<div class="sticky-notes">
				<div class="sticky-note sticky-note--1">
					<p>今天感觉怎么样？</p>
					<span class="sticky-note__dot"></span>
				</div>
				<div class="sticky-note sticky-note--2">
					<p>深呼吸，放轻松 🌿</p>
				</div>
				<div class="sticky-note sticky-note--3">
					<p>你并不孤单 ❤️</p>
				</div>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<style scoped lang="scss">
.home-container {
	flex: 1;
	position: relative;
	background: var(--sketch-paper);
	color: var(--sketch-ink);
	padding: 3rem 0;
	display: flex;
	align-items: center;
	justify-content: center;
	overflow: hidden;

	// 网格纸纹理
	.grid-paper {
		position: absolute;
		inset: 0;
		background-image:
			linear-gradient(rgba(180,190,200,0.15) 1px, transparent 1px),
			linear-gradient(90deg, rgba(180,190,200,0.15) 1px, transparent 1px);
		background-size: 30px 30px;
		pointer-events: none;
	}

	.content {
		position: relative;
		display: flex;
		justify-content: center;
		align-items: flex-start;
		gap: 60px;
		max-width: 1000px;
		padding: 0 40px;
	}

	.text {
		max-width: 460px;
	}
}

.title {
	font-family: var(--font-handwrite-cn);
	font-size: 48px;
	font-weight: 400;
	line-height: 1.35;
	color: var(--sketch-ink);
	text-shadow: 1px 1px 0 rgba(0,0,0,0.03);
	margin-bottom: 20px;

	.highlight {
		font-family: var(--font-handwrite-cn);
		color: #f0a8b8;
		position: relative;
		&::after {
			content: '';
			position: absolute;
			left: -4px;
			bottom: 2px;
			width: calc(100% + 8px);
			height: 12px;
			background: rgba(255,200,100,0.4);
			z-index: -1;
			border-radius: 2px;
		}
	}
}

.description {
	font-family: var(--font-handwrite);
	font-size: 18px;
	line-height: 1.8;
	color: var(--sketch-ink-light);
	margin-bottom: 30px;
}

// 便签风格按钮
.hero-actions {
	display: flex;
	flex-direction: column;
	gap: 16px;
}

.sketch-card {
	all: unset;
	display: flex;
	align-items: center;
	gap: 14px;
	padding: 18px 28px;
	border: var(--sketch-border);
	border-radius: 2px 16px 16px 8px;
	box-shadow: var(--sketch-shadow);
	cursor: pointer;
	transition: all 0.35s cubic-bezier(0.34,1.56,0.64,1);
	position: relative;
	overflow: hidden;

	&--primary {
		background: var(--sketch-yellow);
		border-color: #e8d5a0;
		transform: rotate(-0.5deg);
	}
	&--secondary {
		background: var(--sketch-blue);
		border-color: #c8d8e8;
		transform: rotate(0.8deg);
	}

	&__icon {
		font-size: 28px;
	}
	&__text {
		font-family: var(--font-handwrite);
		font-size: 20px;
		color: var(--sketch-ink);
	}

	.sketch-arrow {
		margin-left: auto;
		opacity: 0.5;
		transition: all 0.3s ease;
	}

	&:hover {
		transform: translateY(-4px) rotate(0deg) scale(1.02);
		box-shadow: var(--sketch-shadow-hover);
		.sketch-arrow { opacity: 1; transform: translateX(4px); }
	}
	&:active {
		transform: translateY(0) rotate(0deg) scale(0.98);
	}
}

// 便签装饰
.sticky-notes {
	position: relative;
	display: flex;
	flex-direction: column;
	gap: 20px;
	padding-top: 10px;
}

.sticky-note {
	font-family: var(--font-handwrite);
	font-size: 16px;
	padding: 16px 24px;
	border: 2px solid;
	border-radius: 2px 14px 14px 2px;
	box-shadow: var(--sketch-shadow);
	color: var(--sketch-ink-light);

	&--1 {
		background: var(--sketch-pink);
		border-color: #e8c8cc;
		transform: rotate(4deg);
		margin-left: 20px;
		position: relative;
		.sticky-note__dot {
			position: absolute;
			top: -8px;
			left: 28px;
			width: 16px;
			height: 16px;
			border-radius: 50%;
			background: #d4687c;
			box-shadow: 1px 2px 0 rgba(0,0,0,0.15);
		}
	}
	&--2 {
		background: var(--sketch-green);
		border-color: #c8dcc8;
		transform: rotate(-2deg);
		margin-right: 30px;
	}
	&--3 {
		background: var(--sketch-yellow);
		border-color: #e8d5a0;
		transform: rotate(1.5deg);
		margin-left: 10px;
	}
}
</style>

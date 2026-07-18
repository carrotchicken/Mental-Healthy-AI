<template>
	<div class="auth-layout">
		<div class="left-section">
			<!-- 网格纹理 -->
			<div class="grid-overlay"></div>
			<!-- 漂浮装饰 -->
			<div class="doodle-float doodle-float--1">💛</div>
			<div class="doodle-float doodle-float--2">✨</div>
			<div class="doodle-float doodle-float--3">🌿</div>
			<div class="doodle-float doodle-float--4">🌸</div>
			<svg class="doodle-line doodle-line--top" width="180" height="30"><path d="M0,15 Q45,2 90,15 Q135,28 180,15" fill="none" stroke="rgba(200,160,170,0.4)" stroke-width="2"/></svg>
			<svg class="doodle-line doodle-line--btm" width="220" height="30"><path d="M0,15 Q55,2 110,15 Q165,28 220,15" fill="none" stroke="rgba(200,160,170,0.4)" stroke-width="2" stroke-dasharray="6,3"/></svg>

			<div class="content">
				<h2 class="title">心理AI助手</h2>
				<div class="title-underline"></div>
				<p class="text">
					每个深夜，每个焦虑的时刻<br/>我们都在这里<br/>不必独自承受<br/>让心与心的连接温暖您的每一天
				</p>
				<div class="robot">
					<img src="@/assets/robot-fill.png" style="width:80px;height:80px" />
				</div>
				<p class="slogan">— 宁渡，时刻倾听 —</p>
			</div>
		</div>
		<div class="right-section">
			<router-view />
		</div>
	</div>
</template>

<script setup lang="ts"></script>

<style scoped lang="scss">
.auth-layout {
	display: flex;
	.left-section {
		position: relative;
		display: flex;
		justify-content: center;
		align-items: center;
		flex: 1;
		background: #fef0f3;
		height: 100vh;
		overflow: hidden;

		.grid-overlay {
			position: absolute;
			inset: 0;
			background-image:
				linear-gradient(rgba(0,0,0,0.04) 1px, transparent 1px),
				linear-gradient(90deg, rgba(0,0,0,0.04) 1px, transparent 1px);
			background-size: 40px 40px;
			pointer-events: none;
		}

		.doodle-float {
			position: absolute;
			font-size: 28px;
			opacity: 0.7;
			animation: float-drift 6s ease-in-out infinite;
			&--1 { top: 12%; left: 15%; animation-delay: 0s; }
			&--2 { top: 20%; right: 12%; font-size: 22px; animation-delay: 1.5s; }
			&--3 { bottom: 25%; left: 10%; font-size: 24px; animation-delay: 3s; }
			&--4 { bottom: 15%; right: 18%; animation-delay: 4.5s; }
		}

		.doodle-line {
			position: absolute;
			&--top { top: 30%; right: 5%; }
			&--btm { bottom: 35%; left: 8%; }
		}

		.content {
			position: relative;
			display: flex;
			flex-direction: column;
			justify-content: center;
			align-items: center;
			z-index: 1;

			.title {
				margin-bottom: 8px;
				font-family: var(--font-handwrite-cn);
				font-size: 44px;
				font-weight: 400;
				color: var(--sketch-ink);
			}
			.title-underline {
				width: 140px;
				height: 6px;
				background: rgba(200,160,170,0.6);
				border-radius: 3px;
				margin-bottom: 24px;
			}
			.text {
				margin-bottom: 30px;
				width: 420px;
				font-family: var(--font-handwrite);
				font-size: 19px;
				color: #8a7a7e;
				text-align: center;
				line-height: 1.9;
			}
			.robot {
				display: flex;
				justify-content: center;
				align-items: center;
				width: 160px;
				height: 160px;
				border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%;
				border: 3px solid rgba(200,160,170,0.5);
				background: rgba(255,255,255,0.6);
				box-shadow: 5px 6px 0 rgba(0,0,0,0.06);
				animation: robot-breathe 4s ease-in-out infinite;
			}
			.slogan {
				margin-top: 24px;
				font-family: var(--font-handwrite);
				font-size: 17px;
				color: #b0a0a5;
			}
		}
	}
	.right-section {
		display: flex;
		justify-content: center;
		align-items: center;
		flex: 1;
		height: 100vh;
		background: var(--sketch-paper);
	}
}

@keyframes float-drift {
	0%, 100% { transform: translateY(0) rotate(0deg); }
	50% { transform: translateY(-14px) rotate(6deg); }
}
@keyframes robot-breathe {
	0%, 100% { transform: scale(1); }
	50% { transform: scale(1.04); }
}
</style>

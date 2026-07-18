<template>
	<div class="dashboard-container">
		<el-row :gutter="20">
			<el-col :span="6">
				<el-card>
					<div class="card-content">
						<div class="avatar users">👥</div>
						<div class="info">
							<p class="title">总用户数</p>
							<p class="number">
								{{ aiData?.systemOverview.totalUsers }}
							</p>
							<p class="subtitle-title">
								活跃用户：{{
									aiData?.systemOverview.activeUsers
								}}
							</p>
						</div>
					</div>
				</el-card>
			</el-col>
			<el-col :span="6">
				<el-card>
					<div class="card-content">
						<div class="avatar like">📝</div>
						<div class="info">
							<p class="title">情绪日志</p>
							<p class="number">
								{{ aiData?.systemOverview.totalDiaries }}
							</p>
							<p class="subtitle-title">
								今日新增：{{
									aiData?.systemOverview.todayNewDiaries
								}}
							</p>
						</div>
					</div>
				</el-card> </el-col
			><el-col :span="6">
				<el-card>
					<div class="card-content">
						<div class="avatar comments">💬</div>
						<div class="info">
							<p class="title">咨询会话</p>
							<p class="number">
								{{ aiData?.systemOverview.totalSessions }}
							</p>
							<p class="subtitle-title">
								今日新增：{{
									aiData?.systemOverview.todayNewSessions
								}}
							</p>
						</div>
					</div>
				</el-card> </el-col
			><el-col :span="6">
				<el-card>
					<div class="card-content">
						<div class="avatar smile">💛</div>
						<div class="info">
							<p class="title">平均情绪</p>
							<p class="number">
								{{
									aiData?.systemOverview.avgMoodScore.toFixed(
										1,
									)
								}}/10
							</p>
							<p class="subtitle-title">情绪健康指数</p>
						</div>
					</div>
				</el-card>
			</el-col>
		</el-row>
		<el-row :gutter="20" style="margin-top: 20px">
			<el-col :span="12">
				<el-card style="width: 100%">
					<template #header>
						<span>情绪趋势分析</span>
					</template>
					<div class="chart-content">
						<div
							ref="emotionChartRef"
							style="width: 100%; height: 300px"></div>
					</div>
				</el-card>
			</el-col>
			<el-col :span="12">
				<el-card style="width: 100%">
					<template #header>
						<span>咨询会话统计</span>
					</template>
					<div class="chart-content" style="top: -18px">
						<div class="consultation-stats">
							<div class="stat-item">
								<div class="stat-label">总会话数</div>
								<div class="stat-value">
									{{
										aiData?.consultationStats.totalSessions
									}}
								</div>
							</div>
							<div class="stat-item">
								<div class="stat-label">平均时长</div>
								<div class="stat-value">
									{{
										aiData?.consultationStats
											.avgDurationMinutes
									}}分钟
								</div>
							</div>
							<div class="stat-item">
								<div class="stat-label">活跃用户</div>
								<div class="stat-value">
									{{ aiData?.systemOverview.totalSessions }}
								</div>
							</div>
						</div>
						<div
							ref="consultationChartRef"
							style="width: 100%; height: 300px"></div>
					</div>
				</el-card>
			</el-col>
		</el-row>
		<el-row style="margin-top: 20px">
			<el-card style="width: 100%">
				<template #header>
					<span>用户活跃度趋势</span>
				</template>
				<div class="chart-content">
					<div
						ref="userActivityChartRef"
						style="width: 100%; height: 300px"></div>
				</div>
			</el-card>
		</el-row>
	</div>
</template>

<script setup lang="ts">
import { getDashboardData } from '@/api/admin'
import type { DashboardData } from '@/types/admin'
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'



const aiData = ref<DashboardData | null>(null)

let emotionChart: any = null
const emotionChartRef = ref<HTMLDivElement | null>(null)
const initEmotionChart = () => {
	if (!emotionChartRef.value) return
	if (emotionChart) emotionChart.dispose()

	emotionChart = echarts.init(emotionChartRef.value)
	const TrendData = aiData.value?.emotionTrend
	const option = {
		title: {
			text: '情绪趋势分析',
			textStyle: { fontSize: 16, color: '#2d3436', fontWeight: 600 },
			left: 'center',
			top: 10,
		},
		tooltip: {
			trigger: 'axis',
			borderColor: '#fab1a0',
			borderWidth: 1,
			textStyle: { color: '#2d3436' },
		},
		legend: { data: ['平均情绪评分', '记录数量'], top: 40 },
		grid: { left: '3%', right: '4%', bottom: '3%', top: 80 },
		xAxis: {
			type: 'category',
			data: TrendData?.map(item => item.date) || [],
			axisLine: { lineStyle: { color: '#2d3436' } },
		},
		yAxis: [
			{
				type: 'value',
				name: '情绪评分',
				position: 'left',
				axisLine: { lineStyle: { color: '#2d3436' } },
			},
			{
				type: 'value',
				name: '记录数量',
				position: 'right',
				axisLine: { lineStyle: { color: '#2d3436' } },
			},
		],
		series: [
			{
				name: '平均情绪评分',
				type: 'line',
				data: TrendData?.map(item => item.avgMoodScore) || [],
				smooth: true,
				lineStyle: { color: '#faebaf', width: 3 },
				itemStyle: { color: '#faebaf' },
			},
			{
				name: '记录数量',
				type: 'line',
				smooth: true,
				data: TrendData?.map(item => item.recordCount) || [],
				lineStyle: { color: '#eeb5a3', width: 3 },
				itemStyle: { color: '#eeb5a3' },
			},
		],
	}
	emotionChart.setOption(option)
}

let consultationChart: any = null
const consultationChartRef = ref<HTMLDivElement | null>(null)
const initConsultationChart = () => {
	if (!consultationChartRef.value) return
	if (consultationChart) consultationChart.dispose()

	consultationChart = echarts.init(consultationChartRef.value)
	const dailyTrend = aiData.value?.consultationStats.dailyTrend || []
	const option = {
		title: {
			text: '咨询活动统计',
			textStyle: { fontSize: 16, fontWeight: 600, color: '#2d3436' },
			left: 'center',
			top: 10,
		},
		tooltip: {
			trigger: 'axis',
			backgroundColor: 'rgba(255, 255, 255, 0.95)',
			borderColor: '#fab1a0',
			borderWidth: 1,
			textStyle: { color: '#2d3436' },
		},
		legend: {
			data: ['会话数量', '参与用户数'],
			top: 40,
			textStyle: { color: '#636e72' },
		},
		grid: {
			left: '3%',
			right: '4%',
			bottom: '10%',

			containLabel: true,
		},
		xAxis: {
			type: 'category',
			data: dailyTrend.map(item => item.date),
			axisLine: { lineStyle: { color: 'rgba(244, 162, 97, 0.3)' } },
			axisLabel: { color: '#636e72' },
		},
		yAxis: {
			type: 'value',
			axisLabel: { color: '#636e72' },
			axisLine: { lineStyle: { color: 'rgba(244, 162, 97, 0.3)' } },
			splitLine: { lineStyle: { color: 'rgba(244, 162, 97, 0.1)' } },
		},
		series: [
			{
				name: '会话数量',
				type: 'bar',
				data: dailyTrend.map(item => item.sessionCount),
				itemStyle: {
					color: {
						type: 'linear',
						x: 0,
						y: 0,
						x2: 0,
						y2: 1,
						colorStops: [
							{ offset: 0, color: '#74b9ff' },
							{ offset: 1, color: '#0984e3' },
						],
					},
				},
				barWidth: '40%',
			},
			{
				name: '参与用户数',
				type: 'bar',
				data: dailyTrend.map(item => item.userCount),
				itemStyle: {
					color: {
						type: 'linear',
						x: 0,
						y: 0,
						x2: 0,
						y2: 1,
						colorStops: [
							{ offset: 0, color: '#fdcb6e' },
							{ offset: 1, color: '#f39c12' },
						],
					},
				},
				barWidth: '40%',
			},
		],
	}
	consultationChart.setOption(option)
}

let userActivityChart: any = null
const userActivityChartRef = ref<HTMLDivElement | null>(null)
const initUserActivityChart = () => {
	if (!userActivityChartRef.value) return
	if (userActivityChart) userActivityChart.dispose()

	userActivityChart = echarts.init(userActivityChartRef.value)
	const activityData = aiData.value?.userActivity || []
	const option = {
		title: {
			text: '用户活跃度趋势',
			textStyle: { fontSize: 16, fontWeight: 600, color: '#2d3436' },
			left: 'center',
			top: 10,
		},
		tooltip: {
			trigger: 'axis',
			backgroundColor: 'rgba(255, 255, 255, 0.95)',
			borderColor: '#fab1a0',
			borderWidth: 1,
			textStyle: { color: '#2d3436' },
		},
		legend: {
			data: ['活跃用户', '新增用户', '日记用户', '咨询用户'],
			top: 40,
			textStyle: { color: '#636e72' },
		},
		grid: {
			left: '3%',
			right: '4%',
			bottom: '3%',
			top: 80,
			containLabel: true,
		},
		xAxis: {
			type: 'category',
			data: activityData.map(item => item.date),
			axisLine: { lineStyle: { color: 'rgba(244, 162, 97, 0.3)' } },
			axisLabel: { color: '#636e72' },
		},
		yAxis: {
			type: 'value',
			axisLabel: { color: '#636e72' },
			axisLine: { lineStyle: { color: 'rgba(244, 162, 97, 0.3)' } },
			splitLine: { lineStyle: { color: 'rgba(244, 162, 97, 0.1)' } },
		},
		series: [
			{
				name: '活跃用户',
				type: 'line',
				data: activityData.map(item => item.activeUsers),
				smooth: true,
				lineStyle: { width: 3, color: '#a29bfe' },
				itemStyle: { color: '#a29bfe' },
				areaStyle: {
					color: {
						type: 'linear',
						x: 0,
						y: 0,
						x2: 0,
						y2: 1,
						colorStops: [
							{ offset: 0, color: 'rgba(162, 155, 254, 0.4)' },
							{ offset: 1, color: 'rgba(162, 155, 254, 0.1)' },
						],
					},
				},
			},
			{
				name: '新增用户',
				type: 'line',
				data: activityData.map(item => item.newUsers),
				smooth: true,
				lineStyle: { width: 3, color: '#fdcb6e' },
				itemStyle: { color: '#fdcb6e' },
			},
			{
				name: '日记用户',
				type: 'line',
				data: activityData.map(item => item.diaryUsers),
				smooth: true,
				lineStyle: { width: 3, color: '#00b894' },
				itemStyle: { color: '#00b894' },
			},
			{
				name: '咨询用户',
				type: 'line',
				data: activityData.map(item => item.consultationUsers),
				smooth: true,
				lineStyle: { width: 3, color: '#fab1a0' },
				itemStyle: { color: '#fab1a0' },
			},
		],
	}
	userActivityChart.setOption(option)
}
const initCharts = () => {
	initEmotionChart()
	initConsultationChart()
	initUserActivityChart()
}

onMounted(async () => {
	try {
		aiData.value = await getDashboardData()
		initCharts()
	} catch (error) {
		console.error('Failed to fetch dashboard data:', error)
	}
})
</script>

<style scoped lang="scss">
.dashboard-container {
	.card-content {
		display: flex;
		align-items: center;
		.avatar {
			margin-right: 12px;
			width: 56px;
			height: 56px;
			border-radius: 14px 4px 14px 4px;
			display: flex;
			align-items: center;
			justify-content: center;
			font-size: 28px;
			border: 2px solid #333;
			box-shadow: 2px 3px 0 rgba(0,0,0,0.06);
			&.users { background: #f0f4ff; }
			&.like { background: #fff0f3; }
			&.comments { background: #f0faf0; }
			&.smile { background: #fffef0; }
		}
		.info {
			.title {
				font-size: 14px;
				color: #7f8c8d;
				margin-bottom: 4px;
			}
			.number {
				font-size: 24px;
				font-weight: 700;
				color: #2c3e50;
				margin-bottom: 4px;
			}
			.subtitle-title {
				font-size: 12px;
				color: #95a5a6;
			}
		}
	}
	.chart-content {
		padding: 20px;
		height: 300px;
		position: relative;

		canvas {
			width: 100% !important;
			height: 100% !important;
		}

		.consultation-stats {
			display: flex;
			justify-content: space-around;
			margin-bottom: 20px;

			.stat-item {
				text-align: center;

				.stat-label {
					font-size: 12px;
					color: #7f8c8d;
					margin-bottom: 4px;
				}

				.stat-value {
					font-size: 18px;
					font-weight: 600;
					color: #2c3e50;
				}
			}
		}
	}
}
</style>

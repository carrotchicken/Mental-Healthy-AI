/**
 * =============================================================================
 * Vite 构建配置
 * =============================================================================
 * 
 * 关键配置：
 * 1. @ 别名 → 指向 src 目录
 * 2. AutoImport / Components → Element Plus 按需自动导入，无需手动 import
 * 3. proxy → 开发环境将 /api 请求代理到 Spring Boot 后端，解决跨域
 */

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import Icons from 'unplugin-icons/vite'
import IconsResolver from 'unplugin-icons/resolver'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
	resolve: {
		// @ 别名 → src 目录，简化 import 路径
		alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
	},
	plugins: [
		vue(),
		// 自动导入 Vue API（ref/reactive/computed 等无需 import）
		AutoImport({
			imports: ['vue', 'vue-router', 'pinia'],
			resolvers: [
				ElementPlusResolver(),
				IconsResolver({ prefix: 'Icon', enabledCollections: ['ep'] }),
			],
		}),
		// 自动导入 Element Plus 组件（el-button/el-input 等无需 import）
		Components({
			resolvers: [
				ElementPlusResolver(),
				IconsResolver({ enabledCollections: ['ep'] }),
			],
		}),
		Icons({ autoInstall: true }),
	],
	server: {
		proxy: {
			// 开发环境：前端 /api/xxx → 转发到 http://localhost:8081/xxx
			// 解决跨域问题，生产环境用 Nginx
			'/api': {
				target: 'http://localhost:8081',
				changeOrigin: true,
				rewrite: (path) => path.replace(/^\/api/, ''),
			},
		},
	},
})

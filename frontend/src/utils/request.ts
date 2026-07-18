/**
 * =============================================================================
 * Axios 请求封装 + 拦截器
 * =============================================================================
 * 
 * 核心作用：
 * 1. 统一配置 baseURL 和超时
 * 2. 请求拦截器：自动从 localStorage 取 Token 放入请求头
 * 3. 响应拦截器：统一处理返回格式，code='200' 返回 data.data，否则弹窗提示
 * 4. 登录过期自动跳转登录页
 */

import axios from 'axios'
import type { InternalAxiosRequestConfig, AxiosResponse, AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例，baseURL='/api' 配合 Vite proxy 转发到后端 8081
const instance = axios.create({ baseURL: '/api', timeout: 5000 })

// ── 请求拦截器：每次请求前自动加 Token ──
instance.interceptors.request.use(
	(config: InternalAxiosRequestConfig) => {
		const token = localStorage.getItem('token')
		if (token) {
			config.headers['token'] = token
		}
		return config
	},
	error => Promise.reject(error),
)

// ── 响应拦截器：统一处理返回结果 ──
instance.interceptors.response.use(
	(response: AxiosResponse) => {
		const { data, config } = response
		// 成功：后端返回 { code:'200', msg:'操作成功', data:{...} }
		if (data.code === '200') {
			return data.data  // 只返回 data 部分，简化调用方取值
		}
		// 未登录：跳转登录页
		if (data.code === '-1') {
			if (!config.url?.includes('/login')) {
				ElMessage.error(data.msg || '登录状态已过期，请重新登录')
				localStorage.removeItem('token')
				localStorage.removeItem('userInfo')
				window.location.href = '/auth/login'
			}
			return response
		}
		// 业务异常：弹窗提示并 reject，让调用方可以 catch
		ElMessage.error(data.msg || '请求失败，请稍后再试')
		return Promise.reject(new Error(data.msg || '网络请求失败...'))
	},
	error => Promise.reject(error),
)

// ── 封装 GET/POST/PUT/DELETE，返回 Promise，类型安全 ──
const request = {
	post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
		return instance.post(url, data, config) as Promise<T>
	},
	get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
		return instance.get(url, config) as Promise<T>
	},
	put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
		return instance.put(url, data, config) as Promise<T>
	},
	delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
		return instance.delete(url, config) as Promise<T>
	},
}

export default request

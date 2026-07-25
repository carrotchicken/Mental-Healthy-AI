/**
 * 通用防抖函数
 * 使用场景：搜索框实时输入筛选、窗口 resize 事件等高频触发场景
 * @param fn    需要防抖的函数
 * @param delay 延迟时间（毫秒），默认 300ms
 * @returns 包装后的防抖函数
 */
export function debounce<T extends (...args: any[]) => any>(
	ffn: T,
	delay: number = 300,
): (...args: Parameters<T>) => void {
	let timer: ReturnType<typeof setTimeout> | null = null
	return (...args: Parameters<T>) => {
		if (timer) clearTimeout(timer)
		timer = setTimeout(() => ffn(...args), delay)
	}
}

/**
 * 通用节流函数
 * 使用场景：滚动加载、按钮防重复点击
 * @param fn      需要节流的函数
 * @param interval 间隔时间（毫秒），默认 300ms
 * @returns 包装后的节流函数
 */
export function throttle<T extends (...args: any[]) => any>(
	fn: T,
	interval: number = 300,
): (...args: Parameters<T>) => void {
	let lastTime = 0
	return (...args: Parameters<T>) => {
		const now = Date.now()
		if (now - lastTime >= interval) {
			lastTime = now
			fn(...args)
		}
	}
}

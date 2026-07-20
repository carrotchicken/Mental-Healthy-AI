import { createApp } from 'vue'
import '@/styles/index.scss'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import { createPinia } from 'pinia'

const app = createApp(App)

const pinia = createPinia()

Object.entries(ElementPlusIconsVue).forEach(([name, component]) => {
	app.component(name, component)
})

app
	.use(ElementPlus)
	.use(pinia)
	.use(router)
	.mount('#app')

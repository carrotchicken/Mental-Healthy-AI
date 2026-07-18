import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAdminStore = defineStore('admin', () => {
    const isCollapse = ref<boolean>(false)

    const toggleCollapse = (): void => {
        isCollapse.value = !isCollapse.value
    }

    return {
        isCollapse,
        toggleCollapse,
    }
})
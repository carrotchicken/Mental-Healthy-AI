<template>
	<el-form :model="formData">
		<el-row :gutter="24">
			<el-col
				v-for="item in formItemAttr"
				:key="item.prop"
				v-bind="item.col">
				<el-form-item :label="item.label" :prop="item.prop">
					<component
						:is="item.comp"
						v-model="formData[item.prop as keyof SearchFormData]"
						:placeholder="item.placeholder">
						<template v-if="item.comp === 'el-select'">
							<el-option label="全部" value="" />
							<el-option
								v-for="opt in item.options"
								:key="opt.value"
								:label="opt.label"
								:value="opt.value" />
						</template>
					</component>
				</el-form-item>
			</el-col>
		</el-row>
		<el-row>
			<el-button type="primary" @click="$emit('search', formData)">
				查询
			</el-button>
			<el-button @click="((formData = {}), $emit('search', formData))">
				重置
			</el-button>
		</el-row>
	</el-form>
</template>

<script setup lang="ts">
import type { FormItemConfig, SearchFormData } from '@/types/admin'
import { ref, computed } from 'vue'

const props = withDefaults(defineProps<{ formItem: FormItemConfig[] }>(), {
	formItem: () => [],
})

const formData = ref<SearchFormData>({})

const formItemAttr = computed(() =>
	props.formItem.map(item => ({
		...item,
		col: item.col || { xs: 24, sm: 12, md: 8, lg: 6, xl: 6 },
	})),
)
</script>

<style scoped lang="scss"></style>

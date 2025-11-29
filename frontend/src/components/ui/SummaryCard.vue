<template>
  <div class="card">
    <div class="flex items-center">
      <div class="flex-shrink-0">
        <div
          class="flex items-center justify-center h-8 w-8 rounded-md text-white text-lg"
          :class="colorClasses"
        >
          {{ icon }}
        </div>
      </div>
      <div class="ml-5 w-0 flex-1">
        <dl>
          <dt class="text-sm font-medium text-gray-500 truncate">{{ title }}</dt>
          <dd class="flex items-baseline">
            <div class="text-2xl font-semibold text-gray-900">
              {{ formattedValue }}
            </div>
            <div v-if="suffix" class="ml-2 text-sm text-gray-500">
              {{ suffix }}
            </div>
            <div
              v-if="growth !== undefined && growth !== null"
              class="ml-2 flex items-baseline text-sm font-semibold"
              :class="growthColorClass"
            >
              <ArrowUpIcon
                v-if="growth > 0"
                class="flex-shrink-0 self-center h-4 w-4"
              />
              <ArrowDownIcon
                v-else-if="growth < 0"
                class="flex-shrink-0 self-center h-4 w-4"
              />
              <span class="sr-only">{{ growth > 0 ? '증가' : '감소' }}</span>
              {{ Math.abs(growth).toFixed(1) }}%
            </div>
          </dd>
        </dl>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ArrowUpIcon, ArrowDownIcon } from '@heroicons/vue/20/solid'

interface Props {
  title: string
  value: string | number
  suffix?: string
  icon: string
  color: 'blue' | 'green' | 'purple' | 'orange' | 'red'
  growth?: number
}

const props = defineProps<Props>()

const formattedValue = computed(() => {
  if (typeof props.value === 'number') {
    return props.value.toLocaleString('ko-KR')
  }
  return props.value
})

const colorClasses = computed(() => {
  const colorMap = {
    blue: 'bg-blue-500',
    green: 'bg-green-500',
    purple: 'bg-purple-500',
    orange: 'bg-orange-500',
    red: 'bg-red-500',
  }
  return colorMap[props.color]
})

const growthColorClass = computed(() => {
  if (props.growth === undefined || props.growth === null) return ''
  return props.growth > 0 ? 'text-green-600' : 'text-red-600'
})
</script>
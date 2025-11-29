<template>
  <div class="h-80">
    <div v-if="!chartData?.labels?.length" class="flex items-center justify-center h-full text-gray-500">
      데이터가 없습니다
    </div>
    <Bar v-else :data="chartData" :options="chartOptions" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Bar } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
  type ChartData,
  type ChartOptions
} from 'chart.js'
import type { CountryExport } from '@/types'

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
)

interface Props {
  data: CountryExport[]
}

const props = defineProps<Props>()

interface ChartDataItem {
  name: string
  fullName: string
  value: number
  percentage: number
}

const processedData = computed<ChartDataItem[]>(() => {
  return props.data.slice(0, 10).map(item => ({
    name: item.countryCode,
    fullName: item.countryName,
    value: item.totalValue,
    percentage: item.marketShare
  }))
})

const chartData = computed<ChartData<'bar'>>(() => {
  const data = processedData.value

  return {
    labels: data.map(item => item.name),
    datasets: [
      {
        label: '수출액 (USD)',
        data: data.map(item => item.value),
        backgroundColor: '#3b82f6',
        borderColor: '#2563eb',
        borderWidth: 1
      }
    ]
  }
})

const chartOptions = computed<ChartOptions<'bar'>>(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      display: true,
      position: 'top' as const
    },
    tooltip: {
      callbacks: {
        label: (context: any) => {
          const index = context.dataIndex
          const item = processedData.value[index]
          const formatted = new Intl.NumberFormat('ko-KR', {
            style: 'currency',
            currency: 'USD',
            minimumFractionDigits: 0,
            maximumFractionDigits: 0,
          }).format(context.parsed.y)
          return `${item.fullName}: ${formatted} (${item.percentage.toFixed(1)}%)`
        }
      }
    }
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        callback: (value: any) => {
          return new Intl.NumberFormat('ko-KR', {
            notation: 'compact',
            compactDisplay: 'short'
          }).format(value as number)
        }
      }
    }
  }
}))
</script>
<template>
  <div class="h-80">
    <div v-if="!chartData?.labels?.length" class="flex items-center justify-center h-full text-gray-500">
      데이터가 없습니다
    </div>
    <Line v-else :data="chartData" :options="chartOptions" />
  </div>
</template>

<script setup lang="ts">
import { computed, withDefaults } from 'vue'
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  type ChartData,
  type ChartOptions
} from 'chart.js'
import type { MonthlyTrend } from '@/types'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend
)

interface Props {
  data?: MonthlyTrend[]
}

const props = withDefaults(defineProps<Props>(), {
  data: () => []
})

const chartData = computed<ChartData<'line'>>(() => {
  if (!props.data || !Array.isArray(props.data)) {
    return {
      labels: [],
      datasets: []
    }
  }

  const labels = props.data.map(item =>
    item.monthName || (item.month ? `${item.month}월` : '미분류')
  )
  const values = props.data.map(item => item.totalValue || 0)

  return {
    labels,
    datasets: [
      {
        label: '수출액 (USD)',
        data: values,
        borderColor: '#3b82f6',
        backgroundColor: '#3b82f6',
        borderWidth: 2,
        tension: 0.4,
        pointRadius: 4,
        pointHoverRadius: 6
      }
    ]
  }
})

const chartOptions = computed<ChartOptions<'line'>>(() => ({
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
          const value = context.parsed.y
          const formatted = new Intl.NumberFormat('ko-KR', {
            style: 'currency',
            currency: 'USD',
            minimumFractionDigits: 0,
            maximumFractionDigits: 0,
          }).format(value)
          return `수출액: ${formatted}`
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
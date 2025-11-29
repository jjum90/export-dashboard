<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">수출 분석</h1>
        <p class="text-gray-600 mt-1">심화된 수출 통계 분석을 확인하세요</p>
      </div>
    </div>

    <!-- Year Range Selector -->
    <div class="card">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">시작 연도</label>
          <select v-model="startYear" class="select-field">
            <option v-for="year in availableYears" :key="year" :value="year">
              {{ year }}
            </option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">종료 연도</label>
          <select v-model="endYear" class="select-field">
            <option v-for="year in availableYears" :key="year" :value="year">
              {{ year }}
            </option>
          </select>
        </div>
      </div>
      <div class="mt-4">
        <button
          @click="fetchTrendData"
          :disabled="loading"
          class="btn-primary"
        >
          {{ loading ? '분석 중...' : '분석 실행' }}
        </button>
      </div>
    </div>

    <!-- Yearly Trend Chart -->
    <div v-if="yearlyTrendData.length > 0" class="card">
      <h3 class="text-lg font-semibold text-gray-900 mb-4">연도별 수출 추이</h3>
      <div class="h-96">
        <Line :data="chartData" :options="areaChartOptions" />
      </div>
    </div>

    <!-- Statistics Cards -->
    <div v-if="yearlyTrendData.length > 0" class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div class="card">
        <div class="text-center">
          <div class="text-2xl font-bold text-primary-600">
            {{ formatCurrency(averageExport) }}
          </div>
          <div class="text-sm text-gray-600 mt-1">연평균 수출액</div>
        </div>
      </div>
      <div class="card">
        <div class="text-center">
          <div class="text-2xl font-bold text-green-600">
            {{ formatCurrency(maxExport) }}
          </div>
          <div class="text-sm text-gray-600 mt-1">최대 수출액</div>
        </div>
      </div>
      <div class="card">
        <div class="text-center">
          <div class="text-2xl font-bold text-orange-600">
            {{ formatPercentage(averageGrowth) }}
          </div>
          <div class="text-sm text-gray-600 mt-1">연평균 증가율</div>
        </div>
      </div>
    </div>

    <!-- Placeholder for more analysis -->
    <div class="card">
      <h3 class="text-lg font-semibold text-gray-900 mb-4">추가 분석</h3>
      <div class="text-center py-12 text-gray-500">
        <p class="text-lg">더 많은 분석 기능이 곧 추가될 예정입니다</p>
        <p class="text-sm mt-2">국가별/품목별 심화 분석, 예측 모델링 등</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useDashboardStore } from '@/stores/dashboard'
import { exportStatisticApi } from '@/services/api'
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Filler,
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
  Filler,
  Title,
  Tooltip,
  Legend
)

const dashboardStore = useDashboardStore()

const startYear = ref(2020)
const endYear = ref(2023)
const yearlyTrendData = ref<MonthlyTrend[]>([])
const loading = ref(false)

const availableYears = computed(() => dashboardStore.availableYears)

const chartData = computed<ChartData<'line'>>(() => {
  if (yearlyTrendData.value.length === 0) {
    return {
      labels: [],
      datasets: []
    }
  }

  const labels = yearlyTrendData.value.map(item => item.year?.toString() || '')
  const values = yearlyTrendData.value.map(item => item.exportValue || 0)

  return {
    labels,
    datasets: [
      {
        label: '수출액 (USD)',
        data: values,
        borderColor: '#3b82f6',
        backgroundColor: 'rgba(59, 130, 246, 0.3)',
        borderWidth: 2,
        tension: 0.4,
        fill: true,
        pointRadius: 4,
        pointHoverRadius: 6
      }
    ]
  }
})

const areaChartOptions = computed<ChartOptions<'line'>>(() => ({
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

const averageExport = computed(() => {
  if (yearlyTrendData.value.length === 0) return 0
  const total = yearlyTrendData.value.reduce((sum, item) => sum + item.exportValue, 0)
  return total / yearlyTrendData.value.length
})

const maxExport = computed(() => {
  if (yearlyTrendData.value.length === 0) return 0
  return Math.max(...yearlyTrendData.value.map(item => item.exportValue))
})

const averageGrowth = computed(() => {
  if (yearlyTrendData.value.length < 2) return 0

  let totalGrowth = 0
  let growthCount = 0

  for (let i = 1; i < yearlyTrendData.value.length; i++) {
    const prevValue = yearlyTrendData.value[i - 1].exportValue
    const currentValue = yearlyTrendData.value[i].exportValue
    if (prevValue > 0) {
      const growth = ((currentValue - prevValue) / prevValue) * 100
      totalGrowth += growth
      growthCount++
    }
  }

  return growthCount > 0 ? totalGrowth / growthCount : 0
})

const fetchTrendData = async () => {
  if (startYear.value > endYear.value) {
    alert('시작 연도는 종료 연도보다 작거나 같아야 합니다.')
    return
  }

  loading.value = true
  try {
    const response = await exportStatisticApi.getTrend(startYear.value, endYear.value)
    yearlyTrendData.value = response.data.map(item => ({
      ...item,
      value: item.exportValue
    }))
  } catch (error) {
    console.error('Failed to fetch trend data:', error)
  } finally {
    loading.value = false
  }
}

const formatCurrency = (value: number): string => {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(value)
}

const formatPercentage = (value: number): string => {
  return `${value > 0 ? '+' : ''}${value.toFixed(1)}%`
}

onMounted(async () => {
  await dashboardStore.fetchAvailableYears()
  if (availableYears.value.length > 0) {
    endYear.value = availableYears.value[0]
    startYear.value = Math.max(endYear.value - 3, availableYears.value[availableYears.value.length - 1])
  }
})
</script>
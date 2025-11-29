<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">수출 통계 대시보드</h1>
        <p class="text-gray-600 mt-1">{{ selectedYear }}년 대한민국 수출 현황</p>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex justify-center items-center h-64">
      <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4">
      <div class="flex">
        <div class="flex-shrink-0">
          <ExclamationTriangleIcon class="h-5 w-5 text-red-400" />
        </div>
        <div class="ml-3">
          <h3 class="text-sm font-medium text-red-800">오류가 발생했습니다</h3>
          <div class="mt-2 text-sm text-red-700">
            <p>{{ error }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Dashboard Content -->
    <div v-else-if="dashboardData" class="space-y-6">
      <!-- Summary Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <SummaryCard
          title="총 수출액"
          :value="formatCurrency(dashboardData.totalExportValue)"
          :growth="dashboardData.growthRateYoy"
          icon="💰"
          color="blue"
        />
        <SummaryCard
          title="수출 국가"
          :value="dashboardData.totalCountries"
          suffix="개국"
          icon="🌍"
          color="green"
        />
        <SummaryCard
          title="수출 품목"
          :value="dashboardData.totalProducts"
          suffix="개"
          icon="📦"
          color="purple"
        />
        <SummaryCard
          title="전년 대비"
          :value="formatPercentage(dashboardData.yearOverYearGrowth)"
          :growth="dashboardData.yearOverYearGrowth"
          icon="📈"
          color="orange"
        />
      </div>

      <!-- Charts Grid -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Monthly Trend Chart -->
        <div class="card">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">월별 수출 추이</h3>
          <MonthlyTrendChart :data="dashboardData.monthlyTrend" />
        </div>

        <!-- Top Countries Chart -->
        <div class="card">
          <h3 class="text-lg font-semibold text-gray-900 mb-4">주요 수출국</h3>
          <TopCountriesChart :data="dashboardData.topCountries" />
        </div>
      </div>

      <!-- Top Products Chart -->
      <div class="card">
        <h3 class="text-lg font-semibold text-gray-900 mb-4">주요 수출 품목</h3>
        <TopProductsChart :data="dashboardData.topProducts" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useDashboardStore } from '@/stores/dashboard'
import { ExclamationTriangleIcon } from '@heroicons/vue/24/outline'
import SummaryCard from '@/components/ui/SummaryCard.vue'
import MonthlyTrendChart from '@/components/charts/MonthlyTrendChart.vue'
import TopCountriesChart from '@/components/charts/TopCountriesChart.vue'
import TopProductsChart from '@/components/charts/TopProductsChart.vue'

const dashboardStore = useDashboardStore()

const dashboardData = computed(() => dashboardStore.dashboardData)
const selectedYear = computed(() => dashboardStore.selectedYear)
const loading = computed(() => dashboardStore.loading)
const error = computed(() => dashboardStore.error)

const formatCurrency = (value: number | undefined): string => {
  if (!value) return '0'
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(value)
}

const formatPercentage = (value: number | undefined): string => {
  if (value === undefined || value === null) return '0%'
  return `${value > 0 ? '+' : ''}${value.toFixed(1)}%`
}

onMounted(async () => {
  // 먼저 사용 가능한 연도를 불러온 후 대시보드 데이터를 불러옴
  await dashboardStore.fetchAvailableYears()
  // fetchAvailableYears에서 이미 데이터를 불러오므로 조건부 호출
  if (!dashboardStore.dashboardData) {
    await dashboardStore.fetchDashboardData()
  }
})
</script>
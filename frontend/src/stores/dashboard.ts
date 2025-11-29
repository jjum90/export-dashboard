import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { exportStatisticApi } from '@/services/api'
import type { DashboardSummary, MonthlyTrend } from '@/types'

export const useDashboardStore = defineStore('dashboard', () => {
  // State
  const dashboardData = ref<DashboardSummary | null>(null)
  const yearlyTrend = ref<MonthlyTrend[]>([])
  const availableYears = ref<number[]>([])
  const selectedYear = ref<number>(new Date().getFullYear())
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const currentYearData = computed(() => dashboardData.value)
  const topCountries = computed(() => dashboardData.value?.topCountries || [])
  const topProducts = computed(() => dashboardData.value?.topProducts || [])
  const monthlyTrend = computed(() => dashboardData.value?.monthlyTrends || [])

  // Actions
  const fetchDashboardData = async (year: number = selectedYear.value) => {
    loading.value = true
    error.value = null

    try {
      const response = await exportStatisticApi.getDashboard(year)
      dashboardData.value = response.data
      selectedYear.value = year
    } catch (err) {
      error.value = '대시보드 데이터를 불러오는데 실패했습니다.'
      console.error('Failed to fetch dashboard data:', err)
    } finally {
      loading.value = false
    }
  }

  const fetchYearlyTrend = async (startYear: number, endYear: number) => {
    try {
      const response = await exportStatisticApi.getTrend(startYear, endYear)
      yearlyTrend.value = response.data
    } catch (err) {
      console.error('Failed to fetch yearly trend:', err)
    }
  }

  const fetchAvailableYears = async () => {
    try {
      const response = await exportStatisticApi.getYears()
      availableYears.value = response.data.sort((a, b) => b - a)

      if (availableYears.value.length > 0 && !availableYears.value.includes(selectedYear.value)) {
        selectedYear.value = availableYears.value[0]
        // 유효한 연도로 변경되었으므로 데이터를 다시 불러옴
        await fetchDashboardData(selectedYear.value)
      }
    } catch (err) {
      console.error('Failed to fetch available years:', err)
    }
  }

  const setSelectedYear = (year: number) => {
    selectedYear.value = year
    fetchDashboardData(year)
  }

  const refreshData = () => {
    fetchDashboardData(selectedYear.value)
  }

  return {
    // State
    dashboardData,
    yearlyTrend,
    availableYears,
    selectedYear,
    loading,
    error,

    // Getters
    currentYearData,
    topCountries,
    topProducts,
    monthlyTrend,

    // Actions
    fetchDashboardData,
    fetchYearlyTrend,
    fetchAvailableYears,
    setSelectedYear,
    refreshData,
  }
})
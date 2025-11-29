<template>
  <nav class="bg-white shadow-sm border-b border-gray-200">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex justify-between h-16">
        <div class="flex">
          <!-- Logo -->
          <div class="flex-shrink-0 flex items-center">
            <router-link to="/" class="text-xl font-bold text-primary-600">
              🇰🇷 수출통계대시보드
            </router-link>
          </div>

          <!-- Navigation Links -->
          <div class="hidden sm:ml-6 sm:flex sm:space-x-8">
            <router-link
              v-for="item in navItems"
              :key="item.name"
              :to="item.path"
              class="border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300 whitespace-nowrap py-2 px-1 border-b-2 font-medium text-sm transition-colors duration-200"
              active-class="border-primary-500 text-primary-600"
            >
              {{ item.name }}
            </router-link>
          </div>
        </div>

        <!-- Right side -->
        <div class="flex items-center space-x-4">
          <!-- Year Selector -->
          <div class="flex items-center space-x-2">
            <label class="text-sm font-medium text-gray-700">연도:</label>
            <select
              v-model="selectedYear"
              @change="onYearChange"
              class="select-field w-24"
            >
              <option v-for="year in availableYears" :key="year" :value="year">
                {{ year }}
              </option>
            </select>
          </div>

          <!-- Refresh Button -->
          <button
            @click="refreshData"
            class="p-2 text-gray-400 hover:text-gray-600 transition-colors duration-200"
            title="새로고침"
          >
            <ArrowPathIcon class="h-5 w-5" />
          </button>
        </div>

        <!-- Mobile menu button -->
        <div class="sm:hidden flex items-center">
          <button
            @click="mobileMenuOpen = !mobileMenuOpen"
            class="inline-flex items-center justify-center p-2 rounded-md text-gray-400 hover:text-gray-500 hover:bg-gray-100"
          >
            <Bars3Icon v-if="!mobileMenuOpen" class="h-6 w-6" />
            <XMarkIcon v-else class="h-6 w-6" />
          </button>
        </div>
      </div>
    </div>

    <!-- Mobile menu -->
    <div v-show="mobileMenuOpen" class="sm:hidden">
      <div class="pt-2 pb-3 space-y-1">
        <router-link
          v-for="item in navItems"
          :key="item.name"
          :to="item.path"
          class="block pl-3 pr-4 py-2 border-l-4 text-base font-medium transition-colors duration-200"
          :class="[
            $route.path === item.path
              ? 'border-primary-500 text-primary-700 bg-primary-50'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:bg-gray-50 hover:border-gray-300'
          ]"
          @click="mobileMenuOpen = false"
        >
          {{ item.name }}
        </router-link>
      </div>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useDashboardStore } from '@/stores/dashboard'
import { Bars3Icon, XMarkIcon, ArrowPathIcon } from '@heroicons/vue/24/outline'

const dashboardStore = useDashboardStore()

const mobileMenuOpen = ref(false)
const selectedYear = ref(new Date().getFullYear())

const navItems = [
  { name: '대시보드', path: '/dashboard' },
  { name: '국가별 통계', path: '/countries' },
  { name: '품목별 통계', path: '/products' },
  { name: '분석', path: '/analytics' },
]

const availableYears = ref<number[]>([2022, 2023, 2024, 2025])

const onYearChange = () => {
  dashboardStore.setSelectedYear(selectedYear.value)
}

const refreshData = () => {
  dashboardStore.refreshData()
}

onMounted(async () => {
  await dashboardStore.fetchAvailableYears()
  availableYears.value = dashboardStore.availableYears
  selectedYear.value = dashboardStore.selectedYear
})
</script>
<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">국가별 수출 통계</h1>
        <p class="text-gray-600 mt-1">국가별 수출 현황을 확인하세요</p>
      </div>
    </div>

    <!-- Filters -->
    <div class="card">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">지역 필터</label>
          <select v-model="selectedRegion" class="select-field">
            <option value="">전체 지역</option>
            <option v-for="region in regions" :key="region" :value="region">
              {{ region }}
            </option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">대륙 필터</label>
          <select v-model="selectedContinent" class="select-field">
            <option value="">전체 대륙</option>
            <option v-for="continent in continents" :key="continent" :value="continent">
              {{ continent }}
            </option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">국가 검색</label>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="국가명 또는 코드 검색"
            class="input-field"
          />
        </div>
      </div>
    </div>

    <!-- Countries List -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="country in filteredCountries"
        :key="country.id"
        class="card hover:shadow-md transition-shadow duration-200 cursor-pointer"
        @click="selectCountry(country)"
      >
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-lg font-semibold text-gray-900">
              {{ country.countryNameKo }}
            </h3>
            <p class="text-sm text-gray-600">{{ country.countryNameEn }}</p>
            <p class="text-xs text-gray-500 mt-1">{{ country.countryCode }}</p>
          </div>
          <div class="text-right">
            <p class="text-sm text-gray-600">{{ country.region }}</p>
            <p class="text-xs text-gray-500">{{ country.continent }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div
      v-if="filteredCountries.length === 0"
      class="text-center py-12"
    >
      <div class="text-gray-500">
        <p class="text-lg">검색 결과가 없습니다</p>
        <p class="text-sm mt-2">다른 검색어나 필터를 시도해보세요</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { countryApi } from '@/services/api'
import type { Country } from '@/types'

const countries = ref<Country[]>([])
const regions = ref<string[]>([])
const continents = ref<string[]>([])
const selectedRegion = ref('')
const selectedContinent = ref('')
const searchKeyword = ref('')

const filteredCountries = computed(() => {
  let filtered = countries.value

  if (selectedRegion.value) {
    filtered = filtered.filter(country => country.region === selectedRegion.value)
  }

  if (selectedContinent.value) {
    filtered = filtered.filter(country => country.continent === selectedContinent.value)
  }

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(country =>
      country.countryNameKo.toLowerCase().includes(keyword) ||
      country.countryNameEn.toLowerCase().includes(keyword) ||
      country.countryCode.toLowerCase().includes(keyword)
    )
  }

  return filtered.sort((a, b) => a.countryNameKo.localeCompare(b.countryNameKo))
})

const selectCountry = (country: Country) => {
  // Navigate to country detail or show more info
  console.log('Selected country:', country)
}

onMounted(async () => {
  try {
    const [countriesRes, regionsRes, continentsRes] = await Promise.all([
      countryApi.getAll(),
      countryApi.getRegions(),
      countryApi.getContinents()
    ])

    countries.value = countriesRes.data
    regions.value = regionsRes.data
    continents.value = continentsRes.data
  } catch (error) {
    console.error('Failed to fetch countries data:', error)
  }
})
</script>
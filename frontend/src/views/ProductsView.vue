<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">품목별 수출 통계</h1>
        <p class="text-gray-600 mt-1">HS코드별 수출 현황을 확인하세요</p>
      </div>
    </div>

    <!-- Filters -->
    <div class="card">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">HS코드 레벨</label>
          <select v-model="selectedLevel" class="select-field">
            <option value="">전체 레벨</option>
            <option v-for="level in hsLevels" :key="level" :value="level">
              {{ level }}자리
            </option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">상위 카테고리</label>
          <select v-model="selectedParent" class="select-field">
            <option value="">전체 카테고리</option>
            <option v-for="category in mainCategories" :key="category.hsCode" :value="category.hsCode">
              {{ category.hsCode }} - {{ category.categoryNameKo }}
            </option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">품목 검색</label>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="품목명 또는 HS코드 검색"
            class="input-field"
          />
        </div>
      </div>
    </div>

    <!-- Products List -->
    <div class="grid grid-cols-1 gap-4">
      <div
        v-for="product in filteredProducts"
        :key="product.id"
        class="card hover:shadow-md transition-shadow duration-200 cursor-pointer"
        @click="selectProduct(product)"
      >
        <div class="flex items-center justify-between">
          <div class="flex-1">
            <div class="flex items-center space-x-3">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                {{ product.hsCode }}
              </span>
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                {{ product.hsLevel }}자리
              </span>
            </div>
            <h3 class="text-lg font-semibold text-gray-900 mt-2">
              {{ product.categoryNameKo }}
            </h3>
            <p class="text-sm text-gray-600 mt-1">{{ product.categoryNameEn }}</p>
            <p v-if="product.description" class="text-xs text-gray-500 mt-2 line-clamp-2">
              {{ product.description }}
            </p>
          </div>
          <div class="flex-shrink-0 ml-4">
            <button class="btn-outline text-sm">
              상세보기
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div
      v-if="filteredProducts.length === 0"
      class="text-center py-12"
    >
      <div class="text-gray-500">
        <p class="text-lg">검색 결과가 없습니다</p>
        <p class="text-sm mt-2">다른 검색어나 필터를 시도해보세요</p>
      </div>
    </div>

    <!-- Pagination -->
    <div
      v-if="filteredProducts.length > 0"
      class="flex items-center justify-between border-t border-gray-200 bg-white px-4 py-3 sm:px-6"
    >
      <div class="flex flex-1 justify-between sm:hidden">
        <button
          :disabled="currentPage === 1"
          @click="goToPage(currentPage - 1)"
          class="relative inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
        >
          이전
        </button>
        <button
          :disabled="currentPage === totalPages"
          @click="goToPage(currentPage + 1)"
          class="relative ml-3 inline-flex items-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50"
        >
          다음
        </button>
      </div>
      <div class="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
        <div>
          <p class="text-sm text-gray-700">
            총 <span class="font-medium">{{ totalItems }}</span>개 중
            <span class="font-medium">{{ startItem }}</span>-<span class="font-medium">{{ endItem }}</span>개 표시
          </p>
        </div>
        <div>
          <nav class="isolate inline-flex -space-x-px rounded-md shadow-sm" aria-label="Pagination">
            <button
              :disabled="currentPage === 1"
              @click="goToPage(currentPage - 1)"
              class="relative inline-flex items-center rounded-l-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0"
            >
              이전
            </button>
            <button
              v-for="page in visiblePages"
              :key="page"
              @click="goToPage(page)"
              class="relative inline-flex items-center px-4 py-2 text-sm font-semibold"
              :class="[
                page === currentPage
                  ? 'bg-primary-600 text-white focus:z-20 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600'
                  : 'text-gray-900 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0'
              ]"
            >
              {{ page }}
            </button>
            <button
              :disabled="currentPage === totalPages"
              @click="goToPage(currentPage + 1)"
              class="relative inline-flex items-center rounded-r-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0"
            >
              다음
            </button>
          </nav>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { productCategoryApi } from '@/services/api'
import type { ProductCategory } from '@/types'

const products = ref<ProductCategory[]>([])
const mainCategories = ref<ProductCategory[]>([])
const hsLevels = ref<number[]>([])
const selectedLevel = ref<number | ''>('')
const selectedParent = ref('')
const searchKeyword = ref('')

// Pagination
const currentPage = ref(1)
const itemsPerPage = 20

const filteredProducts = computed(() => {
  let filtered = products.value

  if (selectedLevel.value) {
    filtered = filtered.filter(product => product.hsLevel === selectedLevel.value)
  }

  if (selectedParent.value) {
    filtered = filtered.filter(product => product.parentHsCode === selectedParent.value)
  }

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(product =>
      product.categoryNameKo.toLowerCase().includes(keyword) ||
      product.categoryNameEn.toLowerCase().includes(keyword) ||
      product.hsCode.toLowerCase().includes(keyword)
    )
  }

  // Sort by HS code
  filtered = filtered.sort((a, b) => a.hsCode.localeCompare(b.hsCode))

  // Pagination
  const startIndex = (currentPage.value - 1) * itemsPerPage
  const endIndex = startIndex + itemsPerPage
  return filtered.slice(startIndex, endIndex)
})

const totalItems = computed(() => {
  let filtered = products.value

  if (selectedLevel.value) {
    filtered = filtered.filter(product => product.hsLevel === selectedLevel.value)
  }

  if (selectedParent.value) {
    filtered = filtered.filter(product => product.parentHsCode === selectedParent.value)
  }

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(product =>
      product.categoryNameKo.toLowerCase().includes(keyword) ||
      product.categoryNameEn.toLowerCase().includes(keyword) ||
      product.hsCode.toLowerCase().includes(keyword)
    )
  }

  return filtered.length
})

const totalPages = computed(() => Math.ceil(totalItems.value / itemsPerPage))
const startItem = computed(() => (currentPage.value - 1) * itemsPerPage + 1)
const endItem = computed(() => Math.min(currentPage.value * itemsPerPage, totalItems.value))

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, currentPage.value + 2)

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

const goToPage = (page: number) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

const selectProduct = (product: ProductCategory) => {
  console.log('Selected product:', product)
}

// Reset pagination when filters change
watch([selectedLevel, selectedParent, searchKeyword], () => {
  currentPage.value = 1
})

onMounted(async () => {
  try {
    const [productsRes, mainCategoriesRes, levelsRes] = await Promise.all([
      productCategoryApi.getAll(),
      productCategoryApi.getMain(),
      productCategoryApi.getLevels()
    ])

    products.value = productsRes.data
    mainCategories.value = mainCategoriesRes.data
    hsLevels.value = levelsRes.data
  } catch (error) {
    console.error('Failed to fetch products data:', error)
  }
})
</script>
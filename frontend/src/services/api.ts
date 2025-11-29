import axios from 'axios'
import type {
  Country,
  ProductCategory,
  ExportStatistic,
  DashboardSummary,
  MonthlyTrend,
  PaginatedResponse
} from '@/types'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  }
})

// Request interceptor
api.interceptors.request.use(
  (config) => {
    // Add auth token if needed
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Response interceptor
api.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export const countryApi = {
  getAll: () => api.get<Country[]>('/countries'),
  getById: (id: number) => api.get<Country>(`/countries/${id}`),
  getByCode: (code: string) => api.get<Country>(`/countries/code/${code}`),
  getByRegion: (region: string) => api.get<Country[]>(`/countries/region/${region}`),
  getByContinent: (continent: string) => api.get<Country[]>(`/countries/continent/${continent}`),
  search: (keyword: string) => api.get<Country[]>(`/countries/search?keyword=${keyword}`),
  getRegions: () => api.get<string[]>('/countries/regions'),
  getContinents: () => api.get<string[]>('/countries/continents'),
}

export const productCategoryApi = {
  getAll: () => api.get<ProductCategory[]>('/product-categories'),
  getById: (id: number) => api.get<ProductCategory>(`/product-categories/${id}`),
  getByHsCode: (hsCode: string) => api.get<ProductCategory>(`/product-categories/hs-code/${hsCode}`),
  getByLevel: (level: number) => api.get<ProductCategory[]>(`/product-categories/level/${level}`),
  getByParent: (parentCode: string) => api.get<ProductCategory[]>(`/product-categories/parent/${parentCode}`),
  search: (keyword: string) => api.get<ProductCategory[]>(`/product-categories/search?keyword=${keyword}`),
  getMain: () => api.get<ProductCategory[]>('/product-categories/main'),
  getLevels: () => api.get<number[]>('/product-categories/levels'),
}

export const exportStatisticApi = {
  getAll: (params?: {
    page?: number
    size?: number
    sortBy?: string
    sortDir?: string
  }) => {
    const searchParams = new URLSearchParams()
    if (params?.page !== undefined) searchParams.append('page', params.page.toString())
    if (params?.size !== undefined) searchParams.append('size', params.size.toString())
    if (params?.sortBy) searchParams.append('sortBy', params.sortBy)
    if (params?.sortDir) searchParams.append('sortDir', params.sortDir)

    return api.get<PaginatedResponse<ExportStatistic>>(`/export-statistics?${searchParams}`)
  },
  getByYear: (year: number) => api.get<ExportStatistic[]>(`/export-statistics/year/${year}`),
  getByCountryAndYear: (countryId: number, year: number) =>
    api.get<ExportStatistic[]>(`/export-statistics/country/${countryId}/year/${year}`),
  getByProductAndYear: (productCategoryId: number, year: number) =>
    api.get<ExportStatistic[]>(`/export-statistics/product/${productCategoryId}/year/${year}`),
  getDashboard: (year: number) => api.get<DashboardSummary>(`/export-statistics/dashboard/${year}`),
  getTrend: (startYear: number, endYear: number) =>
    api.get<MonthlyTrend[]>(`/export-statistics/trend?startYear=${startYear}&endYear=${endYear}`),
  getYears: () => api.get<number[]>('/export-statistics/years'),
}

export default api
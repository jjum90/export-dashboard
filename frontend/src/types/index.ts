export interface Country {
  id: number
  countryCode: string
  countryNameKo: string
  countryNameEn: string
  region?: string
  continent?: string
  isActive: boolean
}

export interface ProductCategory {
  id: number
  hsCode: string
  hsLevel: number
  categoryNameKo: string
  categoryNameEn: string
  parentHsCode?: string
  description?: string
  isActive: boolean
}

export interface ExportStatistic {
  id: number
  country: Country
  productCategory: ProductCategory
  year: number
  month: number
  exportValueUsd: number
  exportWeightKg?: number
  exportQuantity?: number
  quantityUnit?: string
  growthRateYoy?: number
  marketShare?: number
}

export interface CountryExport {
  countryCode: string
  countryName: string
  totalValue: number
  marketShare: number
}

export interface ProductExport {
  hsCode: string
  productName: string
  totalValue: number
  marketShare: number
}

export interface MonthlyTrend {
  year: number
  month?: number
  monthName?: string
  totalValue: number
  growthRate?: number
}

export interface DashboardSummary {
  year: number
  totalExportValue: number
  currency: string
  yearOverYearGrowth?: number
  totalCountries: number
  totalProducts: number
  topCountries: CountryExport[]
  topProducts: ProductExport[]
  monthlyTrends: MonthlyTrend[]
}

export interface ApiResponse<T> {
  data: T
  status: number
  message?: string
}

export interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

export interface ChartData {
  name: string
  value: number
  label?: string
  color?: string
}
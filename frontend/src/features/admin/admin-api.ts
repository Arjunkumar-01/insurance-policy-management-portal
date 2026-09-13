import { apiClient, type ApiResponse } from '../../api/client'

export type AdminOverview = { totalCustomers: number; totalProducts: number; totalPolicies: number; totalClaims: number; totalPayments: number }
export type AdminReport = { premiumCollection: number; claimsRatio: number; productPerformance: { productName: string; policyCount: number }[]; monthlyRevenue: { month: string; revenue: number }[] }
export type AdminCustomer = { id: number; firstName: string; lastName: string; email: string; username: string; role: string; enabled: boolean }
export type AdminPolicy = { id: number; policyNumber: string; customerUsername: string; productName: string; endDate: string; renewalPremium: number; status: string }
export type Page<T> = { content: T[]; totalElements: number; totalPages: number; number: number; size: number }
async function unwrap<T>(request: Promise<{ data: ApiResponse<T> }>) { const response = await request; if (!response.data.success) throw new Error(response.data.message); return response.data.data }
export const adminApi = {
  overview: () => unwrap(apiClient.get<ApiResponse<AdminOverview>>('/api/admin/overview')),
  report: () => unwrap(apiClient.get<ApiResponse<AdminReport>>('/api/reports/admin')),
  customers: () => unwrap(apiClient.get<ApiResponse<Page<AdminCustomer>>>('/api/customers', { params: { page: 0, size: 20, sortBy: 'createdAt', direction: 'DESC' } })),
  policies: () => unwrap(apiClient.get<ApiResponse<Page<AdminPolicy>>>('/api/policies', { params: { page: 0, size: 20, sortBy: 'createdAt', direction: 'DESC' } })),
}
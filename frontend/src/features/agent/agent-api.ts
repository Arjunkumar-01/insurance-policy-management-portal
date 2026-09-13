import { apiClient, type ApiResponse } from '../../api/client'

export type Customer = { id: number; firstName: string; lastName: string; username: string; email: string; role: string; enabled: boolean }
export type Page<T> = { content: T[]; totalElements: number; totalPages: number; number: number; size: number }
export type PolicyStatus = 'PENDING' | 'ACTIVE' | 'EXPIRED' | 'RENEWED' | 'CANCEL_REQUESTED' | 'CANCELLED'
export type Policy = { id: number; policyNumber?: string; customerUsername: string; productName: string; endDate: string; renewalPremium: number; policyStatus?: PolicyStatus; status?: PolicyStatus }
export type ClaimStatus = 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED' | 'SETTLED'
export type Claim = { id: number; status?: ClaimStatus; claimStatus?: ClaimStatus }
export type Payment = { id: number; amount: number; paymentStatus?: string; status?: string }
export type CustomerPortfolio = Customer & { policies: Policy[]; policyCount: number; activePolicyCount: number; openClaimCount: number; policyStatus: PolicyStatus | 'UNKNOWN'; monthlyPremium: number }

async function unwrap<T>(request: Promise<{ data: ApiResponse<T> }>) { const response = await request; if (!response.data.success) throw new Error(response.data.message); return response.data.data }

export const agentApi = {
  getCustomers: (query = '', page = 0, size = 20) => unwrap(apiClient.get<ApiResponse<Page<Customer>>>('/api/agents/customers', { params: { query: query || undefined, role: 'CUSTOMER', enabled: true, page, size } })),
  getCustomerPoliciesByUsername: (username: string) => unwrap(apiClient.get<ApiResponse<Policy[]>>(`/api/agents/customers/${encodeURIComponent(username)}/policies`)),
  getCustomerPolicies: (customerId: number) => unwrap(apiClient.get<ApiResponse<Page<Policy>>>('/api/policies', { params: { customerId, page: 0, size: 100 } })),
  getCustomerClaims: (customerId: number) => unwrap(apiClient.get<ApiResponse<Page<Claim>>>('/api/claims', { params: { customerId, page: 0, size: 100 } })),
  getCustomerPayments: (customerId: number) => unwrap(apiClient.get<ApiResponse<Page<Payment>>>(`/api/payments/customer/${customerId}`, { params: { page: 0, size: 100 } })),
}

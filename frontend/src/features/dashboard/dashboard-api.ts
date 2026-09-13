import { apiClient, type ApiResponse } from '../../api/client'

export type PolicyStatus = 'ACTIVE' | 'EXPIRED' | 'CANCEL_REQUESTED' | 'CANCELLED'
export type ClaimStatus = 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED' | 'SETTLED'
export type PaymentStatus = 'SUCCESS' | 'FAILED' | 'PENDING'

export type CustomerReport = { activePolicies: number; expiredPolicies: number; claimsSubmitted: number }
export type Policy = { id: number; policyNumber: string; customerUsername: string; productName: string; nomineeName: string; endDate: string; renewalPremium: number; status: PolicyStatus }
export type Claim = { id: number; claimNumber: string; policyNumber: string; incidentDate: string; description: string; status: ClaimStatus }
export type Payment = { id: number; paymentReference: string; policyNumber: string; amount: number; paymentDate: string; status: PaymentStatus; receiptNumber: string }

async function getData<T>(url: string): Promise<T> {
  const response = await apiClient.get<ApiResponse<T>>(url)
  if (!response.data.success) throw new Error(response.data.message)
  return response.data.data
}

export const dashboardApi = {
  getReport: () => getData<CustomerReport>('/api/reports/customer'),
  getPolicies: () => getData<Policy[]>('/api/policies/me'),
  getClaims: () => getData<Claim[]>('/api/claims/me'),
  getPayments: () => getData<Payment[]>('/api/payments/history'),
}
import { useQuery } from '@tanstack/react-query'
import { dashboardApi } from './dashboard-api'

export function useDashboard() {
  return useQuery({
    queryKey: ['customer-dashboard'],
    queryFn: async () => {
      const [report, policies, claims, payments] = await Promise.all([
        dashboardApi.getReport(), dashboardApi.getPolicies(), dashboardApi.getClaims(), dashboardApi.getPayments(),
      ])
      return { report, policies, claims, payments }
    },
  })
}
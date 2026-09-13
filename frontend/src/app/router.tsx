import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { ProtectedRoute, PublicOnlyRoute } from '../auth/auth'
import { defaultRoute, useAuth } from '../auth/auth-core'
import { LoginPage } from '../auth/login-page'
import { RegisterPage } from '../auth/register-page'
import { UnauthorizedPage } from '../auth/unauthorized-page'
import { CustomerDashboard } from '../features/dashboard/customer-dashboard'
import { AgentDashboard } from '../features/agent/agent-dashboard'
import { AgentCustomerManagementPage } from '../features/agent/agent-customer-management'
import { AdminDashboard, AdminManagementPage } from '../features/admin/admin-dashboard'
import { ProductWorkspace } from '../features/products/product-workspace'
import { PolicyDetailsPage, PolicyListPage, PurchasePolicyPage, RenewalCenterPage } from '../features/policies/policy-workspace'
import { ClaimDetailsPage, ClaimsDashboardPage, ClaimsListPage, ClaimsWorklistPage, SubmitClaimPage } from '../features/claims/claims-workspace'
import { ClaimsOfficerLayout } from '../features/claims-officer/claims-officer-layout'
import { MakePaymentPage, PaymentDashboardPage, PaymentDetailsPage, PaymentHistoryPage, ReceiptPage } from '../features/payments/payment-workspace'
import { ProfilePage } from '../features/profile/profile-page'
import { CreateUserPage, UserDetailsPage, UserListPage } from '../features/users/user-management'
import { CancellationPage } from '../features/policies/cancellation-page'

export function AppRouter() {
  return <BrowserRouter><Routes>
    <Route element={<PublicOnlyRoute />}><Route path="/login" element={<LoginPage />} /><Route path="/register" element={<RegisterPage />} /></Route>
    <Route element={<ProtectedRoute roles={['CUSTOMER']} />}><Route path="/customer/dashboard" element={<CustomerDashboard />} /></Route>
    <Route element={<ProtectedRoute roles={['CUSTOMER']} />}><Route path="/profile" element={<ProfilePage />} /></Route>
    <Route element={<ProtectedRoute roles={['CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'ADMIN']} />}>
      <Route element={<ClaimsOfficerLayout />}>
        <Route path="/claims" element={<ClaimsListPage />} />
        <Route path="/claims/dashboard" element={<ClaimsDashboardPage />} />
        <Route path="/claims/list" element={<ClaimsListPage />} />
        <Route path="/claims/:id" element={<ClaimDetailsPage />} />
        <Route element={<ProtectedRoute roles={['CLAIMS_OFFICER', 'ADMIN']} />}>
          <Route path="/claims/queue" element={<ClaimsWorklistPage worklist="queue" />} />
          <Route path="/claims/approvals" element={<ClaimsWorklistPage worklist="approvals" />} />
          <Route path="/claims/settlements" element={<ClaimsWorklistPage worklist="settlements" />} />
        </Route>
      </Route>
    </Route>
    <Route element={<ProtectedRoute roles={['CUSTOMER']} />}><Route path="/claims/new" element={<SubmitClaimPage />} /></Route>
    <Route element={<ProtectedRoute roles={['CUSTOMER', 'ADMIN']} />}><Route path="/payments/dashboard" element={<PaymentDashboardPage />} /><Route path="/payments/history" element={<PaymentHistoryPage />} /></Route>
    <Route element={<ProtectedRoute roles={['CUSTOMER']} />}><Route path="/payments/new" element={<MakePaymentPage />} /></Route>
    <Route element={<ProtectedRoute roles={['CUSTOMER', 'AGENT', 'ADMIN', 'CLAIMS_OFFICER']} />}><Route path="/payments/:id" element={<PaymentDetailsPage />} /></Route>
    <Route element={<ProtectedRoute roles={['CUSTOMER', 'AGENT', 'ADMIN']} />}><Route path="/payments/:id/receipt" element={<ReceiptPage />} /></Route>
    <Route element={<ProtectedRoute roles={['CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'ADMIN']} />}><Route path="/policies" element={<PolicyListPage />} /><Route path="/policies/:id" element={<PolicyDetailsPage />} /></Route>
    <Route element={<ProtectedRoute roles={['CUSTOMER', 'AGENT', 'ADMIN']} />}><Route path="/policies/purchase" element={<PurchasePolicyPage />} /><Route path="/policies/renewals" element={<RenewalCenterPage />} /></Route>
    <Route element={<ProtectedRoute roles={['CUSTOMER', 'AGENT', 'CLAIMS_OFFICER', 'ADMIN']} />}><Route path="/products" element={<ProductWorkspace />} /></Route>
    <Route element={<ProtectedRoute roles={['AGENT']} />}><Route path="/agent/customers" element={<AgentCustomerManagementPage />} /></Route>
    <Route element={<ProtectedRoute roles={['AGENT']} />}><Route path="/agent/dashboard" element={<AgentDashboard />} /></Route>
    <Route element={<ProtectedRoute roles={['ADMIN']} />}><Route path="/admin/dashboard" element={<AdminDashboard />} /></Route>
    <Route element={<ProtectedRoute roles={['ADMIN']} />}><Route path="/admin/users" element={<UserListPage />} /><Route path="/admin/users/new" element={<CreateUserPage />} /><Route path="/admin/users/:id" element={<UserDetailsPage />} /></Route>
    <Route element={<ProtectedRoute roles={['ADMIN']} />}><Route path="/admin/policies" element={<AdminManagementPage section="policies" />} /></Route>
    <Route element={<ProtectedRoute roles={['ADMIN']} />}><Route path="/admin/policies/cancellations" element={<CancellationPage />} /></Route>
    <Route element={<ProtectedRoute roles={['ADMIN']} />}><Route path="/admin/claims" element={<AdminManagementPage section="claims" />} /></Route>
    <Route element={<ProtectedRoute roles={['ADMIN']} />}><Route path="/admin/payments" element={<AdminManagementPage section="payments" />} /></Route>
    <Route element={<ProtectedRoute roles={['ADMIN']} />}><Route path="/admin/reports" element={<AdminManagementPage section="reports" />} /></Route>
    <Route path="/unauthorized" element={<UnauthorizedPage />} />
    <Route path="/" element={<RoleHomeRedirect />} />
    <Route path="*" element={<RoleHomeRedirect />} />
  </Routes></BrowserRouter>
}

function RoleHomeRedirect() {
  const { session } = useAuth()
  return <Navigate to={session ? defaultRoute(session.role) : '/login'} replace />
}
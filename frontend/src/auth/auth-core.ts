import { createContext, useContext } from 'react'

export type Role = 'CUSTOMER' | 'AGENT' | 'CLAIMS_OFFICER' | 'ADMIN'
export type AuthSession = { accessToken: string; tokenType: string; username: string; fullName: string; role: Role; expiresAt: number }
export type LoginResponse = Omit<AuthSession, 'expiresAt' | 'role'> & { role: string }
export type AuthContextValue = { session: AuthSession | null; login: (response: LoginResponse) => AuthSession | null; logout: () => void }

export const STORAGE_KEY = 'ipmp.auth.session'
export const AuthContext = createContext<AuthContextValue | null>(null)

function getExpiry(token: string) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))) as { exp?: number }
    return payload.exp ? payload.exp * 1000 : Date.now()
  } catch { return Date.now() }
}

export function normalizeRole(role: string | null | undefined): Role | null {
  const normalized = role?.replace(/^ROLE_/, '').toUpperCase()
  return normalized === 'CUSTOMER' || normalized === 'AGENT' || normalized === 'CLAIMS_OFFICER' || normalized === 'ADMIN'
    ? normalized
    : null
}

export function defaultRoute(role: Role) {
  return ({ CUSTOMER: '/customer/dashboard', AGENT: '/agent/dashboard', CLAIMS_OFFICER: '/claims/dashboard', ADMIN: '/admin/dashboard' })[role]
}

export function readSession(): AuthSession | null {
  try {
    const session = JSON.parse(sessionStorage.getItem(STORAGE_KEY) ?? 'null') as (Omit<AuthSession, 'role'> & { role?: string }) | null
    const role = normalizeRole(session?.role)
    return session && role && session.expiresAt > Date.now() ? { ...session, role } : null
  } catch { return null }
}

export function createSession(response: LoginResponse): AuthSession | null {
  const role = normalizeRole(response.role)
  return role ? { ...response, role, expiresAt: getExpiry(response.accessToken) } : null
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within AuthProvider')
  return context
}
import { useEffect, useState, type ReactNode } from 'react'
import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { AuthContext, createSession, defaultRoute, readSession, STORAGE_KEY, type AuthSession, type LoginResponse, type Role, useAuth } from './auth-core'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(readSession)
  const logout = () => {
    sessionStorage.removeItem(STORAGE_KEY)
    setSession(null)
  }
  const login = (response: LoginResponse) => {
    const nextSession = createSession(response)
    if (!nextSession) return null
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(nextSession))
    setSession(nextSession)
    return nextSession
  }
  useEffect(() => {
    if (!session) return
    const timeout = window.setTimeout(logout, Math.max(0, session.expiresAt - Date.now()))
    return () => window.clearTimeout(timeout)
  }, [session])
  return <AuthContext.Provider value={{ session, login, logout }}>{children}</AuthContext.Provider>
}

export function ProtectedRoute({ roles }: { roles: Role[] }) {
  const { session } = useAuth()
  const location = useLocation()
  if (!session) {
    return <Navigate to="/login" replace state={{ from: location, expired: Boolean(sessionStorage.getItem(STORAGE_KEY)) }} />
  }
  if (!roles.includes(session.role)) {
    return <Navigate to="/unauthorized" replace />
  }
  return <Outlet />
}

export function PublicOnlyRoute() {
  const { session } = useAuth()
  if (session) {
    return <Navigate to={defaultRoute(session.role)} replace />
  }
  return <Outlet />
}
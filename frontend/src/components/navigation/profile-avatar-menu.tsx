import { useQuery } from '@tanstack/react-query'
import { LogOut, UserRound } from 'lucide-react'
import { useCallback, useEffect, useRef, useState } from 'react'
import { useAuth } from '../../auth/auth-core'
import { apiClient, type ApiResponse } from '../../api/client'
import './profile-avatar-menu.css'

type Profile = { firstName: string; lastName: string; email: string; role: string }

const roleLabels: Record<string, string> = {
  CUSTOMER: 'Customer',
  AGENT: 'Agent',
  CLAIMS_OFFICER: 'Claims Officer',
  ADMIN: 'Administrator',
}

async function getProfile() {
  const response = await apiClient.get<ApiResponse<Profile>>('/api/customers/me')
  if (!response.data.success) throw new Error(response.data.message)
  return response.data.data
}

export function ProfileAvatarMenu() {
  const { session, logout } = useAuth()
  const [open, setOpen] = useState(false)
  const [pinned, setPinned] = useState(false)
  const rootRef = useRef<HTMLDivElement>(null)
  const triggerRef = useRef<HTMLButtonElement>(null)
  const openTimerRef = useRef<ReturnType<typeof window.setTimeout> | null>(null)
  const closeTimerRef = useRef<ReturnType<typeof window.setTimeout> | null>(null)
  const profile = useQuery({ queryKey: ['auth', 'profile'], queryFn: getProfile, enabled: open })
  const fullName = profile.data ? `${profile.data.firstName} ${profile.data.lastName}` : session?.fullName ?? ''
  const nameParts = fullName.trim().split(/\s+/).filter(Boolean)
  const initials = (nameParts.length > 1
    ? `${nameParts[0][0]}${nameParts[nameParts.length - 1][0]}`
    : nameParts[0]?.slice(0, 2) ?? ''
  ).toUpperCase() || '--'
  const role = roleLabels[session?.role ?? ''] ?? session?.role ?? 'Authenticated user'

  const clearTimers = useCallback(() => {
    if (openTimerRef.current) window.clearTimeout(openTimerRef.current)
    if (closeTimerRef.current) window.clearTimeout(closeTimerRef.current)
    openTimerRef.current = null
    closeTimerRef.current = null
  }, [])

  const close = useCallback((restoreFocus = false) => {
    clearTimers()
    setOpen(false)
    setPinned(false)
    if (restoreFocus) window.requestAnimationFrame(() => triggerRef.current?.focus())
  }, [clearTimers])

  const scheduleOpen = () => {
    if (closeTimerRef.current) window.clearTimeout(closeTimerRef.current)
    if (open || openTimerRef.current) return
    openTimerRef.current = window.setTimeout(() => {
      setOpen(true)
      openTimerRef.current = null
    }, 200)
  }

  const scheduleClose = () => {
    if (openTimerRef.current) window.clearTimeout(openTimerRef.current)
    openTimerRef.current = null
    if (pinned || !open || closeTimerRef.current) return
    closeTimerRef.current = window.setTimeout(() => {
      setOpen(false)
      closeTimerRef.current = null
    }, 400)
  }

  useEffect(() => {
    if (!open) return
    const onPointerDown = (event: MouseEvent) => {
      if (!rootRef.current?.contains(event.target as Node)) close(true)
    }
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') close(true)
    }
    document.addEventListener('mousedown', onPointerDown)
    document.addEventListener('keydown', onKeyDown)
    return () => {
      document.removeEventListener('mousedown', onPointerDown)
      document.removeEventListener('keydown', onKeyDown)
    }
  }, [close, open])

  useEffect(() => () => clearTimers(), [clearTimers])

  return <div className="profile-menu" ref={rootRef} onMouseEnter={scheduleOpen} onMouseLeave={scheduleClose}>
    <button ref={triggerRef} type="button" className="avatar avatar-trigger" aria-label="Open profile menu" aria-expanded={open} aria-haspopup="dialog" onClick={() => { clearTimers(); if (open) close(); else { setPinned(true); setOpen(true) } }}>{initials}</button>
    {open && <section className="profile-popover" role="dialog" aria-label="User profile">
      <div className="profile-popover-heading"><span className="profile-avatar-large">{initials}</span><div><b>{fullName || 'Loading profile...'}</b><span className="profile-role">{role}</span></div></div>
      <p className="profile-email">{profile.isLoading ? 'Loading email...' : profile.data?.email ?? 'Email unavailable'}</p>
      {profile.isError && <p className="profile-fetch-error">Profile details could not be refreshed.</p>}
      <div className="profile-menu-actions"><button type="button" onClick={logout}><LogOut size={16} />Logout</button></div>
      <div className="profile-role-note"><UserRound size={14} />Authenticated as {role}</div>
    </section>}
  </div>
}

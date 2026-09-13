import {
  Bell,
  CheckCircle2,
  ChevronRight,
  ClipboardCheck,
  FileText,
  Home,
  LogOut,
  Menu,
  ShieldCheck,
  Stamp,
} from 'lucide-react'
import { Link, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../../auth/auth-core'
import { ProfileAvatarMenu } from '../../components/navigation/profile-avatar-menu'
import '../dashboard/dashboard.css'

const navigation = [
  { label: 'Dashboard', path: '/claims/dashboard', icon: Home },
  { label: 'Claims Queue', path: '/claims/queue', icon: ClipboardCheck },
  { label: 'Claims', path: '/claims/list', icon: FileText },
  { label: 'Approvals', path: '/claims/approvals', icon: CheckCircle2 },
  { label: 'Settlements', path: '/claims/settlements', icon: Stamp },
]

export function ClaimsOfficerLayout() {
  const { session, logout } = useAuth()
  const location = useLocation()

  if (session?.role !== 'CLAIMS_OFFICER') return <Outlet />

  const initials = session.fullName.slice(0, 2).toUpperCase()
  return (
    <div className="portal-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark"><ShieldCheck size={22} /></span>
          <span>Insurance Policy<br /><b>Management Portal</b></span>
        </div>
        <nav>
          {navigation.map((item) => {
            const Icon = item.icon
            const active = item.path === location.pathname
            return <Link key={item.label} className={`nav-item${active ? ' active' : ''}`} to={item.path}><Icon size={19} />{item.label}</Link>
          })}
        </nav>
        <div className="sidebar-support">
          <span>Claims workspace</span>
          <b>Review and decision support</b>
        </div>
        <div className="profile-mini">
          <span>{initials}</span>
          <div><b>{session.fullName}</b><small>Claims officer</small></div>
          <button onClick={logout} aria-label="Sign out"><LogOut size={17} /></button>
        </div>
      </aside>
      <section className="content">
        <header className="topbar">
          <button className="mobile-menu" aria-label="Open menu"><Menu size={21} /></button>
          <div className="crumbs">Claims workspace <ChevronRight size={15} /> <b>Dashboard</b></div>
          <div className="header-actions">
            <button aria-label="Notifications" className="icon-button"><Bell size={19} /><i /></button>
            <ProfileAvatarMenu />
          </div>
        </header>
        <Outlet />
      </section>
    </div>
  )
}

import { ShieldAlert } from 'lucide-react'
import { Link } from 'react-router-dom'
import { defaultRoute, useAuth } from './auth-core'
import './auth-pages.css'

export function UnauthorizedPage() { const { session, logout } = useAuth(); return <main className="access-page"><ShieldAlert size={42} /><p>ACCESS RESTRICTED</p><h1>Your current role cannot access this workspace.</h1><span>Choose your assigned workspace or sign out to use another account.</span><div><Link className="auth-submit" to={session ? defaultRoute(session.role) : '/login'}>Go to my workspace</Link><button className="secondary-button" onClick={logout}>Sign out</button></div></main> }
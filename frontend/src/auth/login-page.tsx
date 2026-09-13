import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { Eye, EyeOff, LockKeyhole, ShieldCheck } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { defaultRoute, useAuth } from './auth-core'
import { authService, type LoginRequest } from './auth-service'
import './auth-pages.css'

const schema = z.object({ username: z.string().min(1, 'Username is required'), password: z.string().min(1, 'Password is required') })

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [showPassword, setShowPassword] = useState(false)
  const form = useForm<LoginRequest>({ resolver: zodResolver(schema), defaultValues: { username: '', password: '' } })
  const mutation = useMutation({ mutationFn: authService.login, onSuccess: (response) => { const session = login(response); navigate(session ? defaultRoute(session.role) : '/unauthorized', { replace: true }) } })
  const expired = new URLSearchParams(location.search).get('expired') === 'true'
  return <AuthShell title="Welcome back" subtitle="Sign in to manage your insurance coverage.">
    {expired && <div className="auth-alert info">Your session has expired. Sign in again to continue.</div>}
    {mutation.isError && <div className="auth-alert error">{mutation.error instanceof Error ? mutation.error.message : 'Unable to sign in. Check your credentials and try again.'}</div>}
    <form onSubmit={form.handleSubmit((data) => mutation.mutate(data))} noValidate>
      <Field label="Username" error={form.formState.errors.username?.message}><input autoComplete="username" {...form.register('username')} placeholder="Enter your username" /></Field>
      <Field label="Password" error={form.formState.errors.password?.message}><span className="password-input"><input type={showPassword ? 'text' : 'password'} autoComplete="current-password" {...form.register('password')} placeholder="Enter your password" /><button type="button" onClick={() => setShowPassword(!showPassword)} aria-label={showPassword ? 'Hide password' : 'Show password'}>{showPassword ? <EyeOff size={18} /> : <Eye size={18} />}</button></span></Field>
      <button className="auth-submit" disabled={mutation.isPending} type="submit">{mutation.isPending ? 'Signing in...' : 'Sign in securely'}</button>
    </form>
    <p className="auth-switch">Need an account? <Link to="/register">Create an account</Link></p>
  </AuthShell>
}

export function AuthShell({ title, subtitle, children }: { title: string; subtitle: string; children: React.ReactNode }) {
  return <main className="auth-page"><section className="auth-brand"><div className="auth-brand-lockup"><span><ShieldCheck size={25} /></span><b>Insurance Policy<br />Management Portal</b></div><div className="auth-message"><p>PROTECTED POLICY PORTAL</p><h1>Clarity and confidence for every stage of coverage.</h1><div className="trust-line"><LockKeyhole size={17} /> Secure access to your policy information</div></div></section><section className="auth-form-area"><div className="auth-form"><div className="auth-mobile-brand"><ShieldCheck size={22} /> Insurance Policy Management Portal</div><div className="auth-heading"><h2>{title}</h2><p>{subtitle}</p></div>{children}</div></section></main>
}

export function Field({ label, error, children }: { label: string; error?: string; children: React.ReactNode }) { return <label className="field"><span>{label}</span>{children}{error && <small>{error}</small>}</label> }
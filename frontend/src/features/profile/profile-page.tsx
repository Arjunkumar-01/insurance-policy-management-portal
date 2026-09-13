import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ChevronLeft, Pencil, ShieldCheck } from 'lucide-react'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link } from 'react-router-dom'
import { z } from 'zod'
import { apiClient, type ApiResponse } from '../../api/client'
import './profile-page.css'

type Profile = { firstName: string; lastName: string; email: string; phoneNumber: string; username: string; role: string; addressLine1?: string; addressLine2?: string; city?: string; state?: string; country?: string; postalCode?: string }
type UpdateProfile = Omit<Profile, 'username' | 'role'>
const schema = z.object({ firstName: z.string().min(1, 'First name is required').max(50), lastName: z.string().min(1, 'Last name is required').max(50), email: z.string().email('Enter a valid email').max(100), phoneNumber: z.string().regex(/^\d{10}$/, 'Use exactly 10 digits'), addressLine1: z.string().max(150).optional(), addressLine2: z.string().max(150).optional(), city: z.string().max(50).optional(), state: z.string().max(50).optional(), country: z.string().max(50).optional(), postalCode: z.string().max(15).optional() })
async function unwrap<T>(request: Promise<{ data: ApiResponse<T> }>) { const response = await request; if (!response.data.success) throw new Error(response.data.message); return response.data.data }
const profileApi = { get: () => unwrap(apiClient.get<ApiResponse<Profile>>('/api/customers/me')), update: (body: UpdateProfile) => unwrap(apiClient.put<ApiResponse<Profile>>('/api/customers/me', body)) }

export function ProfilePage() {
  const [editing, setEditing] = useState(false)
  const cache = useQueryClient()
  const profile = useQuery({ queryKey: ['auth', 'profile'], queryFn: profileApi.get })
  const form = useForm<UpdateProfile>({ resolver: zodResolver(schema) })
  const update = useMutation({ mutationFn: profileApi.update, onSuccess: (data) => { cache.setQueryData(['auth', 'profile'], data); form.reset(data); setEditing(false) } })
  useEffect(() => {
    if (profile.data && !form.formState.isDirty) form.reset(profile.data)
  }, [profile.data, form])
  if (profile.isLoading) return <main className="profile-page"><div className="profile-state">Loading profile...</div></main>
  if (!profile.data || profile.isError) return <main className="profile-page"><div className="profile-state error">Unable to load your profile.</div></main>
  const cancel = () => { form.reset(profile.data); setEditing(false) }
  return <main className="profile-page"><header><Link to="/customer/dashboard"><ChevronLeft size={17} />Dashboard</Link><span><ShieldCheck size={18} />Profile</span></header><section className="profile-title"><div><p>ACCOUNT PROFILE</p><h1>Personal information</h1><span>Manage the contact information associated with your policy account.</span></div>{!editing && <button className="profile-primary" onClick={() => setEditing(true)}><Pencil size={16} />Edit profile</button>}</section><form className="profile-card" onSubmit={form.handleSubmit((data) => update.mutate(data))}><section><h2>Account</h2><label>Username<input value={profile.data.username} disabled /></label><label>Role<input value={profile.data.role.replaceAll('_', ' ')} disabled /></label></section><section><h2>Contact details</h2><div className="profile-grid"><Field label="First name" error={form.formState.errors.firstName?.message}><input disabled={!editing} {...form.register('firstName')} /></Field><Field label="Last name" error={form.formState.errors.lastName?.message}><input disabled={!editing} {...form.register('lastName')} /></Field><Field label="Email address" error={form.formState.errors.email?.message}><input disabled={!editing} type="email" {...form.register('email')} /></Field><Field label="Phone number" error={form.formState.errors.phoneNumber?.message}><input disabled={!editing} inputMode="numeric" {...form.register('phoneNumber')} /></Field></div></section><section><h2>Address</h2><div className="profile-grid"><Field label="Address line 1"><input disabled={!editing} {...form.register('addressLine1')} /></Field><Field label="Address line 2"><input disabled={!editing} {...form.register('addressLine2')} /></Field><Field label="City"><input disabled={!editing} {...form.register('city')} /></Field><Field label="State"><input disabled={!editing} {...form.register('state')} /></Field><Field label="Country"><input disabled={!editing} {...form.register('country')} /></Field><Field label="Postal code"><input disabled={!editing} {...form.register('postalCode')} /></Field></div></section>{editing && <div className="profile-form-actions"><button type="button" className="profile-outline" onClick={cancel}>Cancel</button><button className="profile-primary" disabled={update.isPending}>{update.isPending ? 'Saving...' : 'Save changes'}</button></div>}{update.isError && <p className="form-error">{update.error.message}</p>}</form></main>
}
function Field({ label, error, children }: { label: string; error?: string; children: React.ReactNode }) { return <label><span>{label}</span>{children}{error && <small>{error}</small>}</label> }
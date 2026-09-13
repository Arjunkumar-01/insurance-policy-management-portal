import axios from 'axios'

export type ApiResponse<T> = { success: boolean; status: number; message: string; data: T }

const fallbackApiBaseUrl = import.meta.env.DEV
  ? 'http://localhost:8080'
  : typeof window !== 'undefined'
    ? window.location.origin
    : undefined

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? fallbackApiBaseUrl

if (!apiBaseUrl) {
  console.warn('VITE_API_BASE_URL is not configured. The app will try same-origin requests by default.')
}

export const apiClient = axios.create({
  baseURL: apiBaseUrl || undefined,
  headers: { 'Content-Type': 'application/json' },
})

apiClient.interceptors.request.use((config) => {
  const session = sessionStorage.getItem('ipmp.auth.session')
  if (session) {
    const { accessToken } = JSON.parse(session) as { accessToken: string }
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !error.config?.url?.startsWith('/api/auth/')) {
      sessionStorage.removeItem('ipmp.auth.session')
      window.location.assign('/login?expired=true')
    }
    return Promise.reject(error)
  },
)
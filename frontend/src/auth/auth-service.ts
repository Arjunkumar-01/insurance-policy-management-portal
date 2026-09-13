import { apiClient, type ApiResponse } from '../api/client'
import type { Role } from './auth-core'

export type LoginRequest = { username: string; password: string }
export type LoginResponse = { accessToken: string; tokenType: string; username: string; role: Role | `ROLE_${Role}`; fullName: string }
export type RegisterRequest = { firstName: string; lastName: string; dateOfBirth?: string; gender: 'MALE' | 'FEMALE' | 'OTHER'; phoneNumber: string; email: string; username: string; password: string; confirmPassword: string; addressLine1?: string; addressLine2?: string; city?: string; state?: string; country?: string; postalCode?: string }

async function unwrap<T>(request: Promise<{ data: ApiResponse<T> }>) {
  const response = await request
  if (!response.data.success) throw new Error(response.data.message)
  return response.data.data
}

export const authService = {
  login: (request: LoginRequest) => unwrap(apiClient.post<ApiResponse<LoginResponse>>('/api/auth/login', request)),
  register: (request: RegisterRequest) => unwrap(apiClient.post<ApiResponse<unknown>>('/api/auth/register', request)),
}
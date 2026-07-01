import api from './request';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  phone?: string;
  email?: string;
  role?: string;
}

export interface LoginResponse {
  id: number;
  username: string;
  role: string;
  avatarUrl?: string;
  token: string;
}

export interface User {
  id: number;
  username: string;
  phone?: string;
  email?: string;
  role: string;
  avatarUrl?: string;
  status: number;
  createdAt: string;
  updatedAt: string;
}

export const authApi = {
  login: (data: LoginRequest) => 
    api.post<any, { data: LoginResponse }>('/auth/login', data),
  
  register: (data: RegisterRequest) => 
    api.post<any, { data: LoginResponse }>('/auth/register', data),
  
  getCurrentUser: () => 
    api.get<any, { data: User }>('/auth/me'),
  
  refreshToken: () => 
    api.post<any, { data: LoginResponse }>('/auth/refresh'),
};

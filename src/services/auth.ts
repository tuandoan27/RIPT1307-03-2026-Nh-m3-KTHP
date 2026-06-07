// src/services/auth.ts
import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api';

// Tạo axios instance riêng cho auth (không cần token)
const authAxios = axios.create({ baseURL: BASE_URL });

export const login = (email: string, password: string) =>
  authAxios.post('/auth/login', { email, password });

export const getMe = () => {
  const token = localStorage.getItem('token');
  return axios.get(`${BASE_URL}/auth/me`, {
    headers: { Authorization: `Bearer ${token}` },
  });
};

export const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('userRole');
  localStorage.removeItem('userInfo');
  window.location.replace('/login');
};
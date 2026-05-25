// ============================================================
// services/adminRequest.ts
// Axios instance dành riêng cho Admin
// Extend từ baseRequest (của Vy), override baseURL + interceptors
// nếu cần thiết cho luồng Admin.
// ============================================================

import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';

// ─── Kiểu chuẩn API response theo tài liệu ───────────────────
export interface ApiResponse<T = unknown> {
  success: boolean;
  message: string;
  data: T | null;
}

export interface PaginatedData<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
}

// ─── Tạo instance ────────────────────────────────────────────
const adminRequest: AxiosInstance = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL ?? 'http://localhost:8080/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ─── Request interceptor: đính kèm JWT token ─────────────────
adminRequest.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    const token = localStorage.getItem('access_token');
    if (token && config.headers) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

// ─── Response interceptor: xử lý lỗi tập trung ──────────────
adminRequest.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    // API trả về success: false → ném lỗi để catch ở component
    if (response.data && response.data.success === false) {
      return Promise.reject(new Error(response.data.message ?? 'Có lỗi xảy ra'));
    }
    return response;
  },
  (error) => {
    if (error.response) {
      const status: number = error.response.status;

      if (status === 401) {
        // Token hết hạn → xóa token, redirect về login
        localStorage.removeItem('access_token');
        window.location.href = '/login';
        return Promise.reject(new Error('Phiên đăng nhập hết hạn, vui lòng đăng nhập lại'));
      }

      if (status === 403) {
        return Promise.reject(new Error('Bạn không có quyền thực hiện thao tác này'));
      }

      const message: string =
        error.response.data?.message ?? `Lỗi máy chủ (${status})`;
      return Promise.reject(new Error(message));
    }

    if (error.request) {
      return Promise.reject(new Error('Không thể kết nối đến máy chủ. Vui lòng kiểm tra mạng.'));
    }

    return Promise.reject(error);
  },
);

export default adminRequest;
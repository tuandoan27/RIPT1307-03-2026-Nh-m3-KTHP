// src/app.tsx
import Footer from '@/components/Footer';
import RightContent from '@/components/RightContent';
import { logout } from '@/services/auth';
import { notification } from 'antd';
// @ts-ignore
import 'moment/locale/vi';
import type { RequestConfig, RunTimeLayoutConfig } from 'umi';
import { history } from 'umi';
import NotAccessible from './pages/exception/403';
import NotFoundContent from './pages/exception/404';
import './styles/global.less';
import axios from '@/utils/axios';


// ─── Brand Logo (sidebar) ─────────────────────────────────────────────────────
const BrandIcon: React.FC = () => (
  <svg className="nksv-icon" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path d="M2 15L16 4L30 15" stroke="currentColor" strokeWidth="2.2" strokeLinecap="round" strokeLinejoin="round"/>
    <path d="M5 14.5V28H27V14.5" stroke="currentColor" strokeWidth="2" strokeLinejoin="round" fill="currentColor" fillOpacity="0.1"/>
    <rect x="12.5" y="19" width="7" height="9" rx="1" stroke="currentColor" strokeWidth="1.6"/>
    <rect x="6" y="17" width="4.5" height="4.5" rx="0.5" fill="currentColor" fillOpacity="0.55"/>
    <rect x="21.5" y="17" width="4.5" height="4.5" rx="0.5" fill="currentColor" fillOpacity="0.55"/>
  </svg>
);


export const initialStateConfig = { loading: <></> };


export async function getInitialState(): Promise<IInitialState> {
  const token = localStorage.getItem('token');
  if (token) {
    try {
      const response = await axios.get('/auth/me');
      if (response.data?.success) {
        const userRes = response.data?.data;
        const mappedUser = {
          id: userRes.id,
          name: userRes.fullName,
          fullName: userRes.fullName,
          email: userRes.email,
          studentCode: userRes.studentCode,
          role: userRes.role,
          realm_access: {
            roles: userRes.role === 'ADMIN' ? ['QUAN_TRI_VIEN'] : [],
          },
          preferred_username: userRes.studentCode || userRes.email,
          isLocked: userRes.isLocked,
          penaltyPoint: userRes.penaltyPoint,
        };
        return { currentUser: mappedUser as any, permissionLoading: false };
      }
    } catch (error) {
      console.error('Failed to fetch user state:', error);
      localStorage.removeItem('token');
    }
  }
  return { permissionLoading: false };
}


/** Gắn JWT token vào mọi request */
export const request: RequestConfig = {
  errorHandler: (error: any) => {
    const { response } = error;

    if (response?.status === 401) {
      notification.error({ message: 'Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.' });
      logout();
      return;
    }

    if (response?.status === 403) {
      notification.error({ message: 'Bạn không có quyền thực hiện thao tác này.' });
      return;
    }

    if (!response) {
      notification.error({
        message: 'Không thể kết nối đến máy chủ',
        description: 'Vui lòng kiểm tra mạng.',
      });
    }

    throw error;
  },
  requestInterceptors: [
    (url, options) => {
      const token = localStorage.getItem('token');
      return {
        url,
        options: {
          ...options,
          headers: {
            ...options.headers,
            ...(token ? { Authorization: `Bearer ${token}` } : {}),
          },
        },
      };
    },
  ],
};


export const layout: RunTimeLayoutConfig = ({ initialState }) => {
  return {
    unAccessible: <NotAccessible />,
    noFound: <NotFoundContent />,
    rightContentRender: () => <RightContent />,
    disableContentMargin: false,
    footerRender: () => <Footer />,

    onPageChange: () => {
      const token = localStorage.getItem('token');
      const { location } = history;

      if (location.pathname === '/login' || location.pathname === '/register') {
        if (token) history.replace('/home');
        return;
      }

      // Chưa đăng nhập → về trang login
      if (!token && location.pathname !== '/user/login') {
        history.replace('/login');
        return;
      }

      // Đã đăng nhập mà vào trang login → redirect theo role
      if (token && (location.pathname === '/user/login' || location.pathname === '/')) {
        const userRole = localStorage.getItem('userRole');
        history.replace(userRole === 'ADMIN' ? '/admin/dashboard' : '/home');
      }
    },

    menuItemRender: (item: any, dom: any) => (
      <a
        className="not-underline"
        key={item?.path}
        href={item?.path}
        onClick={(e) => {
          e.preventDefault();
          history.push(item?.path ?? '/');
        }}
        style={{ display: 'block' }}
      >
        {dom}
      </a>
    ),

    ...initialState?.settings,

    // ─── Forced overrides ──────────────────────────────────────────────
    navTheme: 'dark' as any,
    logo: (
     <>
    <BrandIcon />
    <span className="nksv-header-title">Nhà Kho Sinh Viên</span>
     </>
    ),
    title: false,
    menuHeaderRender: false,
  };
};

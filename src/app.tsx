// src/app.tsx
import Footer from '@/components/Footer';
import RightContent from '@/components/RightContent';
import { logout } from '@/services/auth';
import { notification } from 'antd';
import { history, type RequestConfig, type RunTimeLayoutConfig } from 'umi';
import NotAccessible from './pages/exception/403';
import NotFoundContent from './pages/exception/404';
import './styles/global.less';

export const initialStateConfig = {
  loading: <></>,
};

/** Lấy thông tin user từ localStorage */
export async function getInitialState() {
  const token = localStorage.getItem('token');
  const userInfo = localStorage.getItem('userInfo');

  // Không có token → redirect về login
  if (!token || !userInfo) {
    if (window.location.pathname !== '/user/login') {
      history.replace('/user/login');
    }
    return { currentUser: null };
  }

  try {
    const user = JSON.parse(userInfo);
    return { currentUser: user };
  } catch {
    logout();
    return { currentUser: null };
  }
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

      // Chưa đăng nhập → về trang login
      if (!token && location.pathname !== '/user/login') {
        history.replace('/user/login');
        return;
      }

      // Đã đăng nhập mà vào trang login → redirect theo role
      if (token && location.pathname === '/user/login') {
        const userRole = localStorage.getItem('userRole');
        history.replace(userRole === 'ADMIN' ? '/admin/requests' : '/dashboard');
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
  };
};
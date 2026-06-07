import Footer from '@/components/Footer';
import RightContent from '@/components/RightContent';
import { notification } from 'antd';
// @ts-ignore
import 'moment/locale/vi';
import type { RequestConfig, RunTimeLayoutConfig } from 'umi';
import { getIntl, getLocale, history } from 'umi';
import type { RequestOptionsInit, ResponseError } from 'umi-request';
import ErrorBoundary from './components/ErrorBoundary';
import OneSignalBounder from './components/OneSignalBounder';
import TechnicalSupportBounder from './components/TechnicalSupportBounder';
import NotAccessible from './pages/exception/403';
import NotFoundContent from './pages/exception/404';
import type { IInitialState } from './services/base/typing';
import './styles/global.less';
import axios from '@/utils/axios';


// ─── Brand Logo (sidebar) ─────────────────────────────────────────────────────
// Thay LogoHeader bằng BrandIcon (chỉ SVG, không cần collapsed)
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


const authHeaderInterceptor = (url: string, options: RequestOptionsInit) => ({});


export const request: RequestConfig = {
  errorHandler: (error: ResponseError) => {
    const { messages } = getIntl(getLocale());
    const { response } = error;
    if (response && response.status) {
      const { status, statusText, url } = response;
      const requestErrorMessage = messages['app.request.error'];
      const errorMessage = `${requestErrorMessage} ${status}: ${url}`;
      const errorDescription = messages[`app.request.${status}`] || statusText;
      notification.error({ message: errorMessage, description: errorDescription });
    }
    if (!response) {
      notification.error({ description: 'Yêu cầu gặp lỗi', message: 'Bạn hãy thử lại sau' });
    }
    throw error;
  },
  requestInterceptors: [authHeaderInterceptor],
};


export const layout: RunTimeLayoutConfig = ({ initialState }) => {
  return {
    unAccessible: (
      <TechnicalSupportBounder><NotAccessible /></TechnicalSupportBounder>
    ),
    noFound: <NotFoundContent />,
    rightContentRender: () => <RightContent />,
    disableContentMargin: false,
    footerRender: () => <Footer />,


    onPageChange: () => {
      const { location } = history;
      const token = localStorage.getItem('token');
      if (location.pathname === '/login' || location.pathname === '/register') {
        if (token) history.replace('/home');
        return;
      }
      if (!token) { history.replace('/login'); return; }
      if (location.pathname === '/') history.replace('/home');
    },


    menuItemRender: (item: any, dom: any) => (
      <a
        className="not-underline"
        key={item?.path}
        href={item?.path}
        onClick={(e) => { e.preventDefault(); history.push(item?.path ?? '/'); }}
        style={{ display: 'block' }}
      >
        {dom}
      </a>
    ),


    childrenRender: (dom) => (
      <ErrorBoundary><OneSignalBounder>{dom}</OneSignalBounder></ErrorBoundary>
    ),


    ...initialState?.settings,


    // ─── Forced overrides ─────────────────────────────────────────────
    // ─── Forced overrides ──────────────────
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

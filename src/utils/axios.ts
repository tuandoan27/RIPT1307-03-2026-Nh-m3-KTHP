import { message, notification } from 'antd';
import axios from 'axios';
import { history } from 'umi';
import data from './data';

// Create a custom Axios instance
const instance = axios.create({
	baseURL: '/api', // Base URL to hit mock endpoints under /api/
});

// Request Interceptor: Attach JWT Token from localStorage
instance.interceptors.request.use(
	(config) => {
		const token = localStorage.getItem('token');
		if (token) {
			config.headers = config.headers || {};
			config.headers.Authorization = `Bearer ${token}`;
		}
		return config;
	},
	(error) => Promise.reject(error),
);

// Response Interceptor: Global Error Handler and 401 Interception
instance.interceptors.response.use(
	(response) => response,
	(error) => {
		const { response } = error;

		// 401 Unauthorized Error: Clear credentials and redirect to login
		if (response && response.status === 401) {
			localStorage.removeItem('token');
			notification.error({
				message: 'Phiên làm việc hết hạn',
				description: 'Vui lòng đăng nhập lại.',
			});
			history.replace('/login');
			return Promise.reject(error);
		}

		let er = response?.data;
		// Convert response data to JSON if it's an array buffer
		if ((response?.config?.responseType as string)?.toLowerCase() === 'arraybuffer' && er) {
			const decoder = new TextDecoder('utf-8');
			try {
				er = JSON.parse(decoder.decode(er));
			} catch (e) {
				// Parsing failed
			}
		}

		const descriptionError = Array.isArray(er?.detail?.exception?.response?.message)
			? er?.detail?.exception?.response?.message?.join(', ')
			: Array.isArray(er?.detail?.exception?.errors)
				? er?.detail?.exception?.errors?.map((e: any) => e?.message)?.join(', ')
				: data.error[er?.detail?.errorCode || er?.errorCode] ||
				er?.detail?.message ||
				er?.message ||
				er?.errorDescription;

		const originalRequest = error.config;
		let originData = originalRequest?.data;
		if (typeof originData === 'string') {
			try {
				originData = JSON.parse(originData);
			} catch (e) {
				// Ignore
			}
		}

		const isSilent = typeof originData === 'object' && originData?.silent;

		if (!isSilent && response && response.status) {
			switch (response.status) {
				case 400:
					notification.error({
						message: 'Dữ liệu chưa đúng (004)',
						description: descriptionError || 'Yêu cầu không hợp lệ.',
					});
					break;

				case 403:
				case 405:
					notification.error({
						message: 'Thao tác không được phép (304)',
						description: descriptionError || 'Bạn không có quyền thực hiện thao tác này.',
					});
					break;

				case 404:
					notification.error({
						message: 'Không tìm thấy dữ liệu (040)',
						description: descriptionError || 'Đường dẫn hoặc tài nguyên không tồn tại.',
					});
					break;

				case 409:
					notification.error({
						message: 'Dữ liệu chưa đúng (904)',
						description: descriptionError || 'Xung đột dữ liệu xảy ra.',
					});
					break;

				case 500:
				case 502:
					notification.error({
						message: 'Hệ thống đang cập nhật (005)',
						description: descriptionError || 'Lỗi hệ thống phía máy chủ.',
					});
					break;

				default:
					message.error(descriptionError || 'Hệ thống đang gặp lỗi. Vui lòng thử lại sau.');
					break;
			}
		} else if (!response) {
			notification.error({
				message: 'Không thể kết nối',
				description: 'Vui lòng kiểm tra lại kết nối mạng.',
			});
		}

		return Promise.reject(error);
	},
);

export default instance;

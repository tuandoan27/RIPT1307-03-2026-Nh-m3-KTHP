import Footer from '@/components/Footer';
import { LockOutlined, MailOutlined } from '@ant-design/icons';
import { Alert, Button, Form, Input, Card, message } from 'antd';
import React, { useState } from 'react';
import { Link, history, useModel } from 'umi';
import axios from '@/utils/axios';
import styles from './index.less';

const Login: React.FC = () => {
	const [submitting, setSubmitting] = useState(false);
	const [errorMsg, setErrorMsg] = useState<string | null>(null);
	const { refresh } = useModel('@@initialState');
	const [form] = Form.useForm();

	const handleSubmit = async (values: any) => {
		setErrorMsg(null);
		setSubmitting(true);
		try {
			// Hit the mock login endpoint
			const response = await axios.post('/login', {
	            email: values.email,
	            password: values.password,
			});

			if (response.data?.status === 'ok') {
				// Save token
				localStorage.setItem('token', response.data?.token || 'mock-jwt-token-xyz');
				localStorage.setItem('userRole', response.data?.role || 'student');
				message.success('Đăng nhập thành công!');

				// Refresh initial state to fetch user info and permissions
				await refresh();

				// Redirect based on role
				const role = response.data?.role || 'student';
				if (role === 'admin') {
					history.push('/admin/dashboard');
				} else {
					history.push('/home');
				}
			} else {
				setErrorMsg(response.data?.message || 'Email hoặc mật khẩu không chính xác.');
			}
		} catch (error: any) {
			setErrorMsg(
				error?.response?.data?.message ||
				'Không thể kết nối đến hệ thống. Vui lòng thử lại sau.'
			);
		} finally {
			setSubmitting(false);
		}
	};

	return (
		<div className={styles.container}>
			<div className={styles.content}>
				<div className={styles.top}>
					<div className={styles.header}>
						<div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column' }}>
							<img alt='logo' className={styles.logo} src='/logo-full.svg' style={{ maxHeight: '80px', marginBottom: '16px' }} />
							<span className={styles.title2} style={{ fontSize: '24px', letterSpacing: '0.5px' }}>
								CỔNG THÔNG TIN SINH VIÊN
							</span>
						</div>
					</div>
				</div>

				<div className={styles.main}>
					<Card bordered={false} bodyStyle={{ padding: 0 }}>
						<h3 style={{ textAlign: 'center', fontSize: '18px', fontWeight: 600, marginBottom: '24px', color: '#333' }}>
							Đăng Nhập Hệ Thống
						</h3>

						{errorMsg && (
							<Alert
								message={errorMsg}
								type='error'
								showIcon
								style={{ marginBottom: 24 }}
							/>
						)}

						<Form
							form={form}
							onFinish={handleSubmit}
							layout='vertical'
							requiredMark={false}
						>
							<Form.Item
								name='email'
								rules={[
									{ required: true, message: 'Vui lòng nhập email!' },
									{ type: 'email', message: 'Email không đúng định dạng!' }
								]}
							>
								<Input
									placeholder='Email (ví dụ: student@ptit.edu.vn)'
									prefix={<MailOutlined className={styles.prefixIcon} />}
									size='large'
									style={{ borderRadius: '6px' }}
								/>
							</Form.Item>
							<Form.Item
								name='password'
								rules={[
									{ required: true, message: 'Vui lòng nhập mật khẩu!' },
									{ min: 6, message: 'Mật khẩu phải chứa ít nhất 6 ký tự!' }
								]}
							>
								<Input.Password
									placeholder='Mật khẩu'
									prefix={<LockOutlined className={styles.prefixIcon} />}
									size='large'
									style={{ borderRadius: '6px' }}
								/>
							</Form.Item>

							<div style={{ marginBottom: 24, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
								<span style={{ color: '#8c8c8c', fontSize: '13px' }}>
									Mẹo: <b>student@ptit.edu.vn</b> / <b>123456</b>
								</span>
								<Link to='/register' style={{ fontSize: '14px', fontWeight: 500 }}>
									Đăng ký tài khoản
								</Link>
							</div>

							<Form.Item>
								<Button
									type='primary'
									htmlType='submit'
									block
									size='large'
									loading={submitting}
									style={{
										height: '45px',
										borderRadius: '6px',
										fontSize: '16px',
										fontWeight: 600,
										boxShadow: '0 4px 6px rgba(0, 0, 0, 0.05)'
									}}
								>
									Đăng Nhập
								</Button>
							</Form.Item>
						</Form>
					</Card>
				</div>
			</div>

			<div className='login-footer'>
				<Footer />
			</div>
		</div>
	);
};

export default Login;

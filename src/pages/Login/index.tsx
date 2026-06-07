import Footer from '@/components/Footer';
import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { Button, Form, Input, message } from 'antd';
import axios from 'axios';
import React, { useState } from 'react';
import { history, useModel } from 'umi';

const Login: React.FC = () => {
	const [submitting, setSubmitting] = useState(false);
	const { initialState, setInitialState } = useModel('@@initialState');
	const [form] = Form.useForm();

	const handleSubmit = async (values: { email: string; password: string }) => {
		try {
			setSubmitting(true);
			const res = await axios.post('http://localhost:8080/api/auth/login', {
				email: values.email,
				password: values.password,
			});

			const { token, user } = res.data.data;

			// Lưu token và thông tin user
			localStorage.setItem('token', token);
			localStorage.setItem('userRole', user.role);
			localStorage.setItem('userInfo', JSON.stringify(user));

			// Cập nhật initialState
			setInitialState({
				...initialState,
				currentUser: user,
			});

			message.success('Đăng nhập thành công!');

			// Redirect theo role
			if (user.role === 'ADMIN') {
				history.replace('/admin/dashboard');
			} else {
				history.replace('/dashboard');
			}
		} catch (error: any) {
			const msg =
				error?.response?.data?.message ||
				'Email hoặc mật khẩu không đúng';
			message.error(msg);
		} finally {
			setSubmitting(false);
		}
	};

	return (
		<div style={{
			minHeight: '100vh',
			display: 'flex',
			flexDirection: 'column',
			alignItems: 'center',
			justifyContent: 'center',
			background: '#f0f2f5',
		}}>
			<div style={{
				width: 380,
				background: '#fff',
				borderRadius: 12,
				padding: '40px 32px',
				boxShadow: '0 4px 24px rgba(0,0,0,0.08)',
			}}>
				<div style={{ textAlign: 'center', marginBottom: 32 }}>
					<img alt='logo' src='/logo-full.svg' style={{ height: 48, marginBottom: 16 }} />
					<div style={{ fontSize: 20, fontWeight: 600 }}>Đăng nhập</div>
				</div>

				<Form form={form} onFinish={handleSubmit} layout='vertical'>
					<Form.Item
						name='email'
						rules={[
							{ required: true, message: 'Vui lòng nhập email' },
							{ type: 'email', message: 'Email không hợp lệ' },
						]}
					>
						<Input
							placeholder='Nhập email'
							prefix={<UserOutlined />}
							size='large'
						/>
					</Form.Item>

					<Form.Item
						name='password'
						rules={[{ required: true, message: 'Vui lòng nhập mật khẩu' }]}
					>
						<Input.Password
							placeholder='Nhập mật khẩu'
							prefix={<LockOutlined />}
							size='large'
						/>
					</Form.Item>

					<Button
						type='primary'
						htmlType='submit'
						block
						size='large'
						loading={submitting}
					>
						Đăng nhập
					</Button>
				</Form>
			</div>

			<div style={{ marginTop: 24 }}>
				<Footer />
			</div>
		</div>
	);
};

export default Login;
import Footer from '@/components/Footer';
import { LockOutlined, MailOutlined, UserOutlined, IdcardOutlined } from '@ant-design/icons';
import { Alert, Button, Form, Input, Card, message } from 'antd';
import React, { useState } from 'react';
import { Link, history } from 'umi';
import axios from '@/utils/axios';
import styles from '../Login/index.less';

const Register: React.FC = () => {
	const [submitting, setSubmitting] = useState(false);
	const [errorMsg, setErrorMsg] = useState<string | null>(null);
	const [form] = Form.useForm();

	const handleSubmit = async (values: any) => {
		setErrorMsg(null);
		setSubmitting(true);
		try {
			const response = await axios.post('/auth/register', {
				fullName: values.fullName,
				studentCode: values.studentId,
				email: values.email,
				password: values.password,
			});

			if (response.data?.success) {
				message.success('Đăng ký tài khoản thành công! Vui lòng đăng nhập.');
				history.push('/login');
			} else {
				setErrorMsg(response.data?.message || 'Đăng ký không thành công.');
			}
		} catch (error: any) {
			setErrorMsg(
				error?.response?.data?.message ||
				'Không thể đăng ký. Vui lòng thử lại.'
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
							Đăng Ký Tài Khoản Mới
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
								name='fullName'
								rules={[
									{ required: true, message: 'Vui lòng nhập họ tên!' },
									{ min: 5, message: 'Họ tên phải có ít nhất 5 ký tự!' },
									{ max: 100, message: 'Họ tên không được quá 100 ký tự!' }
								]}
							>
								<Input
									placeholder='Họ và tên'
									prefix={<UserOutlined className={styles.prefixIcon} />}
									size='large'
									style={{ borderRadius: '6px' }}
								/>
							</Form.Item>

							<Form.Item
								name='studentId'
								rules={[
									{ required: true, message: 'Vui lòng nhập mã sinh viên!' },
									{ pattern: /^[A-Z0-9]{8,12}$/, message: 'Mã sinh viên không hợp lệ!' }
								]}
							>
								<Input
									placeholder='Mã sinh viên (ví dụ: K20CT001)'
									prefix={<IdcardOutlined className={styles.prefixIcon} />}
									size='large'
									style={{ borderRadius: '6px' }}
								/>
							</Form.Item>

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

							<Form.Item
								name='confirmPassword'
								dependencies={['password']}
								rules={[
									{ required: true, message: 'Vui lòng xác nhận mật khẩu!' },
									({ getFieldValue }) => ({
										validator(_, value) {
											if (!value || getFieldValue('password') === value) {
												return Promise.resolve();
											}
											return Promise.reject(new Error('Mật khẩu xác nhận không khớp!'));
										},
									}),
								]}
							>
								<Input.Password
									placeholder='Xác nhận mật khẩu'
									prefix={<LockOutlined className={styles.prefixIcon} />}
									size='large'
									style={{ borderRadius: '6px' }}
								/>
							</Form.Item>

							<div style={{ marginBottom: 24, display: 'flex', justifyContent: 'center' }}>
								<Link to='/login' style={{ fontSize: '14px', fontWeight: 500 }}>
									Đã có tài khoản? Đăng nhập ngay
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
									Đăng Ký
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

export default Register;

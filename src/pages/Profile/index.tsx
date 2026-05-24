import React, { useState, useEffect } from 'react';
import { Card, Form, Input, Button, Descriptions, Table, Alert, Badge, Modal, message, Row, Col, Statistic, Divider } from 'antd';
import { LockOutlined, WarningOutlined, CheckCircleOutlined } from '@ant-design/icons';
import axios from '@/utils/axios';
import dayjs from 'dayjs';
import styles from './index.less';

interface UserProfile {
	id: string;
	fullName: string;
	studentId: string;
	email: string;
	penaltyPoints: number;
	locked: boolean;
	lockReason?: string;
}

interface PenaltyHistory {
	id: string;
	reason: string;
	points: number;
	date: string;
}

const Profile: React.FC = () => {
	const [profile, setProfile] = useState<UserProfile | null>(null);
	const [penaltyHistory, setPenaltyHistory] = useState<PenaltyHistory[]>([]);
	const [loading, setLoading] = useState(true);
	const [passwordForm] = Form.useForm();
	const [passwordSubmitting, setPasswordSubmitting] = useState(false);
	const [passwordModalVisible, setPasswordModalVisible] = useState(false);

	// Fetch profile and penalty data
	useEffect(() => {
		const fetchProfile = async () => {
			setLoading(true);
			try {
				const response = await axios.get('/profile');
				const data = response.data?.data || response.data;

				// Set user profile
				setProfile(data);

				// Fetch penalty history
				const penaltyResponse = await axios.get('/penalty-history');
				const sorted = (penaltyResponse.data?.data || []).sort((a: PenaltyHistory, b: PenaltyHistory) =>
					dayjs(b.date).diff(dayjs(a.date))
				);
				setPenaltyHistory(sorted);
			} catch (error) {
				console.error('Failed to fetch profile:', error);
				// Mock data
				setProfile({
					id: '1',
					fullName: 'Nguyễn Văn A',
					studentId: 'K20CT001',
					email: 'student@ptit.edu.vn',
					penaltyPoints: 3,
					locked: false,
				});

				setPenaltyHistory([
					{
						id: '1',
						reason: 'Trả thiết bị hư hỏng',
						points: 2,
						date: dayjs().subtract(10, 'days').format('YYYY-MM-DD'),
					},
					{
						id: '2',
						reason: 'Trả thiết bị quá hạn 2 ngày',
						points: 1,
						date: dayjs().subtract(20, 'days').format('YYYY-MM-DD'),
					},
				]);
			} finally {
				setLoading(false);
			}
		};
		fetchProfile();
	}, []);

	// Handle password change
	const handlePasswordChange = async (values: any) => {
		if (values.newPassword !== values.confirmPassword) {
			message.error('Mật khẩu mới và xác nhận không khớp!');
			return;
		}

		setPasswordSubmitting(true);
		try {
			await axios.post('/change-password', {
				oldPassword: values.oldPassword,
				newPassword: values.newPassword,
			});

			message.success('Đổi mật khẩu thành công!');
			passwordForm.resetFields();
			setPasswordModalVisible(false);
		} catch (error: any) {
			message.error(error?.response?.data?.message || 'Đổi mật khẩu thất bại!');
		} finally {
			setPasswordSubmitting(false);
		}
	};

	if (!profile) {
		return <div style={{ textAlign: 'center', padding: '50px' }}>Đang tải dữ liệu...</div>;
	}

	const penaltyColumns = [
		{
			title: 'Lý Do Phạt',
			dataIndex: 'reason',
			key: 'reason',
			width: '50%',
		},
		{
			title: 'Điểm Phạt',
			dataIndex: 'points',
			key: 'points',
			width: '15%',
			render: (points: number) => <Badge color="red" text={`${points} điểm`} />,
		},
		{
			title: 'Ngày',
			dataIndex: 'date',
			key: 'date',
			width: '35%',
			render: (date: string) => dayjs(date).format('DD/MM/YYYY'),
		},
	];

	return (
		<div className={styles.container}>
			{/* User Info Card */}
			<Card title="Thông Tin Cá Nhân" style={{ marginBottom: 24 }}>
				<Descriptions bordered column={1} size="small">
					<Descriptions.Item label="Họ Tên">
						{profile.fullName}
					</Descriptions.Item>
					<Descriptions.Item label="Mã Sinh Viên">
						{profile.studentId}
					</Descriptions.Item>
					<Descriptions.Item label="Email">
						{profile.email}
					</Descriptions.Item>
					<Descriptions.Item label="Trạng Thái Tài Khoản">
						{profile.locked ? (
							<Badge status="error" text="Bị khóa" />
						) : (
							<Badge status="success" text="Hoạt động" />
						)}
					</Descriptions.Item>
				</Descriptions>
			</Card>

			{/* Penalty Points Card */}
			<Card title="Điểm Phạt Hiện Tại" style={{ marginBottom: 24 }}>
				<Row gutter={16} align="middle">
					<Col xs={24} sm={12}>
						<Statistic
							title="Tổng Điểm Phạt"
							value={profile.penaltyPoints}
							valueStyle={{
								color: profile.penaltyPoints >= 5 ? '#ff4d4f' : '#52c41a',
								fontSize: '36px',
							}}
							prefix={profile.penaltyPoints >= 5 ? <WarningOutlined /> : <CheckCircleOutlined />}
						/>
					</Col>
					<Col xs={24} sm={12}>
						<div style={{
							padding: '16px',
							backgroundColor: profile.penaltyPoints >= 5 ? '#fff1f0' : '#f6ffed',
							borderRadius: '8px',
							border: `1px solid ${profile.penaltyPoints >= 5 ? '#ffccc7' : '#b7eb8f'}`,
						}}>
							{profile.penaltyPoints >= 5 ? (
								<Alert
									message="Tài Khoản Bị Khóa Mượn"
									description="Bạn đã tích lũy 5 điểm phạt trở lên. Tài khoản của bạn bị khóa mượn thiết bị cho đến khi điểm phạt được giảm."
									type="error"
									showIcon
									icon={<WarningOutlined />}
									style={{ marginBottom: 0 }}
								/>
							) : (
								<Alert
									message="Tài Khoản Bình Thường"
									description={`Bạn còn ${5 - profile.penaltyPoints} điểm trước khi bị khóa mượn.`}
									type="success"
									showIcon
									style={{ marginBottom: 0 }}
								/>
							)}
						</div>
					</Col>
				</Row>

				{profile.lockReason && (
					<Alert
						message={profile.lockReason}
						type="warning"
						showIcon
						style={{ marginTop: 16 }}
					/>
				)}
			</Card>

			{/* Penalty History */}
			<Card title="Lịch Sử Điểm Phạt" style={{ marginBottom: 24 }}>
				{penaltyHistory.length === 0 ? (
					<div style={{ textAlign: 'center', padding: '50px 0', color: '#999' }}>
						Không có lịch sử phạt
					</div>
				) : (
					<Table
						columns={penaltyColumns}
						dataSource={penaltyHistory}
						rowKey="id"
						pagination={{ pageSize: 10 }}
						scroll={{ x: 600 }}
					/>
				)}
			</Card>

			{/* Change Password Section */}
			<Card title="Bảo Mật Tài Khoản" style={{ marginBottom: 24 }}>
				<p style={{ color: '#666', marginBottom: 16 }}>
					Thay đổi mật khẩu của bạn để bảo vệ tài khoản.
				</p>
				<Button
					type="primary"
					icon={<LockOutlined />}
					onClick={() => setPasswordModalVisible(true)}
				>
					Đổi Mật Khẩu
				</Button>
			</Card>

			{/* Change Password Modal */}
			<Modal
				title="Đổi Mật Khẩu"
				visible={passwordModalVisible}
				onCancel={() => {
					setPasswordModalVisible(false);
					passwordForm.resetFields();
				}}
				footer={[
					<Button key="cancel" onClick={() => {
						setPasswordModalVisible(false);
						passwordForm.resetFields();
					}}>
						Hủy
					</Button>,
					<Button
						key="submit"
						type="primary"
						loading={passwordSubmitting}
						onClick={() => passwordForm.submit()}
					>
						Đổi Mật Khẩu
					</Button>,
				]}
			>
				<Form
					form={passwordForm}
					layout="vertical"
					onFinish={handlePasswordChange}
				>
					<Form.Item
						name="oldPassword"
						label="Mật Khẩu Cũ"
						rules={[
							{ required: true, message: 'Vui lòng nhập mật khẩu cũ!' },
						]}
					>
						<Input.Password placeholder="Nhập mật khẩu cũ" />
					</Form.Item>

					<Form.Item
						name="newPassword"
						label="Mật Khẩu Mới"
						rules={[
							{ required: true, message: 'Vui lòng nhập mật khẩu mới!' },
							{ min: 6, message: 'Mật khẩu phải có ít nhất 6 ký tự!' },
						]}
					>
						<Input.Password placeholder="Nhập mật khẩu mới" />
					</Form.Item>

					<Form.Item
						name="confirmPassword"
						label="Xác Nhận Mật Khẩu"
						dependencies={['newPassword']}
						rules={[
							{ required: true, message: 'Vui lòng xác nhận mật khẩu!' },
							({ getFieldValue }) => ({
								validator(_, value) {
									if (!value || getFieldValue('newPassword') === value) {
										return Promise.resolve();
									}
									return Promise.reject(new Error('Mật khẩu xác nhận không khớp!'));
								},
							}),
						]}
					>
						<Input.Password placeholder="Xác nhận mật khẩu mới" />
					</Form.Item>
				</Form>
			</Modal>
		</div>
	);
};

export default Profile;

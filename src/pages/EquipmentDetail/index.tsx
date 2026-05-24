import React, { useState, useEffect } from 'react';
import { Card, Form, Button, Input, DatePicker, Alert, Spin, Empty, Divider, Row, Col, Statistic, Tag, Calendar, Badge, Space, Modal, message } from 'antd';
import { ArrowLeftOutlined, CalendarOutlined, AlertOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { history, useParams } from 'umi';
import dayjs from 'dayjs';
import axios from '@/utils/axios';
import styles from './index.less';

interface Equipment {
	id: string;
	name: string;
	description: string;
	image: string;
	totalQuantity: number;
	availableQuantity: number;
	isDeleted: boolean;
}

interface BookedDate {
	date: string;
	bookedCount: number;
}

const EquipmentDetail: React.FC = () => {
	const { id } = useParams<{ id: string }>();
	const [equipment, setEquipment] = useState<Equipment | null>(null);
	const [loading, setLoading] = useState(true);
	const [submitting, setSubmitting] = useState(false);
	const [bookedDates, setBookedDates] = useState<BookedDate[]>([]);
	const [accountLocked, setAccountLocked] = useState(false);
	const [lockReason, setLockReason] = useState('');
	const [form] = Form.useForm();

	// Fetch equipment detail
	useEffect(() => {
		const fetchEquipment = async () => {
			setLoading(true);
			try {
				const response = await axios.get(`/equipment/${id}`);
				setEquipment(response.data?.data);

				// Fetch booked dates
				const bookedResponse = await axios.get(`/equipment/${id}/booked-dates`);
				setBookedDates(bookedResponse.data?.data || []);

				// Check account status
				const profileResponse = await axios.get('/profile');
				if (profileResponse.data?.locked) {
					setAccountLocked(true);
					setLockReason(profileResponse.data?.lockReason || 'Tài khoản của bạn đã bị khóa mượn thiết bị');
				}
			} catch (error) {
				console.error('Failed to fetch equipment:', error);
				// Mock data
				setEquipment({
					id: id || '1',
					name: 'Laptop Dell XPS 15',
					description: 'Laptop cao cấp với CPU Intel i7, RAM 16GB, SSD 512GB. Phù hợp cho các đồ án lập trình và thiết kế đồ họa.',
					image: 'https://via.placeholder.com/500x400?text=Laptop+XPS',
					totalQuantity: 5,
					availableQuantity: 2,
					isDeleted: false,
				});
			} finally {
				setLoading(false);
			}
		};
		fetchEquipment();
	}, [id]);

	// Check availability in date range
	const checkAvailability = async (startDate: dayjs.Dayjs, endDate: dayjs.Dayjs) => {
		try {
			const response = await axios.get(`/equipment/${id}/check-overlap`, {
				params: {
					startDate: startDate.format('YYYY-MM-DD'),
					endDate: endDate.format('YYYY-MM-DD'),
				},
			});
			const overlapCount = response.data?.overlapCount || 0;
			return equipment && overlapCount < equipment.totalQuantity;
		} catch (error) {
			return false;
		}
	};

	const handleSubmit = async (values: any) => {
		if (!equipment) return;

		const startDate = values.dates[0];
		const endDate = values.dates[1];

		// Check availability
		const isAvailable = await checkAvailability(startDate, endDate);

		if (!isAvailable) {
			Modal.confirm({
				title: 'Thiết bị không đủ số lượng',
				content: 'Số lượng thiết bị không đủ cho khoảng thời gian này. Bạn có muốn chọn ngày khác không?',
				okText: 'Chọn ngày khác',
				cancelText: 'Hủy',
				onCancel: () => {
					setSubmitting(false);
				},
			});
			return;
		}

		setSubmitting(true);
		try {
			await axios.post(`/equipment/${id}/borrow-request`, {
				startDate: startDate.format('YYYY-MM-DD'),
				endDate: endDate.format('YYYY-MM-DD'),
				notes: values.notes,
			});

			message.success('Gửi yêu cầu mượn thành công! Vui lòng chờ phê duyệt.');
			history.push('/my-requests');
		} catch (error: any) {
			message.error(error?.response?.data?.message || 'Gửi yêu cầu thất bại. Vui lòng thử lại.');
		} finally {
			setSubmitting(false);
		}
	};

	if (loading) {
		return <Spin spinning={true} style={{ margin: '50px auto', display: 'block' }} />;
	}

	if (!equipment) {
		return <Empty description="Không tìm thấy thiết bị" />;
	}

	// Mark booked dates on calendar
	const bookedDateSet = new Set(bookedDates.map((d) => d.date));

	return (
		<div className={styles.container}>
			<Button
				type="text"
				icon={<ArrowLeftOutlined />}
				onClick={() => history.push('/home')}
				style={{ marginBottom: 16 }}
			>
				Quay lại
			</Button>

			<Row gutter={[24, 24]}>
				{/* Equipment Info */}
				<Col xs={24} md={12}>
					<Card className={styles.imageCard}>
						<img
							src={equipment.image}
							alt={equipment.name}
							style={{ width: '100%', height: 400, objectFit: 'cover', borderRadius: 8 }}
						/>
					</Card>
				</Col>

				{/* Equipment Details */}
				<Col xs={24} md={12}>
					<Card>
						<h1 className={styles.title}>{equipment.name}</h1>

						{equipment.isDeleted && (
							<Alert
								message="Thiết bị này đã bị xóa khỏi hệ thống"
								type="error"
								showIcon
								icon={<AlertOutlined />}
								style={{ marginBottom: 16 }}
							/>
						)}

						{accountLocked && (
							<Alert
								message="Tài khoản bị khóa mượn"
								description={lockReason}
								type="warning"
								showIcon
								icon={<AlertOutlined />}
								style={{ marginBottom: 16 }}
							/>
						)}

						<p className={styles.description}>{equipment.description}</p>

						<Divider />

						<Row gutter={16}>
							<Col span={12}>
								<Statistic
									title="Tổng số lượng"
									value={equipment.totalQuantity}
									prefix={<CheckCircleOutlined />}
								/>
							</Col>
							<Col span={12}>
								<Statistic
									title="Còn lại"
									value={equipment.availableQuantity}
									valueStyle={{
										color: equipment.availableQuantity > 0 ? '#52c41a' : '#ff4d4f',
									}}
									prefix={<CheckCircleOutlined />}
								/>
							</Col>
						</Row>

						<Divider />

						<div className={styles.statusBadge}>
							{equipment.availableQuantity > 0 ? (
								<Tag color="green" icon={<CheckCircleOutlined />}>
									Còn hàng - Có thể mượn
								</Tag>
							) : (
								<Tag color="red" icon={<AlertOutlined />}>
									Hết hàng
								</Tag>
							)}
						</div>
					</Card>
				</Col>
			</Row>

			{/* Borrow Form */}
			<Row gutter={[24, 24]} style={{ marginTop: 24 }}>
				<Col xs={24} md={12}>
					<Card title="Lịch Đặt Thiết Bị">
						<Calendar
							fullscreen={false}
							dateCellRender={(value) => {
								const dateStr = value.format('YYYY-MM-DD');
								const booked = bookedDateSet.has(dateStr);
								return booked ? (
									<Badge
										count="Đã đặt"
										style={{
											backgroundColor: '#ff4d4f',
											fontSize: '10px',
											height: '20px',
											lineHeight: '20px',
										}}
									/>
								) : null;
							}}
						/>
					</Card>
				</Col>

				<Col xs={24} md={12}>
					<Card title="Gửi Yêu Cầu Mượn">
						{accountLocked || equipment.isDeleted || equipment.availableQuantity === 0 ? (
							<Alert
								message={
									accountLocked
										? 'Tài khoản của bạn đã bị khóa'
										: equipment.isDeleted
											? 'Thiết bị đã bị xóa'
											: 'Thiết bị hiện không có sẵn'
								}
								type="error"
								showIcon
								style={{ marginBottom: 16 }}
							/>
						) : (
							<Form
								form={form}
								layout="vertical"
								onFinish={handleSubmit}
							>
								<Form.Item
									name="dates"
									label="Chọn Ngày Mượn - Ngày Trả"
									rules={[
										{ required: true, message: 'Vui lòng chọn ngày!' },
									]}
								>
									<DatePicker.RangePicker
										style={{ width: '100%' }}
										format="DD/MM/YYYY"
										disabledDate={(current) => {
											if (!current) return false;
											// Disable past dates
											return current.isBefore(dayjs(), 'day');
										}}
									/>
								</Form.Item>

								<Form.Item
									name="notes"
									label="Ghi Chú (tùy chọn)"
									rules={[
										{ max: 500, message: 'Ghi chú không được vượt quá 500 ký tự!' },
									]}
								>
									<Input.TextArea
										rows={4}
										placeholder="Nhập mục đích hoặc ghi chú về việc mượn thiết bị..."
									/>
								</Form.Item>

								<Form.Item>
									<Button
										type="primary"
										htmlType="submit"
										block
										size="large"
										loading={submitting}
										disabled={equipment.isDeleted || accountLocked || equipment.availableQuantity === 0}
									>
										Gửi Yêu Cầu Mượn
									</Button>
								</Form.Item>
							</Form>
						)}
					</Card>
				</Col>
			</Row>
		</div>
	);
};

export default EquipmentDetail;

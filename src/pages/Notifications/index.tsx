import React, { useState, useEffect } from 'react';
import { List, Card, Button, Empty, Space, Badge, Pagination, Avatar, Tag, Modal, message } from 'antd';
import { BellOutlined, CheckOutlined, InfoCircleOutlined, WarningOutlined, CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons';
import { history } from 'umi';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/vi';
import axios from '@/utils/axios';
import styles from './index.less';

dayjs.extend(relativeTime);
dayjs.locale('vi');

interface Notification {
	id: string;
	title: string;
	content: string;
	type: 'info' | 'warning' | 'error' | 'success';
	read: boolean;
	createdDate: string;
	relatedUrl?: string;
}

const Notifications: React.FC = () => {
	const [notifications, setNotifications] = useState<Notification[]>([]);
	const [currentPage, setCurrentPage] = useState(1);
	const pageSize = 10;
	const [loading, setLoading] = useState(true);

	// Fetch notifications
	useEffect(() => {
		const fetchNotifications = async () => {
			setLoading(true);
			try {
				const response = await axios.get('/notifications');
				const sorted = (response.data?.data || []).sort((a: Notification, b: Notification) =>
					dayjs(b.createdDate).diff(dayjs(a.createdDate))
				);
				setNotifications(sorted);
			} catch (error) {
				console.error('Failed to fetch notifications:', error);
				// Mock data
				const mockNotifications: Notification[] = [
					{
						id: '1',
						title: 'Yêu cầu được phê duyệt',
						content: 'Yêu cầu mượn Laptop Dell XPS 15 của bạn đã được phê duyệt. Vui lòng nhận thiết bị tại phòng quản lý.',
						type: 'success',
						read: false,
						createdDate: dayjs().subtract(2, 'hours').format('YYYY-MM-DD HH:mm'),
						relatedUrl: '/my-requests',
					},
					{
						id: '2',
						title: 'Thông báo về ngày trả',
						content: 'Vui lòng nhắc nhở bạn trả Máy Chiếu Epson vào ngày 28/05/2026.',
						type: 'info',
						read: false,
						createdDate: dayjs().subtract(1, 'days').format('YYYY-MM-DD HH:mm'),
						relatedUrl: '/my-requests',
					},
					{
						id: '3',
						title: 'Thiết bị quá hạn',
						content: 'Bạn có thiết bị iPad Pro 12.9 đang quá hạn trả. Vui lòng trả lại ngay hôm nay.',
						type: 'error',
						read: true,
						createdDate: dayjs().subtract(3, 'days').format('YYYY-MM-DD HH:mm'),
						relatedUrl: '/my-requests',
					},
					{
						id: '4',
						title: 'Yêu cầu bị từ chối',
						content: 'Yêu cầu mượn Bộ Vi Xử Lý Raspberry Pi của bạn đã bị từ chối. Lý do: Thiết bị không có sẵn trong khoảng thời gian yêu cầu.',
						type: 'warning',
						read: true,
						createdDate: dayjs().subtract(5, 'days').format('YYYY-MM-DD HH:mm'),
						relatedUrl: '/my-requests',
					},
					{
						id: '5',
						title: 'Thông báo bảo trì hệ thống',
						content: 'Hệ thống sẽ bảo trì vào ngày 01/06/2026 từ 22:00 đến 02:00. Vui lòng không sử dụng trong khoảng thời gian này.',
						type: 'info',
						read: true,
						createdDate: dayjs().subtract(7, 'days').format('YYYY-MM-DD HH:mm'),
					},
				];
				setNotifications(mockNotifications);
			} finally {
				setLoading(false);
			}
		};
		fetchNotifications();
	}, []);

	// Mark notification as read and navigate
	const handleNotificationClick = async (notification: Notification) => {
		try {
			if (!notification.read) {
				await axios.put(`/notifications/${notification.id}/read`);
				setNotifications(
					notifications.map((n) =>
						n.id === notification.id ? { ...n, read: true } : n
					)
				);
			}

			if (notification.relatedUrl) {
				history.push(notification.relatedUrl);
			}
		} catch (error) {
			console.error('Failed to mark notification as read:', error);
		}
	};

	// Mark all as read
	const handleMarkAllRead = async () => {
		Modal.confirm({
			title: 'Đánh dấu tất cả đã đọc',
			content: 'Bạn có chắc muốn đánh dấu tất cả thông báo đã đọc không?',
			okText: 'Có',
			cancelText: 'Không',
			onOk: async () => {
				try {
					await axios.put('/notifications/mark-all-read');
					setNotifications(notifications.map((n) => ({ ...n, read: true })));
					message.success('Đã đánh dấu tất cả thông báo đã đọc');
				} catch (error) {
					message.error('Không thể đánh dấu tất cả thông báo');
				}
			},
		});
	};

	const unreadCount = notifications.filter((n) => !n.read).length;

	// Paginate notifications
	const startIndex = (currentPage - 1) * pageSize;
	const paginatedNotifications = notifications.slice(startIndex, startIndex + pageSize);

	// Get icon based on type
	const getIcon = (type: string) => {
		switch (type) {
			case 'success':
				return <CheckCircleOutlined style={{ color: '#52c41a' }} />;
			case 'error':
				return <CloseCircleOutlined style={{ color: '#ff4d4f' }} />;
			case 'warning':
				return <WarningOutlined style={{ color: '#faad14' }} />;
			default:
				return <InfoCircleOutlined style={{ color: '#1890ff' }} />;
		}
	};

	// Get badge color
	const getBadgeColor = (type: string) => {
		switch (type) {
			case 'success':
				return 'green';
			case 'error':
				return 'red';
			case 'warning':
				return 'orange';
			default:
				return 'blue';
		}
	};

	return (
		<div className={styles.container}>
			<Card
				title="Thông Báo"
				extra={
					<Space>
						{unreadCount > 0 && (
							<Badge count={unreadCount} />
						)}
						<Button
							type="link"
							onClick={handleMarkAllRead}
							disabled={unreadCount === 0}
						>
							Đánh dấu tất cả đã đọc
						</Button>
					</Space>
				}
			>
				{loading ? (
					<div style={{ textAlign: 'center', padding: '50px 0' }}>
						Đang tải...
					</div>
				) : paginatedNotifications.length === 0 ? (
					<Empty
						description="Không có thông báo nào"
						style={{ margin: '50px 0' }}
					/>
				) : (
					<>
						<List
							dataSource={paginatedNotifications}
							renderItem={(notification) => (
								<List.Item
									onClick={() => handleNotificationClick(notification)}
									className={`${styles.notificationItem} ${!notification.read ? styles.unread : ''}`}
									style={{
										padding: '12px 16px',
										backgroundColor: !notification.read ? '#f0f8ff' : 'transparent',
										borderRadius: '6px',
										marginBottom: '8px',
										cursor: 'pointer',
										transition: 'all 0.3s ease',
									}}
								>
									<List.Item.Meta
										avatar={<Avatar icon={getIcon(notification.type)} size={40} />}
										title={
											<div className={styles.title}>
												<span style={{ fontWeight: 600 }}>{notification.title}</span>
												{!notification.read && (
													<Tag color={getBadgeColor(notification.type)}>Chưa đọc</Tag>
												)}
											</div>
										}
										description={
											<div>
												<p style={{ margin: '8px 0 4px 0', color: '#555' }}>
													{notification.content}
												</p>
												<span style={{ color: '#999', fontSize: '12px' }}>
													{dayjs(notification.createdDate).fromNow()}
												</span>
											</div>
										}
									/>
									{!notification.read && (
										<CheckOutlined style={{ color: '#1890ff', marginLeft: '12px' }} />
									)}
								</List.Item>
							)}
						/>

						<div className={styles.pagination}>
							<Pagination
								current={currentPage}
								pageSize={pageSize}
								total={notifications.length}
								onChange={setCurrentPage}
								showSizeChanger={false}
							/>
						</div>
					</>
				)}
			</Card>
		</div>
	);
};

export default Notifications;

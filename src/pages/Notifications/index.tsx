import React, { useState, useEffect } from 'react';
import { List, Card, Button, Empty, Space, Badge, Pagination, Avatar, Tag, Modal, message } from 'antd';
import { CheckOutlined, InfoCircleOutlined, WarningOutlined, CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons';
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
	const [totalItems, setTotalItems] = useState(0);
	const [unreadCount, setUnreadCount] = useState(0);

	// Fetch notifications
	useEffect(() => {
		const fetchNotifications = async () => {
			setLoading(true);
			try {
				const response = await axios.get('/notifications', {
					params: {
						page: currentPage - 1, // backend is 0-based page
						pageSize,
					}
				});
				const data = response.data?.data;
				if (data && Array.isArray(data.items)) {
					const mapped = data.items.map((item: any) => ({
						id: String(item.id),
						title: item.title,
						content: item.message,
						type: item.type === 'REQUEST_APPROVED' ? 'success' :
							item.type === 'REQUEST_REJECTED' ? 'error' :
							item.type === 'REQUEST_OVERDUE' ? 'warning' : 'info',
						read: item.status === 'READ',
						createdDate: item.createdAt,
						relatedUrl: item.link || undefined,
					}));
					setNotifications(mapped);
					setTotalItems(data.total || mapped.length);
					setUnreadCount(Number(data.unreadCount || 0));
				} else {
					setNotifications([]);
					setTotalItems(0);
					setUnreadCount(0);
				}
			} catch (error) {
				console.error('Failed to fetch notifications:', error);
				setNotifications([]);
				setTotalItems(0);
				setUnreadCount(0);
			} finally {
				setLoading(false);
			}
		};
		fetchNotifications();
	}, [currentPage]);

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
				setUnreadCount((prev) => Math.max(0, prev - 1));
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
					await axios.put('/notifications/read-all');
					setNotifications(notifications.map((n) => ({ ...n, read: true })));
					setUnreadCount(0);
					message.success('Đã đánh dấu tất cả thông báo đã đọc');
				} catch (error) {
					message.error('Không thể đánh dấu tất cả thông báo');
				}
			},
		});
	};

	// Paginate notifications
	const paginatedNotifications = notifications;

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
								total={totalItems}
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

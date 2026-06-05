import React, { useState } from 'react';
import { Badge, List, Popover, Button, Avatar, Typography, message } from 'antd';
import { BellOutlined, NotificationOutlined, CheckOutlined } from '@ant-design/icons';
import { history } from 'umi';
import axios from '@/utils/axios';

interface MockNotification {
	id: string;
	title: string;
	description: string;
	datetime: string;
	read: boolean;
	type?: string;
}

const NoticeIconView: React.FC = () => {
	const [notifications, setNotifications] = useState<MockNotification[]>([]);
	const [unreadCount, setUnreadCount] = useState(0);

	const getIcon = (type?: string) => {
		switch (type) {
			case 'REQUEST_APPROVED':
				return <Avatar style={{ backgroundColor: '#52c41a' }} icon={<NotificationOutlined />} />;
			case 'REQUEST_REJECTED':
				return <Avatar style={{ backgroundColor: '#ff4d4f' }} icon={<NotificationOutlined />} />;
			case 'REQUEST_OVERDUE':
				return <Avatar style={{ backgroundColor: '#faad14' }} icon={<NotificationOutlined />} />;
			default:
				return <Avatar style={{ backgroundColor: '#1890ff' }} icon={<NotificationOutlined />} />;
		}
	};

	const loadNotifications = async () => {
		try {
			const response = await axios.get('/notifications', {
				params: {
					page: 0,
					pageSize: 10,
				},
			});
			const data = response.data?.data;
			if (data) {
				setUnreadCount(data.unreadCount || 0);
				if (Array.isArray(data.items)) {
					const mapped = data.items.map((item: any) => ({
						id: String(item.id),
						title: item.title,
						description: item.message,
						datetime: item.createdAt,
						read: item.status === 'READ',
						type: item.type,
					}));
					setNotifications(mapped);
				}
			}
		} catch (e) {
			console.error('Failed to load notifications:', e);
		}
	};

	React.useEffect(() => {
		loadNotifications();
		// Poll every 30 seconds to keep notice count fresh
		const timer = setInterval(loadNotifications, 30000);
		return () => clearInterval(timer);
	}, []);

	const handleItemClick = async (id: string) => {
		try {
			await axios.put(`/notifications/${id}/read`);
			loadNotifications();
			message.info('Đã đánh dấu đọc thông báo.');
		} catch (e) {
			console.error(e);
		}
	};

	const handleMarkAllRead = async () => {
		try {
			await axios.put('/notifications/read-all');
			loadNotifications();
			message.success('Đã đánh dấu đọc tất cả thông báo.');
		} catch (e) {
			console.error(e);
		}
	};

	const notificationList = (
		<div style={{ width: 360 }}>
			<div
				style={{
					display: 'flex',
					justifyContent: 'space-between',
					alignItems: 'center',
					padding: '12px 16px',
					borderBottom: '1px solid #f0f0f0',
				}}
			>
				<Typography.Text strong style={{ fontSize: '15px' }}>
					Thông báo ({unreadCount} chưa đọc)
				</Typography.Text>
				{unreadCount > 0 && (
					<Button
						type='link'
						size='small'
						onClick={handleMarkAllRead}
						icon={<CheckOutlined />}
						style={{ padding: 0 }}
					>
						Đọc tất cả
					</Button>
				)}
			</div>
			<div style={{ maxHeight: 300, overflowY: 'auto' }}>
				<List
					dataSource={notifications}
					renderItem={(item) => (
						<List.Item
							onClick={() => handleItemClick(item.id)}
							style={{
								padding: '12px 16px',
								cursor: 'pointer',
								transition: 'background-color 0.2s',
								backgroundColor: item.read ? 'transparent' : '#e6f7ff50',
							}}
							className='hover-bg-gray'
						>
							<List.Item.Meta
								avatar={getIcon(item.type)}
								title={
									<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
										<span style={{ fontWeight: item.read ? 500 : 600, color: item.read ? '#595959' : '#000' }}>
											{item.title}
										</span>
										{!item.read && (
											<span
												style={{
													width: 6,
													height: 6,
													borderRadius: '50%',
													backgroundColor: '#1890ff',
												}}
											/>
										)}
									</div>
								}
								description={
									<div>
										<div style={{ color: '#8c8c8c', fontSize: '12px', margin: '4px 0' }}>
											{item.description}
										</div>
										<div style={{ color: '#bfbfbf', fontSize: '11px' }}>{item.datetime}</div>
									</div>
								}
							/>
						</List.Item>
					)}
				/>
			</div>
			<div
				style={{
					padding: '10px 16px',
					borderTop: '1px solid #f0f0f0',
					textAlign: 'center',
				}}
			>
				<Button type='link' block style={{ padding: 0 }} onClick={() => history.push('/notifications')}>
					Xem tất cả thông báo
				</Button>
			</div>
		</div>
	);

	return (
		<Popover
			content={notificationList}
			trigger='click'
			placement='bottomRight'
			overlayClassName='notice-popover'
			overlayStyle={{ padding: 0 }}
		>
			<span
				style={{
					display: 'inline-flex',
					alignItems: 'center',
					justifyContent: 'center',
					height: '100%',
					padding: '0 12px',
					cursor: 'pointer',
					transition: 'all 0.3s',
				}}
				className='action-bell'
			>
				<Badge count={unreadCount} offset={[2, -2]}>
					<BellOutlined style={{ fontSize: '18px', color: 'rgba(0, 0, 0, 0.65)' }} />
				</Badge>
			</span>
		</Popover>
	);
};

export default NoticeIconView;

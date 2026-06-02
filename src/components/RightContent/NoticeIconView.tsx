import React, { useState } from 'react';
import { Badge, List, Popover, Button, Avatar, Typography, message } from 'antd';
import { BellOutlined, NotificationOutlined, CheckOutlined } from '@ant-design/icons';
import { history } from 'umi';

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

	const getIcon = (type?: string) => {
		switch (type) {
			case 'success':
				return <Avatar style={{ backgroundColor: '#52c41a' }} icon={<NotificationOutlined />} />;
			case 'error':
				return <Avatar style={{ backgroundColor: '#ff4d4f' }} icon={<NotificationOutlined />} />;
			case 'warning':
				return <Avatar style={{ backgroundColor: '#faad14' }} icon={<NotificationOutlined />} />;
			default:
				return <Avatar style={{ backgroundColor: '#1890ff' }} icon={<NotificationOutlined />} />;
		}
	};

	const loadNotifications = () => {
		const stored = localStorage.getItem('mockNotifications');
		if (stored) {
			try {
				const list = JSON.parse(stored);
				const mapped = list.map((item: any) => ({
					id: item.id,
					title: item.title,
					description: item.content,
					datetime: item.createdDate,
					read: item.read,
					type: item.type,
				}));
				setNotifications(mapped);
			} catch (e) {
				console.error(e);
			}
		}
	};

	React.useEffect(() => {
		loadNotifications();
		// Listen for local storage updates to keep header sync'd
		window.addEventListener('storage', loadNotifications);
		return () => window.removeEventListener('storage', loadNotifications);
	}, []);

	const unreadCount = notifications.filter((item) => !item.read).length;

	const handleItemClick = (id: string) => {
		const stored = localStorage.getItem('mockNotifications');
		if (stored) {
			try {
				const list = JSON.parse(stored);
				const updated = list.map((item: any) => (item.id === id ? { ...item, read: true } : item));
				localStorage.setItem('mockNotifications', JSON.stringify(updated));
				loadNotifications();
				message.info('Đã đánh dấu đọc thông báo.');
			} catch (e) {
				console.error(e);
			}
		}
	};

	const handleMarkAllRead = () => {
		const stored = localStorage.getItem('mockNotifications');
		if (stored) {
			try {
				const list = JSON.parse(stored);
				const updated = list.map((item: any) => ({ ...item, read: true }));
				localStorage.setItem('mockNotifications', JSON.stringify(updated));
				loadNotifications();
				message.success('Đã đánh dấu đọc tất cả thông báo.');
			} catch (e) {
				console.error(e);
			}
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

import React, { useState } from 'react';
import { Badge, List, Popover, Button, Space, Avatar, Typography, Tooltip, message } from 'antd';
import { BellOutlined, MailOutlined, NotificationOutlined, CheckOutlined } from '@ant-design/icons';

interface MockNotification {
	id: string;
	title: string;
	description: string;
	datetime: string;
	read: boolean;
	avatar: React.ReactNode;
}

const NoticeIconView: React.FC = () => {
	const [notifications, setNotifications] = useState<MockNotification[]>([
		{
			id: '1',
			title: 'Thông báo đóng học phí học kỳ II',
			description: 'Hạn cuối đóng học phí học kỳ II là ngày 30/06/2026. Sinh viên chú ý thanh toán đúng hạn.',
			datetime: '5 phút trước',
			read: false,
			avatar: <Avatar style={{ backgroundColor: '#ff4d4f' }} icon={<NotificationOutlined />} />,
		},
		{
			id: '2',
			title: 'Lịch thi học kỳ mới',
			description: 'Đã có lịch thi chính thức cho các môn học kỳ II. Vui lòng kiểm tra phòng thi và số báo danh.',
			datetime: '2 giờ trước',
			read: false,
			avatar: <Avatar style={{ backgroundColor: '#1890ff' }} icon={<NotificationOutlined />} />,
		},
		{
			id: '3',
			title: 'Đăng ký đề tài Nghiên cứu khoa học',
			description: 'Mở đăng ký đề tài NCKH sinh viên cấp Học viện năm học 2026. Hạn đăng ký trước 15/06.',
			datetime: '1 ngày trước',
			read: true,
			avatar: <Avatar style={{ backgroundColor: '#52c41a' }} icon={<NotificationOutlined />} />,
		},
		{
			id: '4',
			title: 'Thông báo kết quả điểm rèn luyện',
			description: 'Đã cập nhật điểm rèn luyện dự kiến Học kỳ I. Sinh viên khiếu nại trước ngày 30/05.',
			datetime: '3 ngày trước',
			read: true,
			avatar: <Avatar style={{ backgroundColor: '#faad14' }} icon={<NotificationOutlined />} />,
		},
	]);

	const unreadCount = notifications.filter((item) => !item.read).length;

	const handleItemClick = (id: string) => {
		setNotifications(
			notifications.map((item) => (item.id === id ? { ...item, read: true } : item))
		);
		message.info('Đã đánh dấu đọc thông báo.');
	};

	const handleMarkAllRead = () => {
		setNotifications(notifications.map((item) => ({ ...item, read: true })));
		message.success('Đã đánh dấu đọc tất cả thông báo.');
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
								avatar={item.avatar}
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
				<Button type='link' block style={{ padding: 0 }}>
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
